package `in`.okcredit.backend._offline.usecase.reports_v2

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.utils.TimeUtils.toSeconds
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetReportV2UrlWithTimeout @Inject constructor(
    private val downloadReport: Lazy<DownloadReport>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
) {
    fun execute(
        accountId: String?,
        startTimeSec: Long,
        endTimeSec: Long,
        reportTypeServerKey: String,
    ): Single<String> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            downloadReport.get().getReportV2Url(
                accountId,
                startTimeSec.toSeconds(),
                endTimeSec.toSeconds(),
                reportTypeServerKey,
                businessId,
            )
                .timeout(
                    firebaseRemoteConfig.get().getLong(FRC_SHARE_REPORT_MAX_POLLING_TIME_IN_SECONDS_KEY),
                    TimeUnit.SECONDS
                )
                .map {
                    it.reportUrl
                }
        }
    }

    companion object {
        const val FRC_SHARE_REPORT_MAX_POLLING_TIME_IN_SECONDS_KEY = "share_report_max_polling_time_in_seconds"
    }
}
