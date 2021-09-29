package tech.okcredit.account_chat_sdk

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import tech.okcredit.account_chat_contract.COLLECTIONS
import tech.okcredit.account_chat_contract.FIELDS
import tech.okcredit.account_chat_contract.FIELDS.METAINFO_RECEIVER_ROLE
import java.util.concurrent.ConcurrentHashMap

object ChatProvider {

    private val unseenCountQueryProviderMap = ConcurrentHashMap<String, Query>()
    val connectionSubject = BehaviorSubject.create<Boolean>()
    private var messageCollectionReference: CollectionReference? = null

    fun isConnected(): Observable<Boolean> {
        return connectionSubject.flatMap {
            Observable.just(it)
        }
    }

    fun getUnreadFunction(accountId: String, merchantId: String): Query {
        return if (unseenCountQueryProviderMap.containsKey(accountId) && unseenCountQueryProviderMap[accountId] != null) {
            unseenCountQueryProviderMap[accountId]!!
        } else {
            val query = provideMessagesCollectionPath(merchantId)
                .whereEqualTo(FIELDS.ACCOUNT_ID, accountId)
                .whereEqualTo(FIELDS.FIRST_SEEN_TIME, null)
                .whereEqualTo(FIELDS.SENT_BY_ME, false)
                .orderBy(FIELDS.ORDER_FOR_ME, Query.Direction.ASCENDING)
            unseenCountQueryProviderMap[accountId] = query
            unseenCountQueryProviderMap[accountId]!!
        }
    }

    fun getUnreadFunctionAllAccounts(merchantId: String, relationship: String, unreadMaxLimitCount: Long): Query {
        return provideMessagesCollectionPath(merchantId)
            .whereEqualTo(FIELDS.FIRST_SEEN_TIME, null)
            .whereEqualTo(FIELDS.SENT_BY_ME, false)
            .whereEqualTo(METAINFO_RECEIVER_ROLE, relationship)
            .orderBy(FIELDS.ORDER_FOR_ME, Query.Direction.ASCENDING)
            .limit(unreadMaxLimitCount)
    }

    fun provideMessagesCollectionPath(id: String): CollectionReference {
        if (messageCollectionReference == null) {
            synchronized(this) {
                messageCollectionReference =
                    FirebaseFirestore.getInstance().collection(COLLECTIONS.MERCHANTS).document(id)
                        .collection(COLLECTIONS.MESSAGES)
            }
        }
        return messageCollectionReference!!
    }

    fun clearCache() {
        unseenCountQueryProviderMap.clear()
        messageCollectionReference = null
    }
}
