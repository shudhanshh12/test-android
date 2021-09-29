package `in`.okcredit.merchant.core.sync

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.Command
import `in`.okcredit.merchant.core.Command.CommandType.CREATE_CUSTOMER_DIRTY
import `in`.okcredit.merchant.core.Command.CommandType.CREATE_CUSTOMER_IMMUTABLE
import `in`.okcredit.merchant.core.CoreSdkImpl
import `in`.okcredit.merchant.core.analytics.CoreTracker
import `in`.okcredit.merchant.core.common.CoreUtils
import `in`.okcredit.merchant.core.model.Customer
import `in`.okcredit.merchant.core.server.CoreRemoteSource
import `in`.okcredit.merchant.core.server.internal.CoreApiMessages
import `in`.okcredit.merchant.core.server.toApiCustomerCommand
import `in`.okcredit.merchant.core.server.toCustomer
import `in`.okcredit.merchant.core.server.toStatus
import `in`.okcredit.merchant.core.store.CoreLocalSource
import android.content.Context
import androidx.work.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseCoroutineWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class SyncCustomerCommands @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
    private val localSource: Lazy<CoreLocalSource>,
    private val remoteSource: Lazy<CoreRemoteSource>,
    private val tracker: Lazy<CoreTracker>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val dispatcherProvider: Lazy<DispatcherProvider>,
    private val syncTransactionsCommands: Lazy<SyncTransactionsCommands>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    companion object {
        @NonNls
        const val TAG = "${CoreSdkImpl.TAG}/SyncCustomerCommands"

        @NonNls
        const val WORKER_TAG_BASE = "core"

        @NonNls
        const val WORKER_TAG_SYNC = "core/syncCustomerCommands"

        @NonNls
        const val WORKER_INPUT_INCLUDE_IMMUTABLE_CUSTOMERS = "sync_customers_include_immutable_customers"

        @NonNls
        const val MAX_COUNT_PER_REQUEST_FOR_SYNC_CUSTOMER_KEY = "max_count_per_request_for_sync_customer"

        @NonNls
        const val MAX_RETRY_COUNT_OF_SYNC_CUSTOMER_KEY = "max_retry_count_of_sync_customer_key"

        @NonNls
        const val CONFLICT_CUSTOMER_ID = "customer_sync_conflict_customer_id"

        @NonNls
        const val CONFLICT_CUSTOMER_EXITS = "customer_sync_conflict_customer_exists"

        private const val BUSINESS_ID = "business_id"

        val lock = Mutex()
    }

    suspend fun execute(customerId: String, businessId: String? = null) = withContext(dispatcherProvider.get().io()) {
        val flowId = CoreUtils.generateRandomId()

        val maxRetryCount = firebaseRemoteConfig.get().getLong(MAX_RETRY_COUNT_OF_SYNC_CUSTOMER_KEY).toInt()

        lock.withLock {
            // Currently only supports dirty customers
            val mBusinessId = getActiveBusinessId.get().thisOrActiveBusinessId(businessId).await()
            syncDirtyCustomer(flowId, maxRetryCount, customerId, mBusinessId)
        }
    }

    suspend fun execute(includeImmutableCustomers: Boolean, businessId: String? = null) =
        withContext(dispatcherProvider.get().io()) {
            val flowId = CoreUtils.generateRandomId()

            val maxCountPerRequest =
                firebaseRemoteConfig.get().getLong(MAX_COUNT_PER_REQUEST_FOR_SYNC_CUSTOMER_KEY).toInt()
            val maxRetryCount = firebaseRemoteConfig.get().getLong(MAX_RETRY_COUNT_OF_SYNC_CUSTOMER_KEY).toInt()

            // Lock is necessary to prevent multiple entities calling this use case simultaneously breaking data integrity
            lock.withLock {
                // Currently there is no way for an immutable customer to become clean again, but they are synced none the
                // less to prevent id conflicts, need to figure out a better way to manage this

                val mBusinessId = getActiveBusinessId.get().thisOrActiveBusinessId(businessId).await()
                if (includeImmutableCustomers) {
                    syncImmutableCustomers(flowId, maxCountPerRequest, mBusinessId)
                }

                // Dirty customers are synced later so those accounts marked as immutable during processing of dirty
                // customers are not included in the processing of immutable customers

                syncDirtyCustomers(flowId, maxCountPerRequest, maxRetryCount, mBusinessId)
                syncTransactionsCommands.get().schedule(mBusinessId).await()
            }
        }

    private suspend fun syncDirtyCustomers(
        flowId: String,
        maxCountPerRequest: Int,
        maxRetryCount: Int,
        businessId: String,
    ) {
        var retryAttemptCount = 0

        while (true) {
            val dirtyCustomersCommands = localSource.get().listDirtyCustomerCommands(maxCountPerRequest, businessId)
                .onEach {
                    tracker.get().trackSyncCustomerCommands(
                        "1.Read Command from DB",
                        it.id,
                        it.customerId,
                        flowId,
                        retryAttemptCount,
                        comment = "CommandType : ${it.commandType.code}"
                    )
                }
                .takeIf { it.isNotEmpty() }
                ?: return

            val apiCommandList = dirtyCustomersCommands.mapNotNull {
                // assuming customer always found todo add record exception
                val customer = localSource.get().getCustomerSus(it.customerId)
                runCatching { it.toApiCustomerCommand(customer) }.getOrNull()
            }.onEach {
                tracker.get().trackSyncCustomerCommands(
                    "2.Map to ApiCustomer",
                    it.id,
                    it.customer.id,
                    flowId,
                    retryAttemptCount,
                )
            }

            val request = CoreApiMessages.PushCustomersCommandsRequest(apiCommandList)
            val response = runCatching {
                remoteSource.get().pushCustomerCommands(
                    businessId = businessId,
                    request = request
                )
            }

            response.exceptionOrNull()?.takeIf { it is CancellationException }?.also { throw it }

            val responses = response.getOrNull()?.operation_responses
                ?.onEach {
                    val resolution = it.error?.description ?: "success"
                    tracker.get().trackSyncCustomerCommands(
                        "3.Sync Customer Resolution",
                        it.id,
                        it.customer?.id ?: "",
                        flowId,
                        retryAttemptCount,
                        comment = resolution
                    )
                }
                ?: run {
                    tracker.get().trackSyncCustomerCommandsError(
                        "3.Sync Customer Api Failed",
                        flowId,
                        response.exceptionOrNull()?.message
                    )
                    emptyList()
                }

            val successCommandIdsList: List<String> = responses
                .filter { it.status.toStatus() == CoreApiMessages.Status.SUCCESS }
                .map { it.id }

            when {
                successCommandIdsList.isNotEmpty() -> retryAttemptCount = 0
                retryAttemptCount < maxRetryCount -> retryAttemptCount++
                else -> {
                    tracker.get().trackSyncCustomerCommandsError(
                        "4.Sync Customer Retry Exceeded",
                        flowId
                    )
                    return
                }
            }

            // Cleanup
            deleteSuccessfulCustomerCommands(successCommandIdsList)
            // Success Processing
            saveSuccessfullySyncedCustomers(flowId, retryAttemptCount, responses, businessId)
            // Conflict Processing
            handleConflictDueToCustomerId(flowId, retryAttemptCount, responses, dirtyCustomersCommands)
            handleConflictDueToCustomerExists(flowId, retryAttemptCount, responses, dirtyCustomersCommands)
        }
    }

    private suspend fun syncImmutableCustomers(flowId: String, maxCountPerRequest: Int, businessId: String) {
        var startOffset = 0

        while (true) {
            val immutableCustomersCommands = localSource.get().listImmutableCustomerCommands(
                offset = startOffset,
                pageSize = maxCountPerRequest,
                businessId = businessId
            ).onEach {
                tracker.get().trackSyncCustomerCommands(
                    "1.Read Command from DB",
                    it.id,
                    it.customerId,
                    flowId,
                    0,
                    comment = "CommandType : ${it.commandType.code}"
                )
            }
                .takeIf { it.isNotEmpty() }
                ?: return

            val apiCommandList = immutableCustomersCommands.mapNotNull {
                // assuming customer always found todo add record exception
                val customer = localSource.get().getCustomerSus(it.customerId)
                runCatching { it.toApiCustomerCommand(customer) }.getOrNull()
            }.onEach {
                tracker.get().trackSyncCustomerCommands(
                    "2.Map to ApiCustomer",
                    it.id,
                    it.customer.id,
                    flowId,
                    0,
                )
            }

            val request = CoreApiMessages.PushCustomersCommandsRequest(apiCommandList)
            val response = runCatching {
                remoteSource.get().pushCustomerCommands(
                    businessId = businessId,
                    request = request
                )
            }

            response.exceptionOrNull()?.takeIf { it is CancellationException }?.also { throw it }

            val responses = response.getOrNull()?.operation_responses?.onEach {
                val resolution = it.error?.description ?: "success"
                tracker.get().trackSyncCustomerCommands(
                    "3.Sync Customer Resolution",
                    it.id,
                    it.customer?.id ?: "",
                    flowId,
                    0,
                    comment = resolution
                )
            }
                ?: run {
                    tracker.get().trackSyncCustomerCommandsError(
                        "3.Sync Customer Api Failed",
                        flowId,
                        response.exceptionOrNull()?.message
                    )
                    emptyList()
                }

            val successCommandIdsList: List<String> = responses
                .filter { it.status.toStatus() == CoreApiMessages.Status.SUCCESS }
                .map { it.id }

            // Cleanup
            deleteSuccessfulCustomerCommands(successCommandIdsList)
            // Success Processing
            saveSuccessfullySyncedCustomers(flowId, 0, responses, businessId)
            // Conflict Processing
            handleConflictDueToCustomerId(flowId, 0, responses, immutableCustomersCommands)
            // handleConflictDueToCustomerExists is redundant as their status is already Immutable

            // Skip over failed AddCustomerImmutable commands
            startOffset += immutableCustomersCommands.size - successCommandIdsList.size
        }
    }

    private suspend fun syncDirtyCustomer(flowId: String, maxRetryCount: Int, customerId: String, businessId: String) {
        var retryAttemptCount = 0

        while (true) {
            val dirtyCustomersCommands = localSource.get().getCommandForCustomerId(customerId, businessId)
                ?.takeIf { it.commandType == CREATE_CUSTOMER_DIRTY }
                ?.also {
                    tracker.get().trackSyncCustomerCommands(
                        "1.Read Command from DB",
                        it.id,
                        it.customerId,
                        flowId,
                        retryAttemptCount,
                        comment = "CommandType : ${it.commandType.code}"
                    )
                }
                ?.run { listOf(this) }
                ?: return

            val apiCommandList = dirtyCustomersCommands.mapNotNull {
                // assuming customer always found todo add record exception
                val customer = localSource.get().getCustomerSus(it.customerId)
                runCatching { it.toApiCustomerCommand(customer) }.getOrNull()
            }.onEach {
                tracker.get().trackSyncCustomerCommands(
                    "2.Map to ApiCustomer",
                    it.id,
                    it.customer.id,
                    flowId,
                    retryAttemptCount,
                )
            }

            val request = CoreApiMessages.PushCustomersCommandsRequest(apiCommandList)
            val response = runCatching {
                remoteSource.get().pushCustomerCommands(
                    businessId = businessId,
                    request = request
                )
            }

            response.exceptionOrNull()?.takeIf { it is CancellationException }?.also { throw it }

            val responses = response.getOrNull()?.operation_responses?.onEach {
                val resolution = it.error?.description ?: "success"
                tracker.get().trackSyncCustomerCommands(
                    "3.Sync Customer Resolution",
                    it.id,
                    it.customer?.id ?: "",
                    flowId,
                    retryAttemptCount,
                    comment = resolution
                )
            }
                ?: run {
                    tracker.get().trackSyncCustomerCommandsError(
                        "3.Sync Customer Api Failed",
                        flowId,
                        response.exceptionOrNull()?.message
                    )
                    emptyList()
                }

            val successCommandIdsList: List<String> = responses
                .filter { it.status.toStatus() == CoreApiMessages.Status.SUCCESS }
                .map { it.id }

            when {
                successCommandIdsList.isNotEmpty() -> retryAttemptCount = 0
                retryAttemptCount < maxRetryCount -> retryAttemptCount++
                else -> {
                    tracker.get().trackSyncCustomerCommandsError(
                        "4.Sync Customer Retry Exceeded",
                        flowId
                    )
                    return
                }
            }

            // Cleanup
            deleteSuccessfulCustomerCommands(successCommandIdsList)
            // Success Processing
            saveSuccessfullySyncedCustomers(flowId, retryAttemptCount, responses, businessId)
            // Conflict Processing
            handleConflictDueToCustomerId(flowId, retryAttemptCount, responses, dirtyCustomersCommands)
            handleConflictDueToCustomerExists(flowId, retryAttemptCount, responses, dirtyCustomersCommands)
        }
    }

    private suspend fun deleteSuccessfulCustomerCommands(successCommandIdsList: List<String>) {
        localSource.get().deleteCommands(successCommandIdsList).await()
    }

    private suspend fun saveSuccessfullySyncedCustomers(
        flowId: String,
        retryAttemptCount: Int,
        responses: List<CoreApiMessages.CustomerOperationResponse>,
        businessId: String
    ) {
        responses
            .filter { it.status.toStatus() == CoreApiMessages.Status.SUCCESS }
            .also { syncedCustomerOps ->
                val syncedCustomers = syncedCustomerOps.mapNotNull { it.customer?.toCustomer() }
                localSource.get().putCustomerSus(syncedCustomers, businessId)
            }.onEach {
                tracker.get().trackSyncCustomerCommands(
                    "Saved to DB",
                    it.id,
                    it.customer?.id ?: "",
                    flowId,
                    retryAttemptCount,
                )
            }
    }

    private suspend fun handleConflictDueToCustomerId(
        flowId: String,
        retryAttemptCount: Int,
        responses: List<CoreApiMessages.CustomerOperationResponse>,
        requestCommands: List<Command>,
    ) {
        val errorCommands = responses
            .filter { isConflictCustomerId(it) }
            .map { it.id }
            .toHashSet()

        requestCommands
            .filter { errorCommands.contains(it.id) }
            .forEach {
                val newId = CoreUtils.generateRandomId() // Possibility of conflict ?

                // Update the customer id for customer and pending transactions
                localSource.get().replaceCustomerId(it.customerId, newId)
                tracker.get().trackSyncCustomerCommands(
                    "Id replaced due to conflict",
                    it.id,
                    it.customerId,
                    flowId,
                    retryAttemptCount,
                    comment = newId // Do not format this any other way
                )
            }
    }

    private suspend fun handleConflictDueToCustomerExists(
        flowId: String,
        retryAttemptCount: Int,
        responses: List<CoreApiMessages.CustomerOperationResponse>,
        requestCommands: List<Command>,
    ) {
        val errorCommands = responses
            .filter { isConflictCustomerExists(it) }
            .map { it.id }
            .toHashSet()
        val conflictingCommands = requestCommands
            .filter { errorCommands.contains(it.id) }

        conflictingCommands.forEach {
            val transactionCount = localSource.get().getTransactionCountForCustomer(it.customerId)
            if (transactionCount == 0) {
                // Delete local customer directly for those that don't have transactions attached
                localSource.get().deleteImmutableAccount(it.customerId)
                tracker.get().trackSyncCustomerCommands(
                    "Account AutoDeleted: Immutable without transactions",
                    it.id,
                    it.customerId,
                    flowId,
                    retryAttemptCount,
                )
            } else {
                // Mark customer profile as immutable and preserve transactions
                localSource.get().updateCustomerSyncStatus(it.customerId, Customer.CustomerSyncStatus.IMMUTABLE)
                localSource.get().updateCustomerCommandType(it.id, CREATE_CUSTOMER_IMMUTABLE)
                tracker.get().trackSyncCustomerCommands(
                    "Account marked Immutable",
                    it.id,
                    it.customerId,
                    flowId,
                    retryAttemptCount,
                )
            }
        }
    }

    private fun isConflictCustomerId(it: CoreApiMessages.CustomerOperationResponse): Boolean {
        return it.status.toStatus() == CoreApiMessages.Status.FAILURE &&
            it.error?.code == CoreApiMessages.ErrorCodes.CONFLICT.value &&
            it.error.description == CONFLICT_CUSTOMER_ID
    }

    private fun isConflictCustomerExists(it: CoreApiMessages.CustomerOperationResponse): Boolean {
        return it.status.toStatus() == CoreApiMessages.Status.FAILURE &&
            it.error?.code == CoreApiMessages.ErrorCodes.CONFLICT.value &&
            it.error.description == CONFLICT_CUSTOMER_EXITS
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
                    .setInputData(
                        workDataOf(
                            WORKER_INPUT_INCLUDE_IMMUTABLE_CUSTOMERS to false,
                            BUSINESS_ID to businessId
                        )
                    )
                    .setConstraints(constraints)
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
        private val includeImmutableCustomers: Boolean,
        private val syncTransactionsCommands: Lazy<SyncCustomerCommands>,
    ) : BaseCoroutineWorker(context, params) {

        class Factory @Inject constructor(
            private val syncTransactionsCommands: Lazy<SyncCustomerCommands>,
        ) : ChildWorkerFactory {

            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                val includeImmutables = params.inputData.getBoolean(WORKER_INPUT_INCLUDE_IMMUTABLE_CUSTOMERS, false)
                return Worker(context, params, includeImmutables, syncTransactionsCommands)
            }
        }

        override suspend fun doActualWork() {
            val businessId = inputData.getString(BUSINESS_ID)
            syncTransactionsCommands.get().execute(includeImmutableCustomers, businessId)
        }
    }
}
