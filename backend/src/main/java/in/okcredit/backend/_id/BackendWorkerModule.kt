package `in`.okcredit.backend._id

import `in`.okcredit.backend._offline._hack.FetchVersionTask
import `in`.okcredit.backend._offline.usecase.*
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncCustomer
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncCustomersImpl
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncDirtyTransactions
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.backend.contract.HomeDataSyncWorker
import `in`.okcredit.backend.contract.HomeRefreshSyncWorker
import `in`.okcredit.backend.contract.NonActiveBusinessesDataSyncWorker
import `in`.okcredit.backend.contract.PeriodicDataSyncWorker
import `in`.okcredit.backend.worker.HomeDataSyncWorkerImpl
import `in`.okcredit.backend.worker.HomeRefreshSyncWorkerImpl
import `in`.okcredit.backend.worker.NonActiveBusinessesDataSyncWorkerImpl
import `in`.okcredit.backend.worker.PeriodicDataSyncWorkerImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.multibindings.IntoMap
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey

@Module
abstract class BackendWorkerModule {

    @Binds
    @Reusable
    abstract fun homeDataSyncWorker(homeDataSyncWorker: HomeDataSyncWorkerImpl): HomeDataSyncWorker

    @Binds
    @Reusable
    abstract fun homeRefreshSync(homeRefreshSync: HomeRefreshSyncWorkerImpl): HomeRefreshSyncWorker

    @Binds
    @Reusable
    abstract fun periodicDataSyncWorker(periodicDataSyncWorker: PeriodicDataSyncWorkerImpl): PeriodicDataSyncWorker

    @Binds
    @Reusable
    abstract fun nonActiveBusinessesDataSyncWorker(
        nonActiveBusinessesDataSyncWorker: NonActiveBusinessesDataSyncWorkerImpl
    ): NonActiveBusinessesDataSyncWorker

    @Binds
    @IntoMap
    @WorkerKey(FetchVersionTask::class)
    @Reusable
    abstract fun fetchVersionTask(factory: FetchVersionTask.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncCustomer.Worker::class)
    @Reusable
    abstract fun workerSyncCustomer(factory: SyncCustomer.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncTransactionsImpl.Worker::class)
    @Reusable
    abstract fun workerSyncTransactions(factory: SyncTransactionsImpl.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncCustomersImpl.Worker::class)
    @Reusable
    abstract fun workerSyncCustomers(factory: SyncCustomersImpl.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncDirtyTransactions.Worker::class)
    abstract fun workerSyncDirtyTransactions(factory: SyncDirtyTransactions.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(LinkDevice.Worker::class)
    @Reusable
    abstract fun WorkerLinkDevice(factory: LinkDevice.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(DueInfoSyncer.Worker::class)
    @Reusable
    abstract fun WorkerDueInfoSyncer(factory: DueInfoSyncer.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(DueInfoParticularCustomerSyncer.Worker::class)
    @Reusable
    abstract fun WorkerDueInfoParticularCustomerSyncer(factory: DueInfoParticularCustomerSyncer.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncDeleteTransactionImage.Worker::class)
    @Reusable
    abstract fun WorkerSyncDeleteTransactionImage(factory: SyncDeleteTransactionImage.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(UpdateTransactionNote.Worker::class)
    @Reusable
    abstract fun WorkerUpdateTransactionNote(factory: UpdateTransactionNote.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncTransactionImage.Worker::class)
    @Reusable
    abstract fun WorkerSyncTransactionImage(factory: SyncTransactionImage.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SubmitFeedbackImpl.Worker::class)
    @Reusable
    abstract fun WorkerSubmitFeedbackImpl(factory: SubmitFeedbackImpl.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncContactsWithAccount.Worker::class)
    @Reusable
    abstract fun WorkerSyncContactsWithAccount(factory: SyncContactsWithAccount.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncCustomerTxnAlert.Worker::class)
    @Reusable
    abstract fun WorkerSyncCustomerTxnAlert(factory: SyncCustomerTxnAlert.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(CustomerTxnAlertDialogDismissWorker.Worker::class)
    @Reusable
    abstract fun WorkerCustomerTxnAlertDialogDismissWorker(factory: CustomerTxnAlertDialogDismissWorker.Worker.Factory): ChildWorkerFactory
}
