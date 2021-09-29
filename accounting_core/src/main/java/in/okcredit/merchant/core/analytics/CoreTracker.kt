package `in`.okcredit.merchant.core.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import dagger.Lazy
import tech.okcredit.android.base.utils.getStringStackTrace
import javax.inject.Inject

class CoreTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    object Key {
        const val STEP = "step"
        const val ID = "id"
        const val SOURCE = "source"
        const val COMMAND_COUNT = "Command Count"
        const val IS_FILE = "Is File"
        const val REASON = "Reason"
        const val STACKTRACE = "StackTrace"
        const val SCREEN = "Screen"
        const val RELATION = "Relation"
        const val TRANSACTION_COUNT = "Txn Count"
        const val ITERATION = "Iteration"
        const val SUCCESS_RESPONSE_COUNT = "Success response command count"
        const val TYPE = "Type"
        const val META = "Meta"
        const val FEATURE_FLAG_STATUS = "Feature flag status"
        const val IN_APP_FEATURE_STATUS = "In-app feature status"
        const val TOGGLE_SUCCESSFUL = "Toggle successful"
        const val FEATURE = "Feature"
        const val TRANSACTION_ID = "Transaction_id"
        const val TRANSACTION_IDS = "Transaction_ids"
        const val CUSTOMER_ID = "Customer_id"
        const val CUSTOMER_IDS = "Customer_ids"
        const val COMMAND_TYPES = "Command_types"
        const val COMMAND_TYPE = "Command_type"
        const val COMMENT = "Comment"
        const val OPERATION_ID = "Operation Id"
        const val REQUEST_SIZE = "Request Size"
        const val IS_CORE_SYNC = "isCoreSync"
        const val TRANSACTION_COUNT_DELTA = "Txn Count Delta"
    }

    object Event {
        const val SYNC_TRANSACTION_COMMANDS = "CoreSdk Sync Transaction Commands"
        const val SYNC_TRANSACTION_COMMANDS_ERROR = "CoreSdk Sync Transaction Commands: Error"
        const val SYNC_TRANSACTIONS = "CoreSdk Sync Transactions"
        const val SYNC_TRANSACTIONS_ERROR = "CoreSdk Sync Transactions: Error"
        const val SYNC_TRANSACTION_SUCCESSFUL = "CoreSdk Transaction Sync Successful"

        const val SYNC_CUSTOMER_COMMANDS = "CoreSdk Sync Customer Commands"
        const val SYNC_CUSTOMER_COMMANDS_ERROR = "CoreSdk Sync Customer Commands: Error"

        const val DEBUG = "Debug"
        const val ERROR = "CoreSdk Error"

        const val CORE_SDK_FEATURE_TOGGLE = "CoreSdk Feature Toggle"
        const val FEATURE_ACTIVATED = "Feature Activated"

        const val FORCE_SYNC_TRANSACTION_STARTED = "Force sync transaction started"
        const val FORCE_SYNC_TRANSACTION_COMPLETED = "Force sync transaction completed"
        const val FORCE_SYNC_TRANSACTION_ERROR = "Force sync transaction error"
    }

    object DebugType {
        const val SERVER_CONFLICT = "Server Conflict"
        const val LOCAL_CONFLICT = "Local Conflict"
        const val FILE_NOT_FOUND = "Txn Image Not Found"
        const val SYNC_COMMANDS_MAX_RETRIES_EXCEEDED = "Sync Commands Max Retries Exceeded"
        const val TRANSACTION_NOT_FOUND = "Transaction Not Found"
        const val FILE_DOWNLOAD_STATE = "File Download State"
        const val TRANSACTION_RECOVERY = "Transaction Recovery"
        const val TRANSACTION_RECOVERY_ERROR = "Transaction Recovery Error"
        const val ERROR_CODE_CONFLICT_UNHANDLED_DESCRIPTION = "Error Code Conflict Unhandled Description"
    }

    fun trackSyncTransactionCommands(
        step: String,
        transactionIds: List<String>,
        commandTypes: List<String>,
        flowId: String,
        iteration: Int,
        count: Int? = null,
        successResponseCount: Int? = null,
    ) {
        val properties = HashMap<String, Any>().apply {
            this[Key.STEP] = step
            this[Key.TRANSACTION_IDS] = transactionIds
            this[Key.COMMAND_TYPES] = commandTypes
            this[Key.ID] = flowId
            this[Key.ITERATION] = iteration.toString()
            count?.let { this[Key.COMMAND_COUNT] = it.toString() }
            successResponseCount?.let { this[Key.SUCCESS_RESPONSE_COUNT] = successResponseCount.toString() }
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.SYNC_TRANSACTION_COMMANDS, properties)
    }

    fun trackSyncTransactionCommandsError(
        type: String,
        flowId: String,
        reason: String?,
        stackTrace: String?,
        transactionIds: List<String>? = null,
        commandTypes: List<String>? = null,
    ) {
        val properties = HashMap<String, Any>().apply {
            this[Key.TYPE] = type
            transactionIds?.let { this[Key.TRANSACTION_IDS] = it }
            commandTypes?.let { this[Key.COMMAND_TYPES] = it }
            this[Key.ID] = flowId
            this[Key.REASON] = reason ?: ""
            this[Key.STACKTRACE] = stackTrace ?: ""
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.SYNC_TRANSACTION_COMMANDS_ERROR, properties)
    }

    fun trackSyncTransactions(step: String, flowId: String, source: String, type: String = "", count: Int = 0) {
        val properties = HashMap<String, String>().apply {
            this[Key.STEP] = step
            this[Key.ID] = flowId
            this[Key.SOURCE] = source
            this[Key.TYPE] = type
            this[Key.TRANSACTION_COUNT] = count.toString()
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.SYNC_TRANSACTIONS, properties)
    }

    fun trackSyncTransactionError(isFile: Boolean, type: String, flowId: String, reason: String?, stackTrace: String?) {
        val properties = HashMap<String, String>().apply {
            this[Key.IS_FILE] = isFile.toString()
            this[Key.TYPE] = type
            this[Key.ID] = flowId
            this[Key.REASON] = reason ?: ""
            this[Key.STACKTRACE] = stackTrace ?: ""
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.SYNC_TRANSACTIONS_ERROR, properties)
    }

    fun trackTransactionSyncSuccessful(transactionId: String, commandType: String, requestSize: Int) {
        val properties = HashMap<String, String>().apply {
            this[Key.TRANSACTION_ID] = transactionId
            this[Key.COMMAND_TYPE] = commandType
            this[Key.REQUEST_SIZE] = requestSize.toString()
        }
        analyticsProvider.get().trackEvents(Event.SYNC_TRANSACTION_SUCCESSFUL, properties)
    }

    fun trackDebug(type: String, meta: String) {
        val properties = HashMap<String, String>().apply {
            this[Key.TYPE] = type
            this[Key.META] = meta
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.DEBUG, properties)
    }

    fun trackCoreSdkFeatureStatus(
        step: String,
        featureFlagStatus: Boolean,
        inAppFeatureStatus: Boolean,
        toggleSuccessful: Boolean = false,
        reason: String? = null,
    ) {
        val properties = HashMap<String, String>().apply {
            this[Key.STEP] = step
            this[Key.FEATURE_FLAG_STATUS] = featureFlagStatus.toString()
            this[Key.IN_APP_FEATURE_STATUS] = inAppFeatureStatus.toString()
            this[Key.TOGGLE_SUCCESSFUL] = toggleSuccessful.toString()
            if (reason != null) this[Key.REASON] = reason
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.CORE_SDK_FEATURE_TOGGLE, properties)
    }

    fun trackFeatureActivated(feature: String) {
        val properties = HashMap<String, String>().apply {
            this[Key.FEATURE] = feature
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.FEATURE_ACTIVATED, properties)
    }

    fun trackForceTransactionSyncStarted(isCoreSync: Boolean) {
        val properties = HashMap<String, String>().apply {
            this[Key.IS_CORE_SYNC] = isCoreSync.toString()
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.FORCE_SYNC_TRANSACTION_STARTED, properties)
    }

    fun trackForceSyncTransactionsSuccess(isCoreSync: Boolean, totalDifferenceInTransaction: Int) {
        val properties = HashMap<String, String>().apply {
            this[Key.IS_CORE_SYNC] = isCoreSync.toString()
            this[Key.TRANSACTION_COUNT_DELTA] = totalDifferenceInTransaction.toString()
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.FORCE_SYNC_TRANSACTION_COMPLETED, properties)
    }

    fun trackForceSyncTransactionsError(isCoreSync: Boolean, throwable: Throwable) {
        val properties = HashMap<String, String>().apply {
            this[Key.IS_CORE_SYNC] = isCoreSync.toString()
            this[Key.REASON] = throwable.message.toString()
            this[Key.STACKTRACE] = throwable.getStringStackTrace()
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.FORCE_SYNC_TRANSACTION_ERROR, properties)
    }

    fun trackSyncCustomerCommands(
        step: String,
        operationId: String,
        customerId: String,
        flowId: String,
        iteration: Int,
        comment: String = "",
    ) {
        val properties = HashMap<String, Any>().apply {
            this[Key.STEP] = step
            this[Key.OPERATION_ID] = operationId
            this[Key.CUSTOMER_ID] = customerId
            this[Key.ID] = flowId
            this[Key.ITERATION] = iteration.toString()
            this[Key.COMMENT] = comment
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.SYNC_CUSTOMER_COMMANDS, properties)
    }

    fun trackSyncCustomerCommandsError(
        type: String,
        flowId: String,
        reason: String? = null,
    ) {
        val properties = HashMap<String, Any>().apply {
            this[Key.TYPE] = type
            this[Key.ID] = flowId
            this[Key.REASON] = reason ?: ""
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.SYNC_CUSTOMER_COMMANDS_ERROR, properties)
    }
}
