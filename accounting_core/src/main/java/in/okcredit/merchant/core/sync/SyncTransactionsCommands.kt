package `in`.okcredit.merchant.core.sync

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.Command
import `in`.okcredit.merchant.core.CoreSdkImpl
import `in`.okcredit.merchant.core.analytics.CoreTracker
import `in`.okcredit.merchant.core.analytics.CoreTracker.DebugType.ERROR_CODE_CONFLICT_UNHANDLED_DESCRIPTION
import `in`.okcredit.merchant.core.common.CoreUtils
import `in`.okcredit.merchant.core.server.CoreRemoteSource
import `in`.okcredit.merchant.core.server.internal.CoreApiMessages
import `in`.okcredit.merchant.core.server.toApiTransactionCommandList
import `in`.okcredit.merchant.core.server.toStatus
import `in`.okcredit.merchant.core.store.CoreLocalSource
import android.content.Context
import androidx.work.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.utils.getStringStackTrace
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import `in`.okcredit.merchant.core.store.database.Command as DbCommand
import `in`.okcredit.merchant.core.store.database.Transaction as DbTransaction

@Reusable
class SyncTransactionsCommands @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
    private val localSource: Lazy<CoreLocalSource>,
    private val remoteSource: Lazy<CoreRemoteSource>,
    private val tracker: Lazy<CoreTracker>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    companion object {
        @NonNls
        const val TAG = "${CoreSdkImpl.TAG}/SyncTransactionCommands"

        @NonNls
        const val WORKER_TAG_BASE = "core"

        @NonNls
        const val WORKER_TAG_SYNC = "core/syncTransactionCommands"

        const val MAX_COUNT_PER_REQUEST_FOR_SYNC_TXN_KEY = "max_count_per_request_for_sync_txn"
        const val MAX_RETRY_COUNT_OF_SYNC_TRANSACTION_KEY = "max_retry_count_of_sync_transaction"

        @NonNls
        const val TRANSACTION_ID_EXISTS_ERROR_DESCRIPTION = "transaction_id_conflict"

        @NonNls
        const val IMAGE_ID_EXISTS_ERROR_DESCRIPTION = "txn_image_request_id_exists"

        private const val BUSINESS_ID = "business_id"
    }

    fun execute(businessId: String? = null): Completable {
        val flowId = CoreUtils.generateRandomId()
        var iteration = 0
        var retryAttemptCount = AtomicInteger(0)
        return Single.defer {
            val maxCountPerRequest = firebaseRemoteConfig.get().getLong(MAX_COUNT_PER_REQUEST_FOR_SYNC_TXN_KEY).toInt()
            getActiveBusinessId.get().thisOrActiveBusinessId(businessId)
                .flatMap { _businessId ->
                    localSource.get().listTransactionsCommandsForCleanCustomers(maxCountPerRequest, _businessId)
                        .firstOrError()
                        .doOnSubscribe {
                            iteration++
                        }.flatMap {
                            if (it.isEmpty()) {
                                return@flatMap Single.just(0)
                            }
                            // Timber.v("$TAG[$iteration] Sending ${it.size} commands")
                            val transactionIds = it.map { command -> command.transactionId }
                            val commandTypes = it.map { command -> command.commandType.name }
                            val list: List<CoreApiMessages.ApiTransactionCommand> = it.toApiTransactionCommandList()
                            tracker.get()
                                .trackSyncTransactionCommands(
                                    "Sent_Server_Call",
                                    transactionIds,
                                    commandTypes,
                                    flowId,
                                    iteration,
                                    it.size
                                )
                            return@flatMap syncTransactions(
                                flowId = flowId,
                                iteration = iteration,
                                retryAttemptCount = retryAttemptCount,
                                transactionIds = transactionIds,
                                commandTypes = commandTypes,
                                list = list,
                                commandList = it,
                                businessId = _businessId
                            )
                        }
                }
        }
            .repeat()
            .takeWhile { it != 0 }
            .ignoreElements()
    }

    private fun syncTransactions(
        flowId: String,
        iteration: Int,
        retryAttemptCount: AtomicInteger,
        transactionIds: List<String>,
        commandTypes: List<String>,
        list: List<CoreApiMessages.ApiTransactionCommand>,
        commandList: List<Command>,
        businessId: String,
    ): Single<Int> {
        return remoteSource.get()
            .pushTransactionCommands(CoreApiMessages.PushTransactionsCommandsRequest(list), businessId)
            .doOnError { error ->
                RecordException.recordException(error)
                tracker.get().trackSyncTransactionCommandsError(
                    "Server error",
                    flowId,
                    error?.message,
                    error?.getStringStackTrace(),
                    transactionIds,
                    commandTypes
                )
            }.flatMap { response ->
                val successCommandIdsList: List<String> = response.operation_responses.filter { command ->
                    command.status.toStatus() == CoreApiMessages.Status.SUCCESS
                }.map { item -> item.id }
                tracker.get().trackSyncTransactionCommands(
                    "Completed_Server_Call",
                    transactionIds,
                    commandTypes,
                    flowId,
                    iteration,
                    commandList.size,
                    successCommandIdsList.size
                )
                // Timber.v("$TAG[$iteration] Response: ${successCommandIdsList.size}/${it.size} commands successful")
                if (successCommandIdsList.isEmpty()) {
                    val maxRetryCount =
                        firebaseRemoteConfig.get().getLong(MAX_RETRY_COUNT_OF_SYNC_TRANSACTION_KEY).toInt()
                    if (retryAttemptCount.incrementAndGet() >= maxRetryCount) {
                        Timber.e("$TAG[$iteration] MAX_RETRY_COUNT($maxRetryCount) exceeded")
                        val responseCodes = response.operation_responses
                            .map { operationResponse ->
                                "id: ${operationResponse.id} code: ${operationResponse.error?.code} " +
                                    "desc: ${operationResponse.error?.description}"
                            }
                        tracker.get().trackDebug(
                            CoreTracker.DebugType.SYNC_COMMANDS_MAX_RETRIES_EXCEEDED,
                            "count: ${commandList.size}, codes: $responseCodes"
                        )
                        return@flatMap Single.just(0)
                    }
                } else {
                    retryAttemptCount.set(0)
                }
                return@flatMap deleteSuccessfulCommands(successCommandIdsList)
                    .andThen(markSyncedTransactionsNotDirty(successCommandIdsList, commandList, businessId))
                    .andThen(handleTransactionIdConflictIfPresent(response, commandList))
                    .andThen(handleImageIdConflictIfPresent(response, commandList, businessId))
                    .doOnComplete {
                        trackSyncSuccessful(
                            commandList.filter { command -> successCommandIdsList.contains(command.id) },
                            commandList.size
                        )
                        tracker.get().trackSyncTransactionCommands(
                            "Saved_To_Database",
                            transactionIds,
                            commandTypes, flowId,
                            iteration,
                            commandList.size
                        )
                    }.doOnError { error ->
                        RecordException.recordException(error)
                        tracker.get().trackSyncTransactionCommandsError(
                            "Saving to DB error",
                            flowId,
                            error?.message,
                            error?.getStringStackTrace(),
                            transactionIds,
                            commandTypes
                        )
                    }.andThen(Single.just(commandList.size))
            }.doOnSuccess { size ->
                tracker.get().trackSyncTransactionCommands(
                    "Completed_Sync",
                    transactionIds,
                    commandTypes,
                    flowId,
                    iteration,
                    size
                )
            }
    }

    private fun deleteSuccessfulCommands(successCommandIdsList: List<String>): Completable {
        if (successCommandIdsList.isNotEmpty()) {
            // Timber.v("$TAG Deleting ${successCommandIdsList.size} commands")
            return localSource.get().deleteCommands(successCommandIdsList)
        }
        return Completable.complete()
    }

    private fun markSyncedTransactionsNotDirty(
        successCommandIdsList: List<String>,
        requestCommands: List<Command>,
        businessId: String,
    ): Completable {
        return localSource.get().listTransactionsCommandsForCleanCustomers(businessId)
            .firstOrError()
            .flatMapCompletable {
                val dirtyTransactionIds = it.map { command -> command.transactionId }
                val syncedTransactionIds = requestCommands
                    .filter { command -> successCommandIdsList.contains(command.id) }
                    .map { command -> command.transactionId }
                    .distinct()
                    .filterNot { id -> dirtyTransactionIds.contains(id) }
                if (syncedTransactionIds.isNotEmpty()) {
                    // Timber.v("$TAG Marking ${syncedTransactionIds.size} transactions as not dirty")
                    return@flatMapCompletable localSource.get().markTransactionDirty(syncedTransactionIds, false)
                }
                return@flatMapCompletable Completable.complete()
            }
    }

    private fun trackSyncSuccessful(syncedCommands: List<Command>, requestSize: Int) =
        syncedCommands.forEach {
            tracker.get().trackTransactionSyncSuccessful(it.transactionId, it.commandType.name, requestSize)
        }

    private fun handleTransactionIdConflictIfPresent(
        responseTransactions: CoreApiMessages.PushTransactionsCommandsResponse,
        requestCommands: List<Command>,
    ): Completable {
        val errorCommandIds: List<String> = responseTransactions.operation_responses
            .filter { isFailed(it) && isTransactionIdConflict(it) }
            .map { it.id }
        val conflictTransactionIds = requestCommands
            .filter { errorCommandIds.contains(it.id) }
            .map { it.transactionId }
            .distinct()
        if (conflictTransactionIds.isNotEmpty()) {
            Timber.w("$TAG Conflict in ${conflictTransactionIds.size} transaction ids, replacing.")
            return Observable.fromIterable(conflictTransactionIds)
                .flatMapCompletable { oldId ->
                    val newId = CoreUtils.generateRandomId()
                    trackTransactionIdConflict(oldId, newId)
                    localSource.get().replaceTransactionId(oldId, newId)
                }
        }
        return Completable.complete()
    }

    private fun trackTransactionIdConflict(oldId: String, newId: String) {
        val message = "$TAG Transaction Id: replaced [$oldId] with [$newId]"
        Timber.w(message)
        tracker.get().trackDebug(CoreTracker.DebugType.SERVER_CONFLICT, message)
    }

    private fun handleImageIdConflictIfPresent(
        responseTransactions: CoreApiMessages.PushTransactionsCommandsResponse,
        requestCommands: List<Command>,
        businessId: String,
    ): Completable {
        val errorCommandIds: List<String> = responseTransactions.operation_responses
            .filter { isFailed(it) && isImageIdConflict(it) }
            .map { it.id }
        val conflictImageIds = mutableListOf<String>()
        requestCommands.forEach { command ->
            if (errorCommandIds.contains(command.id)) {
                when (command) {
                    is Command.CreateTransaction -> command.transactionImages?.forEach { image ->
                        conflictImageIds.add(image.id)
                    }
                    is Command.CreateTransactionImage -> conflictImageIds.add(command.imageId)
                    else -> RecordException.recordException(IllegalArgumentException("Image Id Conflict for $command"))
                }
            }
        }
        // return early if there is no conflict
        if (conflictImageIds.isEmpty()) {
            return Completable.complete()
        }

        Timber.w("$TAG Conflict in ${conflictImageIds.size} image ids, replacing.")
        return Observable.fromIterable(conflictImageIds)
            .flatMapCompletable { oldId ->
                val newId = CoreUtils.generateRandomId()
                trackImageIdConflict(oldId, newId)
                Single.zip(
                    localSource.get().getDbTransactionsWithImageId(oldId, businessId),
                    localSource.get().getDbCommandsWithImageId(oldId, businessId),
                    BiFunction<List<DbTransaction>, List<DbCommand>, Pair<List<DbTransaction>, List<DbCommand>>>
                    { transactionList, commandList ->
                        return@BiFunction transactionList to commandList
                    }
                )
                    .flatMapCompletable { transactionAndCommand ->
                        val transactionIdToImagesList = arrayListOf<Pair<String, String>>()
                        val commandIdToValueList = arrayListOf<Pair<Int, String>>()
                        transactionAndCommand.first.forEach { transaction ->
                            transaction.images?.let {
                                val imagesWithNewId = transaction.images.replace(oldId, newId)
                                transactionIdToImagesList.add(transaction.id to imagesWithNewId)
                            }
                        }
                        transactionAndCommand.second.forEach { command ->
                            val valueWithNewId = command.value.replace(oldId, newId)
                            commandIdToValueList.add(command.id to valueWithNewId)
                        }
                        localSource.get().updateTransactionImagesAndCommandValues(
                            transactionIdToImagesList, commandIdToValueList
                        )
                    }
            }
    }

    private fun trackImageIdConflict(oldId: String, newId: String) {
        val message = "$TAG Image Id: replaced [$oldId] with [$newId]"
        Timber.w(message)
        tracker.get().trackDebug(CoreTracker.DebugType.SERVER_CONFLICT, message)
    }

    private fun isFailed(it: CoreApiMessages.OperationResponseForTransactions): Boolean {
        return (it.status.toStatus() == CoreApiMessages.Status.FAILURE)
    }

    private fun isTransactionIdConflict(it: CoreApiMessages.OperationResponseForTransactions): Boolean {
        if (it.error != null && isConflict(it)) {
            return it.error.description == TRANSACTION_ID_EXISTS_ERROR_DESCRIPTION
        }
        return false
    }

    private fun isImageIdConflict(it: CoreApiMessages.OperationResponseForTransactions): Boolean {
        if (it.error != null && isConflict(it)) {
            return it.error.description == IMAGE_ID_EXISTS_ERROR_DESCRIPTION
        }
        return false
    }

    private fun isConflict(it: CoreApiMessages.OperationResponseForTransactions): Boolean {
        if (it.error?.code == CoreApiMessages.ErrorCodes.CONFLICT.value) {
            when (it.error.description) {
                TRANSACTION_ID_EXISTS_ERROR_DESCRIPTION -> return true
                IMAGE_ID_EXISTS_ERROR_DESCRIPTION -> return true
                else -> recordUnhandledDescriptionException(it)
            }
        }
        return false
    }

    private fun recordUnhandledDescriptionException(it: CoreApiMessages.OperationResponseForTransactions) {
        tracker.get().trackDebug(ERROR_CODE_CONFLICT_UNHANDLED_DESCRIPTION, it.toString())
        RecordException
            .recordException(IllegalArgumentException("Unsupported error description in response: $it"))
    }

    fun schedule(businessId: String): Completable {
        return Completable
            .fromAction {
                val workName = WORKER_TAG_SYNC
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val workRequest = OneTimeWorkRequest.Builder(Worker::class.java)
                    .addTag(WORKER_TAG_BASE)
                    .addTag(workName)
                    .setConstraints(constraints)
                    .setInputData(
                        workDataOf(
                            BUSINESS_ID to businessId
                        )
                    )
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        30, TimeUnit.SECONDS
                    )
                    .build()
                    .enableWorkerLogging()

                workManager.get()
                    .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.KEEP, workRequest)
            }
            .subscribeOn(ThreadUtils.newThread())
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncTransactionsCommands: Lazy<SyncTransactionsCommands>,
    ) : BaseRxWorker(context, params) {

        override fun doRxWork(): Completable {
            val businessId = inputData.getString(BUSINESS_ID)
            return syncTransactionsCommands.get().execute(businessId)
        }

        class Factory @Inject constructor(
            private val syncTransactionsCommands: Lazy<SyncTransactionsCommands>,
        ) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, syncTransactionsCommands)
            }
        }
    }
}
