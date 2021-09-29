package `in`.okcredit.backend._offline.usecase.reports_v2

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkInfo
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import kotlinx.coroutines.launch
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.lang.ref.WeakReference
import javax.inject.Inject

class DownloadReportWorkerStatusProvider @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
    private val dispatcherProvider: Lazy<DispatcherProvider>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(weakLifecycleOwner: WeakReference<LifecycleOwner>, workerName: String): Observable<WorkerStatus> {
        return getActiveBusinessId.get().execute()
            .flatMapObservable { businessId ->
                Observable.create { emitter ->
                    emitCurrentStatus(emitter, weakLifecycleOwner, workerName, businessId)
                    observeAndEmitStatus(emitter, weakLifecycleOwner, workerName, businessId)
                }
            }
    }

    private fun emitCurrentStatus(
        emitter: ObservableEmitter<WorkerStatus>,
        weakLifecycleOwner: WeakReference<LifecycleOwner>,
        workerName: String,
        businessId: String,
    ) = weakLifecycleOwner.get()?.lifecycleScope?.launch(dispatcherProvider.get().io()) {
        val currentStatus = getCurrentWorkerStatus(workerName, businessId)
        emitter.onNext(currentStatus)
    }

    private fun getCurrentWorkerStatus(workerName: String, businessId: String): WorkerStatus {
        val workInfoList =
            workManager.get().getWorkInfosForUniqueWork(workerName, Scope.Business(businessId)).get()
        return WorkerStatus.fromWorkInfoStateList(workInfoList)
    }

    private fun observeAndEmitStatus(
        emitter: ObservableEmitter<WorkerStatus>,
        weakLifecycleOwner: WeakReference<LifecycleOwner>,
        workerName: String,
        businessId: String,
    ) = weakLifecycleOwner.get()?.let { lifecycleOwner ->
        lifecycleOwner.lifecycleScope.launch(dispatcherProvider.get().main()) {
            workManager.get().getWorkInfosForUniqueWorkLiveData(workerName, Scope.Business(businessId))
                .observe(lifecycleOwner) { workInfoList ->
                    val status = WorkerStatus.fromWorkInfoStateList(workInfoList)
                    emitter.onNext(status)
                }
        }
    }
}

sealed class WorkerStatus {
    object Idle : WorkerStatus()
    object Running : WorkerStatus()
    data class Completed(val uriString: String?) : WorkerStatus()
    data class Error(val isInternetIssue: Boolean = false) : WorkerStatus()

    companion object {
        @NonNls
        const val URI_STRING_KEY = "uri_string_key"

        @NonNls
        const val IS_INTERNET_ISSUE_KEY = "is_internet_issue_key"

        fun fromWorkInfoStateList(workInfoList: List<WorkInfo>): WorkerStatus {
            if (workInfoList.isEmpty()) return Idle
            val workInfo = workInfoList[0]

            return when (workInfo.state) {
                WorkInfo.State.ENQUEUED -> Running
                WorkInfo.State.RUNNING -> Running
                WorkInfo.State.SUCCEEDED -> completeWithPath(workInfo)
                WorkInfo.State.FAILED -> errorWithReason(workInfo)
                WorkInfo.State.BLOCKED -> Running
                WorkInfo.State.CANCELLED -> errorWithReason(workInfo)
            }
        }

        private fun completeWithPath(workInfo: WorkInfo): WorkerStatus {
            val path = workInfo.outputData.getString(URI_STRING_KEY)
            return Completed(path)
        }

        private fun errorWithReason(workInfo: WorkInfo): WorkerStatus {
            val isInternetIssue = workInfo.outputData.getBoolean(IS_INTERNET_ISSUE_KEY, false)
            return Error(isInternetIssue)
        }
    }
}
