package `in`.okcredit.backend._offline.usecase.reports_v2

import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.backend._offline.server.internal.GenerateReportUrlRequest
import `in`.okcredit.backend._offline.server.internal.GenerateReportUrlResponse
import `in`.okcredit.backend._offline.server.internal.GetReportUrlResponse
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class ReportsV2Repository @Inject constructor(
    private val backendRemoteSource: Lazy<BackendRemoteSource>,
) {
    fun generateReportUrl(
        generateReportUrlRequest: GenerateReportUrlRequest,
        businessId: String,
    ): Single<GenerateReportUrlResponse> {
        return backendRemoteSource.get().generateReportUrl(generateReportUrlRequest, businessId)
    }

    fun getReportUrl(reportId: String, businessId: String): Single<GetReportUrlResponse> {
        return backendRemoteSource.get().getReportUrl(reportId, businessId)
    }
}
