package tech.okcredit.account_chat_sdk.use_cases

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.account_chat_contract.FIELDS
import tech.okcredit.account_chat_contract.IGetChatUnreadMessageCount
import tech.okcredit.account_chat_sdk.ChatProvider
import javax.inject.Inject

class GetChatUnreadMessageCount @Inject constructor(
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
) :
    IGetChatUnreadMessageCount {
    companion object {
        private const val UNREAD_CHAT_MAX_LIMIT_COUNT = "unread_chat_max_limit_count"
    }

    private var registration: ListenerRegistration? = null
    private var firstUnseenMessageId: String? = null

    data class Request(val accountId: String)

    override fun execute(accountId: String): Observable<Result<Pair<String, String?>>> {
        return UseCase.wrapObservable(
            getDistinctMerchant()
                .flatMap { business ->
                    getChatStatus().flatMap {
                        getUnreadCountFromFirestore(accountId, business)
                    }.doOnDispose {
                        registration?.remove()
                        registration = null
                    }
                }
        )
    }

    override fun getUnreadCountForRelation(relation: String): Observable<HashMap<String, Long>> {
        return getDistinctMerchant()
            .flatMap {
                getUnreadCountFromFireStoreForRelation(business = it, relation)
            }.doOnDispose {
                registration?.remove()
                registration = null
            }
    }

    fun getChatStatus(): Observable<Boolean> {
        return ChatProvider.isConnected().filter { it }
            .distinctUntilChanged { t1: Boolean, t2: Boolean ->
                t1 == t2
            }
    }

    fun getDistinctMerchant(): Observable<Business> {
        return getActiveBusiness.get().execute()
            .distinctUntilChanged { t1: Business, t2: Business ->
                t1.id == t2.id
            }
    }

    fun getUnreadCountFromFirestore(
        accountId: String,
        business: Business,
    ): Observable<Pair<String, String?>> {
        return Observable.create { emitter ->
            registration = ChatProvider.getUnreadFunction(accountId, business.id)
                .addSnapshotListener { querySnapshot, p1 ->
                    querySnapshot?.let {
                        if (firstUnseenMessageId == null && it.documents.size > 0) {
                            firstUnseenMessageId = it.documents[0].id
                        }
                        if (it.documents.size == 0) {
                            firstUnseenMessageId = null
                        }
                        emitter.onNext(it.documents.size.toString() to firstUnseenMessageId)
                    }
                }
        }
    }

    fun getUnreadCountFromFireStoreForRelation(
        business: Business,
        relationship: String,
    ): Observable<HashMap<String, Long>> {
        return Observable.create { emitter ->
            registration = ChatProvider.getUnreadFunctionAllAccounts(
                business.id,
                relationship,
                firebaseRemoteConfig.get().getLong(UNREAD_CHAT_MAX_LIMIT_COUNT)
            )

                .addSnapshotListener { querySnapshot, _ ->
                    querySnapshot?.let { snapshot ->

                        val accountIdMessageCountMap =
                            snapshot.documents.groupingBy { it[FIELDS.ACCOUNT_ID] }.eachCount()

                        emitter.onNext(HashMap(accountIdMessageCountMap) as HashMap<String, Long>)
                    } ?: run {
                        emitter.onNext(HashMap())
                    }
                }
        }
    }
}
