package `in`.okcredit.backend._offline.usecase.reports_v2

import `in`.okcredit.backend._offline.server.internal.GenerateReportUrlRequest
import `in`.okcredit.backend._offline.server.internal.GetReportUrlResponse
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.device.DeviceRepository
import `in`.okcredit.shared.service.rxdownloader.RxDownloader
import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.NonNull
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxCompletable
import org.jetbrains.annotations.NonNls
import org.joda.time.DateTime
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.android.base.workmanager.WorkerConfig
import tech.okcredit.base.network.utils.NetworkHelper
import java.util.concurrent.TimeUnit
import javax.inject.Inject

open class DownloadReport @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
    private val rxDownloader: Lazy<RxDownloader>,
    private val localeManager: Lazy<LocaleManager>,
    private val deviceRepository: Lazy<DeviceRepository>,
    private val repository: Lazy<ReportsV2Repository>,
    private val downloadReportFileNameProvider: Lazy<DownloadReportFileNameProvider>,
    private val tracker: Lazy<ReportsV2Tracker>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    @Keep
    enum class ReportType(val fileName: String, val typeKeywordAtServer: String) {

        @NonNls
        BACKUP_ALL("OkCredit_%s_Backup.pdf", "backup-all"),

        @NonNls
        CUSTOMER_ACCOUNT("OkCredit_AccountStatement_%s_%s.pdf", "account"),

        @NonNls
        SUPPLIER_ACCOUNT("OkCredit_AccountStatement_%s_%s.pdf", "supplier"),

        @NonNls
        CUSTOMER_REPORT("OkCredit_CustomerStatement_%s_%s.pdf", "customer-report"),

        @NonNls
        SUPPLIER_REPORT("OkCredit_SupplierStatement_%s_%s.pdf", "supplier-report")
    }

    data class Request(
        val reportType: ReportType,
        val accountId: String? = null,
        val startTimeSec: DateTime? = null,
        val endTimeSec: DateTime? = null,
        val workName: String,
    )

    companion object {
        @NonNls
        const val WORK_TAG = "DownloadReportV2"

        @NonNls
        private const val ACCOUNT_ID_KEY = "account_id"

        @NonNls
        private const val START_TIME_KEY = "start_time"

        @NonNls
        private const val END_TIME_KEY = "end_time"

        @NonNls
        private const val FILE_NAME_KEY = "file_name"

        @NonNls
        private const val REPORT_TYPE_AT_SERVER_KEY = "report_type_at_server"

        @NonNls
        private const val REPORT_TYPE_NAME = "report_type_name"

        @NonNls
        private const val BUSINESS_ID = "business_id"

        @NonNls
        private const val REPORT_URL_GENERATION_IN_PROGRESS = "open"

        @NonNls
        private const val REPORT_URL_ERROR = "error"

        @NonNls
        private const val MIME_TYPE = "application/pdf"

        @NonNls
        private const val WORK_NAME_PREFIX = "download-report-"

        const val DOWNLOAD_REPORT_INTERVAL_IN_SECONDS_KEY = "download_report_interval_in_seconds"

        fun getWorkName(reportType: ReportType, accountId: String? = null) =
            if (accountId.isNullOrEmpty())
                WORK_NAME_PREFIX + reportType.name
            else
                WORK_NAME_PREFIX + reportType.name + "-$accountId"
    }

    private var pollingCounter = 0

    internal fun execute(
        accountId: String?,
        startTimeSec: Long,
        endTimeSec: Long,
        fileName: String,
        reportTypeServerKey: String,
        reportTypeName: String,
        businessId: String,
    ): Single<String> {
        return getReportV2Url(
            accountId,
            startTimeSec,
            endTimeSec,
            reportTypeServerKey,
            businessId,
        )
            .doOnSubscribe {
                trackWorkerStarted(reportTypeName, accountId, startTimeSec, endTimeSec)
            }
            .flatMap { downloadReport(it.reportUrl, fileName) }
            .onErrorResumeNext {
                RecordException.recordException(it)
                tracker.get().trackError(it, pollingCounter, reportTypeName, accountId, startTimeSec, endTimeSec)
                propagateException(it)
            }
            .doOnSuccess {
                tracker.get().trackSuccess(reportTypeName, pollingCounter, accountId, startTimeSec, endTimeSec)
            }
    }

    fun getReportV2Url(
        accountId: String?,
        startTimeSec: Long,
        endTimeSec: Long,
        reportTypeServerKey: String,
        businessId: String,
    ): Single<GetReportUrlResponse> {
        return deviceRepository.get().getDevice()
            .firstOrError()
            .flatMap {
                createReportUrlGenerateRequest(
                    accountId,
                    startTimeSec,
                    endTimeSec,
                    reportTypeServerKey,
                    it.id,
                    businessId
                )
            }
            .flatMap { keepRetryingAndGetReportUrl(it.reportId, businessId) }
            .flatMap { checkForError(it) }
    }

    private fun trackWorkerStarted(
        reportTypeName: String,
        accountId: String?,
        startTimeSec: Long,
        endTimeSec: Long,
    ) {
        pollingCounter = 0
        tracker.get().trackWorkerStarted(reportTypeName, accountId, startTimeSec, endTimeSec)
    }

    private fun createReportUrlGenerateRequest(
        accountId: String?,
        startTimeSec: Long,
        endTimeSec: Long,
        reportTypeString: String,
        deviceId: String,
        businessId: String,
    ) = repository.get().generateReportUrl(
        GenerateReportUrlRequest(
            type = reportTypeString,
            lang = localeManager.get().getLanguage(),
            accountId = accountId,
            startTime = startTimeSec,
            endTime = endTimeSec,
            deviceId = deviceId
        ),
        businessId
    )

    private fun keepRetryingAndGetReportUrl(reportId: String, businessId: String): Single<GetReportUrlResponse> {
        val intervalInSeconds = firebaseRemoteConfig.get().getLong(DOWNLOAD_REPORT_INTERVAL_IN_SECONDS_KEY)
        return Single.defer {
            repository.get().getReportUrl(reportId, businessId)
                .doOnSubscribe { pollingCounter++ }
        }
            .repeatWhen { it.delay(intervalInSeconds, TimeUnit.SECONDS) }
            .takeUntil { isUrlGenerationCompleted(it) }
            .filter { isUrlGenerationCompleted(it) }
            .firstOrError()
    }

    private fun isUrlGenerationCompleted(getReportUrlResponse: GetReportUrlResponse) =
        getReportUrlResponse.status != REPORT_URL_GENERATION_IN_PROGRESS

    private fun checkForError(response: GetReportUrlResponse): Single<GetReportUrlResponse> {
        return if (isUrlGenerationError(response)) Single.error(ReportUrlGenerationApiError(response.errorMessage))
        else Single.just(response)
    }

    private fun isUrlGenerationError(getReportUrlResponse: GetReportUrlResponse) =
        getReportUrlResponse.status == REPORT_URL_ERROR

    private fun downloadReport(reportUrl: String?, fileName: String): Single<String> {
        if (reportUrl.isNullOrBlank()) throw IllegalArgumentException("reportUrl should not be null or blank")
        return rxDownloader.get().download(reportUrl, fileName, MIME_TYPE, true)
    }

    private fun propagateException(throwable: Throwable): Single<String> {
        val exception = if (isInternetIssue(throwable)) NoInternetException else throwable
        return Single.error(exception)
    }

    private fun isInternetIssue(@NonNull throwable: Throwable): Boolean {
        return NetworkHelper.isNetworkError(throwable)
    }

    fun schedule(request: Request): Completable {
        return rxCompletable {
            val businessId = getActiveBusinessId.get().execute().await()
            val inputData = buildInputData(request, businessId)
            val workRequest = buildWorkRequest(inputData, request.workName)
            enqueueWorkRequest(workRequest, request.workName, businessId)
        }
            .subscribeOn(ThreadUtils.newThread())
    }

    private fun buildInputData(request: Request, businessId: String): Data {
        val fileName = downloadReportFileNameProvider.get().execute(request)
        val startTimeSec = request.startTimeSec?.toSeconds() ?: -1
        val endTimeSec = request.endTimeSec?.toSeconds() ?: -1
        return Data.Builder()
            .putString(ACCOUNT_ID_KEY, request.accountId)
            .putLong(START_TIME_KEY, startTimeSec)
            .putLong(END_TIME_KEY, endTimeSec)
            .putString(FILE_NAME_KEY, fileName)
            .putString(REPORT_TYPE_AT_SERVER_KEY, request.reportType.typeKeywordAtServer)
            .putString(REPORT_TYPE_NAME, request.reportType.name)
            .putString(BUSINESS_ID, businessId)
            .build()
    }

    private fun enqueueWorkRequest(workRequest: OneTimeWorkRequest, workName: String, businessId: String) =
        workManager.get()
            .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.KEEP, workRequest)

    private fun buildWorkRequest(inputData: Data, workName: String) =
        OneTimeWorkRequest.Builder(Worker::class.java)
            .addTag(workName)
            .addTag(WORK_TAG)
            .setInputData(inputData)
            .build()
            .enableWorkerLogging()

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val downloadReport: Lazy<DownloadReport>,
    ) : BaseRxWorker(context, params, WorkerConfig(maxAttemptCount = 0)) {
        override fun doRxWork(): Completable {
            val accountId = inputData.getString(ACCOUNT_ID_KEY)
            val startTimeSec = inputData.getLong(START_TIME_KEY, -1)
            val endTimeSec = inputData.getLong(END_TIME_KEY, -1)
            val fileName = inputData.getString(FILE_NAME_KEY) ?: throw IllegalArgumentException()
            val reportTypeServerKey = inputData.getString(REPORT_TYPE_AT_SERVER_KEY) ?: throw IllegalArgumentException()
            val reportTypeName = inputData.getString(REPORT_TYPE_NAME) ?: throw IllegalArgumentException()
            val businessId = inputData.getString(BUSINESS_ID) ?: throw IllegalArgumentException()

            return downloadReport.get()
                .execute(accountId, startTimeSec, endTimeSec, fileName, reportTypeServerKey, reportTypeName, businessId)
                .doOnSuccess { path ->
                    val outputData = Data.Builder().putString(WorkerStatus.URI_STRING_KEY, path).build()
                    setOutputData(outputData)
                }.doOnError { throwable: Throwable? ->
                    throwable?.let {
                        RecordException.recordException(it)
                        if (it is NoInternetException) {
                            val outputData = Data.Builder().putBoolean(WorkerStatus.IS_INTERNET_ISSUE_KEY, true).build()
                            setOutputData(outputData)
                        }
                    }
                }.ignoreElement()
        }

        class Factory @Inject constructor(
            private val downloadReport: Lazy<DownloadReport>,
        ) :
            ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, downloadReport)
            }
        }
    }

    object NoInternetException : Exception()
    data class ReportUrlGenerationApiError(val errorMessage: String?) : Exception(errorMessage ?: "")

    private fun DateTime.toSeconds() = TimeUnit.MILLISECONDS.toSeconds(this.millis)
}
