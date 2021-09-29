package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.backend._offline.server.internal.CheckActionableStatusRequest
import `in`.okcredit.backend._offline.usecase.ServerActionableChecker.Action
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.device.DeviceRepository
import android.content.Context
import androidx.work.*
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import org.apache.commons.jcs.access.exception.InvalidArgumentException
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * This class checks if there is any [Action] available for the merchant's device.
 * An [Action] is particularly used in rare conditions like when some re-sync of data is required.
 *
 * Flow : Check if there is any action to be done for current device by calling POST /status API.
 * If there is some action available in the API response, perform the action and call PUT /status/{action_id} to mark
 * the action as completed.
 */
@Reusable
class ServerActionableChecker @Inject constructor(
    private val backendRemoteSource: Lazy<BackendRemoteSource>,
    private val deviceRepository: Lazy<DeviceRepository>,
    private val coreSdkTransactionActionableHandler: Lazy<CoreSdkTransactionActionableHandler>,
    private val workManager: Lazy<OkcWorkManager>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    companion object {
        private const val WORKER_NAME = "ServerActionableChecker"
    }

    enum class Action(val code: Int) {
        NO_ACTION(0),
        TXN_CREATE(1),
    }

    internal fun execute(businessId: String? = null): Completable {
        val deviceId = deviceRepository.get().deviceDeprecated.id
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId)
            .flatMapCompletable { _businessId ->
                backendRemoteSource.get().checkActionableStatus(CheckActionableStatusRequest(deviceId), _businessId)
                    .flatMapCompletable { response ->
                        when (response.action) {
                            Action.NO_ACTION.code -> {
                                Completable.complete()
                            }
                            Action.TXN_CREATE.code -> {
                                coreSdkTransactionActionableHandler.get()
                                    .handle(response.actionId, response.startTime, response.endTime, _businessId)
                                    .flatMapCompletable { wasHandled ->
                                        if (wasHandled) {
                                            backendRemoteSource.get().updateActionableStatus(response.actionId, _businessId)
                                        } else {
                                            Completable.complete()
                                        }
                                    }
                            }
                            else -> {
                                val e =
                                    InvalidArgumentException("ServerActionableChecker - unsupported action: ${response.action}")
                                Timber.e(e)
                                RecordException.recordException(e)
                                Completable.complete()
                            }
                        }
                    }
            }
    }

    fun schedule(businessId: String): Completable {
        return Completable
            .fromAction {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val workRequest = OneTimeWorkRequest.Builder(ServerActionableCheckerWorker::class.java)
                    .setConstraints(constraints)
                    .setInputData(
                        workDataOf(
                            ServerActionableCheckerWorker.BUSINESS_ID to businessId
                        )
                    )
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                    .build()
                    .enableWorkerLogging()

                workManager.get()
                    .schedule(WORKER_NAME, Scope.Business(businessId), ExistingWorkPolicy.KEEP, workRequest)
            }
            .subscribeOn(ThreadUtils.newThread())
    }
}

class ServerActionableCheckerWorker constructor(
    context: Context,
    params: WorkerParameters,
    private val serverActionableChecker: ServerActionableChecker,
) : BaseRxWorker(context, params) {
    companion object {
        const val BUSINESS_ID = "business_id"
    }

    override fun doRxWork(): Completable {
        val businessId = inputData.getString(BUSINESS_ID)
        return serverActionableChecker.execute(businessId)
    }

    class Factory @Inject constructor(
        private val serverActionableChecker: Lazy<ServerActionableChecker>
    ) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return ServerActionableCheckerWorker(context, params, serverActionableChecker.get())
        }
    }
}
