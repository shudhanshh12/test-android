package tech.okcredit.account_chat_sdk

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import tech.okcredit.account_chat_contract.FEATURE
import tech.okcredit.account_chat_contract.SignOutFirebaseAndRemoveChatListener
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.base.crashlytics.RecordException
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@Reusable
class ChatCore @Inject constructor(
    private val server: Lazy<AccountsChatRemoteSourceImpl>,
    private val authService: Lazy<AuthService>,
    private val ab: Lazy<AbRepository>,
    private val firestore: Lazy<FirebaseFirestore>,
    private val fireAuth: Lazy<FirebaseAuth>,
    private val chatListener: Lazy<IChatListner>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : SignOutFirebaseAndRemoveChatListener {
    private var activityCount: Int = 0

    private var isFirestoreInitialised = AtomicBoolean(false)

    private fun authState(): Observable<Boolean> {
        return authService.get().authState()
    }

    private fun currentFireBaseUser(): Observable<FirebaseUser> {
        val firebaseUser = getActiveBusinessId.get().execute().flatMap { _businessId ->
            server.get().getToken(_businessId).flatMap {
                Single.fromCallable {
                    val mAuth = fireAuth.get()
                    val authResultTask = mAuth.signInWithCustomToken(it.token)
                    val authResult = Tasks.await(authResultTask)
                    authResult.user
                }.subscribeOn(Schedulers.newThread())
            }
        }.toObservable()
        return Observable.merge(
            firebaseUser,
            Observable.interval(50, TimeUnit.MINUTES).flatMap { firebaseUser }
        )
    }

    private fun startListeningMessages(businessId: String): Completable {
        return Completable.fromAction { chatListener.get().executeMessages(businessId) }
    }

    private fun checkIsChatEnabled(businessId: String): Observable<Boolean> {
        return ab.get().isFeatureEnabled(FEATURE.FEATURE_ACCOUNT_CHATS, businessId = businessId)
    }

    private fun initFireBaseSettings(): Completable {
        return Completable.fromAction {
            val settings = FirebaseFirestoreSettings.Builder()
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            firestore.get().firestoreSettings = settings
        }
    }

    fun initialise() {
        if (isFirestoreInitialised.get().not() && activityCount > 0) {
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                if (businessId.isEmpty()) return@flatMapCompletable Completable.complete()
                isFirestoreInitialised.set(true)
                checkIsChatEnabled(businessId).doOnError {
                    RecordException.recordException(it)
                    isFirestoreInitialised.set(false)
                }.filter { it }.distinctUntilChanged { t1: Boolean, t2: Boolean -> t1 == t2 }.flatMap {
                    initFireBaseSettings().andThen(Observable.just(it))
                }.flatMapCompletable {
                    authState()
                        .flatMapCompletable {
                            return@flatMapCompletable if (it) {
                                if (businessId == fireAuth.get().currentUser?.uid) {
                                    startListeningMessages(businessId).doOnComplete {
                                        ChatProvider.connectionSubject.onNext(true)
                                    }.andThen(FirebaseRDB.initRDB(businessId))
                                } else {
                                    currentFireBaseUser().flatMapCompletable {
                                        startListeningMessages(businessId).doOnComplete {
                                            Timber.i("firestoreapi Complete start listening ")
                                            ChatProvider.connectionSubject.onNext(true)
                                        }.andThen(FirebaseRDB.initRDB(businessId))
                                    }
                                }
                            } else signOutFireBaseAndRemoveChatListener()
                        }
                }
            }.subscribeOn(Schedulers.io())
                .doOnError {
                    RecordException.recordException(it)
                    isFirestoreInitialised.set(false)
                }
                .subscribe(
                    {
                    },
                    {
                        isFirestoreInitialised.set(false)
                        RecordException.recordException(it)
                    }
                )
        }
    }

    private fun clearPersistence(): Completable {
        return Completable.fromAction {
            firestore.get().clearPersistence()
        }
    }

    fun onActivityCreated() {
        activityCount++
        initialise()
    }

    fun onActivityDestroyed() {
        activityCount--
        initialise()
    }

    private fun signOutFireBaseAndRemoveChatListener() = Completable.fromAction {
        fireAuth.get().signOut()
    }.andThen(clearPersistence())
        .doOnComplete {
            chatListener.get().removeListener()
            ChatProvider.connectionSubject.onNext(false)
        }

    override fun execute(): Completable {
        return signOutFireBaseAndRemoveChatListener().andThen(
            Completable.fromCallable {
                isFirestoreInitialised.set(false)
            }
        )
    }

    companion object {

        fun setActiveAccountId(activeAccountId: String?) {
            ChatListener.setActiveAccountId(activeAccountId)
        }
    }
}
