package `in`.okcredit.merchant.suppliercredit

import `in`.okcredit.merchant.suppliercredit.server.internal.ApiEntityMapper.toEntityMapper
import `in`.okcredit.merchant.suppliercredit.server.internal.ApiMessages
import `in`.okcredit.merchant.suppliercredit.tracker.SupplierEventTracker
import `in`.okcredit.merchant.suppliercredit.utils.RxJavaUtils
import `in`.okcredit.shared.utils.CommonUtils.currentDateTime
import android.content.Context
import androidx.work.*
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.rx2.await
import org.joda.time.DateTime
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseCoroutineWorker
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncerImpl @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
    private val store: Lazy<SupplierLocalSource>,
    private val server: Lazy<SupplierRemoteSource>,
    schedulerProvider: Lazy<SchedulerProvider>,
    private val supplierEventTracker: Lazy<SupplierEventTracker>,
) : ISyncer {

    companion object {
        const val WORKER_TAG_BASE = "suppliercredit"
        const val WORKER_TAG_SUPPLIER = "suppliercredit/supplier"
        const val WORKER_TAG_TRANSACTION = "suppliercredit/transaction"
        const val WORKER_TAG_SYNC_EVERYTHING = "suppliercredit/syncEverything"
        const val WORKER_TAG_SYNC_SUPPLIER_CREDIT_EVERYTHING = "suppliercredit/syncSupplierEnabledCustomerIds"
        const val WORKER_TAG_SYNC_NOTIFICATION_REMINDER = "suppliercredit/notificationReminder"
    }

    init {
        getLastSyncEverythingTime().subscribeOn(schedulerProvider.get().io()).subscribe()
    }

    /********** Supplier **********/
    private fun syncSupplierTag(supplierId: String): String = "$WORKER_TAG_SUPPLIER/$supplierId"

    override fun syncSupplier(supplierId: String, businessId: String): Completable {

        return scheduleSyncSupplier(supplierId, businessId)
            .andThen(waitForWorkCompletion(syncSupplierTag(supplierId), businessId).first(false))
            .ignoreElement()
    }

    override fun syncSpecificSupplier(supplierId: String, businessId: String): Completable {
        Timber.i("<<<<[syncSpecificSupplier  Started")
        return server.get().getSupplier(supplierId, businessId)
            .flatMapCompletable { server ->
                store.get().saveSupplier(server, businessId).doOnComplete {
                    Timber.i("<<<<[syncSpecificSupplier  store.saveSupplier(server)=%s", currentDateTime())
                }
            }
    }

    override fun scheduleSyncSupplier(supplierId: String, businessId: String): Completable {
        return Completable.fromAction {
            Timber.i("<<SyncingTime  scheduleSyncSupplier=%s", currentDateTime())
            val workName = syncSupplierTag(supplierId)

            val workRequest = OneTimeWorkRequestBuilder<SyncSupplierWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(WORKER_TAG_BASE)
                .addTag(WORKER_TAG_SUPPLIER)
                .addTag(workName)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
                .setInputData(
                    workDataOf(
                        SyncSupplierWorker.SUPPLIER_ID to supplierId,
                        SyncSupplierWorker.BUSINESS_ID to businessId
                    )
                )
                .build()
                .enableWorkerLogging()

            workManager.get()
                .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.KEEP, workRequest)
            Timber.i("scheduled sync supplier (supplier_id = $supplierId, work_id = ${workRequest.id}")
        }
    }

    override fun syncAllSuppliers(businessId: String): Completable {

        return server.get().getSuppliers(businessId)
            .flatMapCompletable {
                store.get().saveSuppliers(it, businessId)
                    .observeOn(ThreadUtils.worker())
            }
    }

    // Worker
    class SyncSupplierWorker constructor(
        context: Context,
        private val params: WorkerParameters,
        private val store: Lazy<SupplierLocalSource>,
        private val server: Lazy<SupplierRemoteSource>,
        private val syncer: Lazy<ISyncer>,
    ) : BaseRxWorker(context, params) {

        override fun doRxWork(): Completable {

            Timber.i("SyncSupplierWorker created")
            val supplierId = inputData.getString(SUPPLIER_ID)!!
            val businessId = inputData.getString(BUSINESS_ID)!!
            return syncSupplier(supplierId, businessId)
        }

        class Factory @Inject constructor(
            private val store: Lazy<SupplierLocalSource>,
            private val server: Lazy<SupplierRemoteSource>,
            private var syncer: Lazy<ISyncer>,
        ) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return SyncSupplierWorker(context, params, store, server, syncer)
            }
        }

        private fun syncSupplier(supplierId: String, businessId: String): Completable {
            return server.get().getSupplier(supplierId, businessId)
                .flatMapCompletable { server ->
                    store.get().saveSupplier(server, businessId).doOnComplete {
                        Timber.i("<<SyncingTime  store.saveSupplier(server)=%s", currentDateTime())
                    }
                }
                .andThen(
                    server.get().getTransactionOfSupplier(supplierId, businessId)
                        .flatMapCompletable {
                            store.get().saveTransactions(it, businessId).observeOn(ThreadUtils.worker())
                        }
                )
        }

        companion object {
            const val SUPPLIER_ID = "supplier_id"
            const val BUSINESS_ID = "business_id"
        }
    }

    private fun waitForWorkCompletion(name: String, businessId: String): Observable<Boolean> {
        val isCompleted = BehaviorSubject.create<Boolean>()

        var completed = true
        val workInfo = workManager.get().getWorkInfosForUniqueWork(name, Scope.Business(businessId)).get()

        if (workInfo.isNullOrEmpty()) {
            completed = true
            isCompleted.onNext(completed)
        } else {
            // on WorkInfo.State.FAILED case , it will automatically retry , so nothing to do
            workInfo.forEach {
                completed = when (it.state) {
                    WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> false
                    else -> true
                }
            }
            isCompleted.onNext(completed)
        }
        Timber.i("waitForWorkCompletion supplier sync $completed")
        return isCompleted.distinctUntilChanged()
    }

    /********** Transaction **********/
    private fun syncTransactionTag(txnId: String): String = "$WORKER_TAG_TRANSACTION/$txnId"

    override fun scheduleSyncTransaction(txnId: String, businessId: String): Completable {
        Timber.i("addTransaction 5 scheduleSyncTransaction")

        return Completable.fromAction {
            Timber.i("addTransaction 55 scheduleSyncTransaction")
            val workName = syncTransactionTag(txnId)

            val workRequest = OneTimeWorkRequestBuilder<SyncTransactionWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(WORKER_TAG_BASE)
                .addTag(WORKER_TAG_TRANSACTION)
                .addTag(workName)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
                .setInputData(
                    workDataOf(
                        SyncTransactionWorker.TXN_ID to txnId,
                        SyncTransactionWorker.BUSINESS_ID to businessId
                    )
                )
                .build()
                .enableWorkerLogging()

            workManager.get()
                .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.KEEP, workRequest)
            Timber.i("scheduled sync transaction (txn_id = $txnId, work_id = ${workRequest.id}")
        }
            .subscribeOn(ThreadUtils.database())
    }

    override fun scheduleSyncSupplierEnabledCustomerIds(businessId: String): Completable {
        return Completable.fromAction {
            val workName = WORKER_TAG_SYNC_SUPPLIER_CREDIT_EVERYTHING

            val workRequest = OneTimeWorkRequestBuilder<SyncSupplierEnabledCustomerIdsWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    workDataOf(
                        SyncSupplierEnabledCustomerIdsWorker.BUSINESS_ID to businessId
                    )
                )
                .addTag(WORKER_TAG_BASE)
                .addTag(WORKER_TAG_SYNC_SUPPLIER_CREDIT_EVERYTHING)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()
                .enableWorkerLogging()

            workManager.get()
                .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
        }
            .subscribeOn(ThreadUtils.database())
    }

    override fun executeSyncSupplierAndTransactions(supplierId: String, businessId: String): Completable {
        return server.get().getSupplier(supplierId, businessId)
            .flatMapCompletable { server ->
                store.get().saveSupplier(server, businessId).doOnComplete {
                    Timber.i("<<SyncingTime  store.saveSupplier(server)=%s", currentDateTime())
                }
            }
            .andThen(
                server.get().getTransactionOfSupplier(supplierId, businessId)
                    .doAfterSuccess {
                        Timber.i(
                            "<<SyncingTime  server.getTransactionOfSupplier(supplierId)=%s",
                            currentDateTime()
                        )
                    }
                    .flatMapCompletable {

                        store.get().saveTransactions(it, businessId).observeOn(ThreadUtils.worker())
                            .doOnComplete {
                                Timber.i("removeAllTransaction 1 size=${it.size}")
                                Timber.i("<<<SyncingTime sync completed")
                            }
                    }
            )
    }

    override fun scheduleNotificationReminderSync(businessId: String): Completable {
        return Completable.fromAction {
            val workName = WORKER_TAG_SYNC_NOTIFICATION_REMINDER

            val workRequest = OneTimeWorkRequestBuilder<SyncNotificationReminderWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    workDataOf(
                        SyncNotificationReminderWorker.BUSINESS_ID to businessId
                    )
                )
                .addTag(WORKER_TAG_SYNC_NOTIFICATION_REMINDER)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()
                .enableWorkerLogging()

            workManager.get()
                .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
        }.subscribeOn(ThreadUtils.database())
    }

    override fun syncAllDirtyTransactions(businessId: String): Completable {
        Timber.i("<<syncAllDirtyTransactions executed")
        return store.get().listDirtyTransactions(businessId)
            .firstOrError()
            .flatMapCompletable { transactions ->
                if (transactions.isNullOrEmpty()) {
                    Timber.i("<<syncAllDirtyTransactions completed")
                    Completable.complete()
                } else {
                    val jobs = ArrayList<Completable>()
                    transactions.map {
                        jobs.add(syncDirtyTransaction(it.id, businessId).ignoreElement())
                    }
                    RxJavaUtils.runConcurrently(jobs, 4)
                }
            }
    }

    // if dirty transaction is of 'added' type then we can simply send to server for syncing
    // if dirty transaction is of 'deleted' type then check these conditions
    // 1. check whether this deleted transaction was synced when is was in added state , If synced then directly deleted this transaction from server (sync this transaction)
    // 2. if this transaction was not synced with server when it was added (user added and deleted this transaction while offline), then server does know that this transaction exist
    // so we first add this transaction to server , then delete this transaction from server for syncing

    // once every syncing is done , finally we remove tha transaction that is in db with local transaction id via store.removeTransaction(localTxn.id)
    // so that we don't get duplicate transactions in UI , because local id's gets changes when synced with server
    override fun syncDirtyTransaction(transactionId: String, businessId: String): Single<String> {
        Timber.i("Transaction id = $transactionId")
        return store.get().getTransaction(transactionId, businessId).firstOrError()
            .flatMap { localTxn ->

                val deleted = localTxn.deleted
                val unsynced =
                    localTxn.lastSyncTime == null || localTxn.lastSyncTime.millis == 0L // was unsynced when it was added

                if (deleted) {
                    if (unsynced) {

                        Timber.i("unsyncedTransaction 1 id=${localTxn.id} amount=${localTxn.amount}")

                        server.get().addTransaction(localTxn, businessId)
                            .flatMap { serverTxn ->
                                server.get().deleteTransaction(serverTxn.id, businessId)
                                    // now local transaction was added and deleted from server one after another , now we only have to replace local transaction id
                                    // with server transaction id and change syncing =true
                                    .andThen(
                                        store.get().saveTransaction(
                                            localTxn.copy(id = serverTxn.id, syncing = true),
                                            businessId
                                        )
                                    )
                                    .andThen(store.get().removeTransaction(localTxn.id))
                                    .andThen(Single.just(serverTxn.id))
                            }
                    } else {
                        //  'deleted' and was 'synced' when the transaction was added , so localTxn.id is same  as server transaction id
                        // so, no need to use store.removeTransaction(localTxn.id)
                        server.get().deleteTransaction(localTxn.id, businessId)
                            .andThen(store.get().saveTransaction(localTxn.copy(syncing = true), businessId))
                            .andThen(Single.just(localTxn.id))
                    }
                } else { // 'add' transaction
                    server.get().addTransaction(localTxn, businessId)
                        .flatMap { server ->
                            store.get().saveTransaction(server.copy(syncing = true), businessId)
                                .andThen(store.get().removeTransaction(transactionId))
                                .andThen(Single.just(server.id))
                        }
                }
            }
            .subscribeOn(ThreadUtils.database())
    }

    override fun syncAllTransactions(startTime: DateTime?, businessId: String): Completable {
        return server.get().getTransactions(startTime, businessId)
            .doOnEvent { t1, t2 -> Timber.i("supplierThread syncAllTransactions ${Thread.currentThread()}") }
            .flatMapCompletable {
                Timber.i("supplierThread syncAllTransactions ${it.size}")
                store.get().saveTransactions(it, businessId)
                    .observeOn(ThreadUtils.worker())
            }
            .observeOn(ThreadUtils.worker())
    }

    override fun syncSupplierEnabledCustomerIds(businessId: String): Completable {
        return server.get().getSCEnabledCustomerIds(businessId)
            .doOnEvent { t1, t2 -> Timber.i("getSupplierEnabledCustomerIds serverIds=$t1") }
            .flatMapCompletable {
                store.get().saveSupplierEnabledCustomerIds(it.accountIdsListWithFeatureEnabled, businessId)
                    .andThen(
                        store.get()
                            .saveRestrictTxnEnabledCustomers(it.accountIdsListWithRestrictTxnEnabled, businessId)
                    )
                    .andThen(
                        store.get()
                            .saveAccountIdsListWithAddTransactionRestricted(
                                it.accountIdsListWithAddTransactionRestricted,
                                businessId
                            )
                    )
            }
            .doOnComplete { Timber.i("getSupplierEnabledCustomerIds stored") }
            .observeOn(ThreadUtils.worker())
    }

    // Note : refer 'syncDirtyTransaction(transactionId: String)' for comments
    // Worker
    class SyncTransactionWorker constructor(
        context: Context,
        private val params: WorkerParameters,
        private val store: Lazy<SupplierLocalSource>,
        private val server: Lazy<SupplierRemoteSource>,
    ) : BaseRxWorker(context, params) {

        override fun doRxWork(): Completable {
            Timber.i("addTransaction 6 scheduleSyncTransaction")
            val txnId = inputData.getString(TXN_ID)!!
            val businessId = inputData.getString(BUSINESS_ID)!!
            return syncTransaction(txnId, businessId)
        }

        class Factory @Inject constructor(
            private val store: Lazy<SupplierLocalSource>,
            private val server: Lazy<SupplierRemoteSource>,
        ) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return SyncTransactionWorker(context, params, store, server)
            }
        }

        private fun syncTransaction(txnId: String, businessId: String): Completable {
            Timber.i("addTransaction 7 scheduleSyncTransaction")
            Timber.i("Transaction id = $txnId")

            return store.get().getTransaction(txnId, businessId).firstOrError()
                .flatMapCompletable { localTxn ->

                    val deleted = localTxn.deleted
                    val unsynced =
                        localTxn.lastSyncTime == null || localTxn.lastSyncTime.millis == 0L // was uncysned when it was added

                    if (deleted) {
                        if (unsynced) {

                            Timber.i("unsyncedTransaction 1 id=${localTxn.id} amount=${localTxn.amount}")

                            server.get().addTransaction(localTxn, businessId)
                                .flatMapCompletable { serverTxn ->
                                    server.get().deleteTransaction(serverTxn.id, businessId)
                                        // now local transaction was added and deleted from server one after another , now we only have to replace local transaction id
                                        // with server transaction id and change syncing =true
                                        .andThen(
                                            store.get().saveTransaction(
                                                localTxn.copy(
                                                    id = serverTxn.id,
                                                    syncing = true
                                                ),
                                                businessId
                                            )
                                        )
                                        .andThen(store.get().removeTransaction(txnId))
                                }
                        } else { //  'deleted' and was 'synced' when the transaction was added , so localTxn.id is same  as server transaction id
                            server.get().deleteTransaction(localTxn.id, businessId)
                                .andThen(
                                    store.get().saveTransaction(localTxn.copy(syncing = true), businessId)
                                ) // here 'localTxn' has 'transaction id' from server , because it is already synced
                        }
                    } else { // 'add' transaction
                        server.get().addTransaction(localTxn, businessId)
                            .flatMapCompletable {
                                store.get().saveTransaction(it.copy(syncing = true), businessId)
                            }
                            // since we have saved transaction modifying syncing = true, returned by server
                            // so, removing same transaction from db which is stored  with local id ,
                            .andThen(store.get().removeTransaction(txnId))
                    }
                }
                .subscribeOn(ThreadUtils.database())
        }

        companion object {
            const val TXN_ID = "txn_id"
            const val BUSINESS_ID = "business_id"
        }
    }

    /********** Everything **********/
    override fun syncEverything(businessId: String): Completable {

        Timber.i("<<<SupplierTabTime syncEverything initiated")

        return Completable.fromAction {

            val workName = WORKER_TAG_SYNC_EVERYTHING

            val workRequest = OneTimeWorkRequestBuilder<SyncEverythingWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    workDataOf(
                        SyncEverythingWorker.BUSINESS_ID to businessId
                    )
                )
                .addTag(WORKER_TAG_BASE)
                .addTag(WORKER_TAG_SYNC_EVERYTHING)
                .addTag(workName)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()
                .enableWorkerLogging()

            workManager.get()
                .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
            Timber.i("<<<SupplierTabTime worker scheduled")
        }
            .subscribeOn(ThreadUtils.database())
    }

    class SyncEverythingWorker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncer: Lazy<ISyncer>,
    ) : BaseRxWorker(context, params) {
        companion object {
            const val BUSINESS_ID = "business_id"
        }

        override fun doRxWork(): Completable {
            return syncEverything()
        }

        private fun syncEverything(): Completable {
            val businessId = inputData.getString(BUSINESS_ID)!!
            val startTime = currentDateTime()
            return syncer.get().syncAllDirtyTransactions(businessId)
                .doOnComplete { Timber.i("<<<SupplierTabTime syncAllDirtyTransactions()") }
                .andThen(syncer.get().syncAllSuppliers(businessId))
                .doOnComplete { Timber.i("<<<SupplierTabTime syncAllSuppliers()") }
                .andThen(syncer.get().getLastSyncEverythingTime().first(false to null))
                .doOnSuccess { Timber.i("<<<SupplierTabTime getLastSyncEverythingTime()") }
                .flatMapCompletable {
                    if (it.first) {
                        return@flatMapCompletable syncer.get().syncAllTransactions(it.second, businessId)
                            .doOnComplete { Timber.i("<<<SupplierTabTime syncAllTransactions(it.second)") }
                            .doOnError { throwable: Throwable? ->
                                throwable?.let {
                                    RecordException.recordException(it)
                                }
                                Timber.i("tt<<<SupplierTabTime 1 $throwable")
                            }
                    } else {
                        return@flatMapCompletable syncer.get().syncAllTransactions(businessId = businessId)
                            .doOnComplete { Timber.i("<<<SupplierTabTime syncAllTransactions(1) completed") }
                            .doOnError { throwable: Throwable? ->
                                throwable?.let {
                                    RecordException.recordException(it)
                                }
                                Timber.i("tt<<<SupplierTabTime 2 $throwable")
                            }
                    }
                }
                .andThen(
                    syncer.get().syncSupplierEnabledCustomerIds(businessId)
                ) // Ignoring Error. syncSupplierEnabledCustomerIds is not needed for core functionality of the App.
                .doOnComplete { Timber.i("<<<SupplierTabTime syncAllTransactions()") }
                .andThen(syncer.get().setLastSyncEverythingTime(startTime, businessId))
                .doOnComplete { Timber.i("<<<SupplierTabTime setLastSyncEverythingTime()") }
                .doOnError { throwable: Throwable? ->
                    throwable?.let {
                        RecordException.recordException(it)
                    }
                    Timber.i("tt<<<SupplierTabTime $throwable")
                }
        }

        class Factory @Inject constructor(
            private val syncer: Lazy<ISyncer>,
        ) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return SyncEverythingWorker(context, params, syncer)
            }
        }
    }

    class SyncSupplierEnabledCustomerIdsWorker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncer: Lazy<ISyncer>,
    ) : BaseRxWorker(context, params) {
        companion object {
            const val BUSINESS_ID = "business_id"
        }

        override fun doRxWork(): Completable {
            Timber.i("<<<SupplierTabTime worker created")
            val businessId = inputData.getString(BUSINESS_ID)!!
            return syncer.get().syncSupplierEnabledCustomerIds(businessId)
        }

        class Factory @Inject constructor(private val syncer: Lazy<ISyncer>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return SyncSupplierEnabledCustomerIdsWorker(context, params, syncer)
            }
        }
    }

    // use store
    override fun getLastSyncEverythingTime(): Observable<Pair<Boolean, DateTime?>> {
        Timber.i(" exicuted getLastSyncEverythingTime")
        return store.get().getLastSyncEverythingTime()
            .doOnNext { Timber.i(">>>>>> exicuted getLastSyncEverythingTime 2 $it") }
    }

    // update in store
    override fun setLastSyncEverythingTime(time: DateTime, businessId: String): Completable {
        Timber.i(">>>> exicuted setLastSyncEverythingTime")
        return store.get().setLastSyncEverythingTime(time, businessId)
            .observeOn(ThreadUtils.worker())
    }

    override fun syncNotificationReminder(businessId: String): Completable {
        return store.get().getStartTimeNotificationReminder(businessId).flatMapCompletable { startTime ->
            return@flatMapCompletable server.get().getNotificationReminder(startTime.toLong(), businessId)
                .subscribeOn(ThreadUtils.api())
                .observeOn(ThreadUtils.database())
                .map {
                    Timber.d("syncNotificationReminder get data ${it.notificationReminders.size}")
                    supplierEventTracker.get()
                        .trackInAppReminderReceived(it.notificationReminders.size, startTime)
                    it.notificationReminders.toEntityMapper(businessId)
                }.flatMapCompletable {
                    Timber.d("syncNotificationReminder  data saved  $it")
                    store.get().saveNotificationReminder(it)
                }
        }
    }

    override fun syncUpdateNotificationReminder(businessId: String): Completable {
        return store.get().getProcessedNotificationReminder(businessId).flatMapCompletable { reminders ->
            if (reminders.isNullOrEmpty()) {
                Timber.d("syncUpdateNotificationReminder : no data to send to server")
                return@flatMapCompletable Completable.complete()
            } else {
                val updateNotificationReminder = ApiMessages.UpdateNotificationReminder(
                    reminders.map { ApiMessages.NotificationReminderAction(it.id, it.status) }
                )
                return@flatMapCompletable server.get().updateNotificationReminder(updateNotificationReminder, businessId)
                    .flatMapCompletable { notificationReminderActions ->

                        val successfulActions =
                            reminders.filter { localReminder ->
                                notificationReminderActions
                                    .any { it.id == localReminder.id && it.success }
                            }
                        if (!successfulActions.isNullOrEmpty()) {
                            Timber.d("syncUpdateNotificationReminder :  data sent successfully")
                            return@flatMapCompletable store.get().deleteProcessedNotificationReminder(successfulActions)
                        } else {
                            Timber.d("syncUpdateNotificationReminder :  data send unsuccessful")
                            Completable.complete()
                        }
                    }
            }
        }
    }

    class SyncNotificationReminderWorker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncer: Lazy<ISyncer>,
    ) : BaseCoroutineWorker(context, params) {

        companion object {
            const val BUSINESS_ID = "business_id"
        }

        class Factory @Inject constructor(private val syncer: Lazy<ISyncer>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return SyncNotificationReminderWorker(context, params, syncer)
            }
        }

        override suspend fun doActualWork() {
            val businessId = inputData.getString(BUSINESS_ID)!!
            syncer.get().syncUpdateNotificationReminder(businessId).andThen(
                syncer.get().syncNotificationReminder(businessId)
            ).await()
        }
    }
}
