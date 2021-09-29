package `in`.okcredit.backend._offline.usecase._sync_usecases

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend._offline.server.BackendRemoteSource
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

// sync a locally saved "dirty" transaction
class SyncDirtyTransaction @Inject
constructor(
    private val transactionRepo: Lazy<TransactionRepo>,
    private val remoteSource: Lazy<BackendRemoteSource>,
    private val tracker: Lazy<Tracker>,
) {

    companion object {
        const val TAG = "<<<<SyncTransaction"
    }

    fun execute(txnId: String, businessId: String): Completable {
        tracker.get().trackSyncDirtyTransaction("1_Started_Sync", txnId)
        return transactionRepo.get().getTransaction(txnId, businessId)
            .doOnNext { Timber.d("$TAG Transaction found in local DB ${it.id}") }
            .firstOrError()
            .flatMapCompletable { localCopy ->
                if (localCopy.amountV2 == 0L) { // to fix invalid_txn_amount bug
                    tracker.get().trackDebug("fix_invalid_txn_amount", mapOf("transaction_id" to txnId))
                    transactionRepo.get().deleteTransaction(txnId)
                } else {
                    sync(localCopy, businessId)
                }
            }
    }

    private fun sync(localCopy: merchant.okcredit.accounting.model.Transaction, businessId: String): Completable {
        // proceed only if the local transaction is dirty
        // check if local transaction exists on server, if no, create it (idempotent on server for exactly same request)
        // check if local transaction is deleted, if yes, delete it on server (idempotent on server)

        Timber.d("$TAG Started Syncing Transaction: $localCopy")

        if (!localCopy.isDirty) {
            Timber.d("$TAG Transaction is not dirty")
            return Completable.complete()
        }

        tracker.get().trackSyncDirtyTransaction("2_Pre_Server", localCopy.id, localCopy.customerId)

        return remoteSource.get().getTransaction(localCopy.id, businessId)
            .onErrorResumeNext {
                Timber.i("$TAG Transaction not found in server. Creating one")
                tracker.get().trackSyncDirtyTransaction("3_Server_Call", localCopy.id, localCopy.customerId)

                // if txn is not present on server then create it
                remoteSource.get().addTransaction(
                    localCopy.customerId,
                    localCopy.id,
                    localCopy.type,
                    localCopy.amountV2,
                    localCopy.receiptUrl,
                    localCopy.note,
                    localCopy.createdAt,
                    localCopy.isOnboarding,
                    localCopy.billDate,
                    localCopy.isSmsSent,
                    localCopy.inputType,
                    localCopy.voiceId,
                    businessId
                )
            }
            .flatMap {
                tracker.get().trackSyncDirtyTransaction("4_Server_Completed", localCopy.id, it.customerId, it.id)
                Timber.i("$TAG Get Server Transaction call Success")

                // if txn is deleted locally but not deleted on server, delete it on server
                var deleteCompletable = Completable.complete()
                if (localCopy.isDeleted && !it.isDeleted) {
                    tracker.get().trackSyncDirtyTransaction("4-1_Pre_Delete_Txn", localCopy.id, it.customerId, it.id)
                    deleteCompletable = remoteSource.get().deleteTransaction(it.id, businessId)
                        .doOnComplete {
                            tracker.get()
                                .trackSyncDirtyTransaction("4-2_Post_Delete_Txn", localCopy.id, it.customerId, it.id)
                        }
                }
                return@flatMap deleteCompletable.andThen(Single.just(it))
            }
            .flatMapCompletable {
                tracker.get().trackSyncDirtyTransaction("5_Completed_Sync", localCopy.id, it.customerId, it.id)
                Timber.i("$TAG Replace Transaction")

                // replace local copy of the transaction with server copy
                transactionRepo.get().replaceTransaction(localCopy.id, it, businessId)
            }
    }
}
