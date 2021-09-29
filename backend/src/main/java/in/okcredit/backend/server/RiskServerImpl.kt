package `in`.okcredit.backend.server

import `in`.okcredit.backend.server.riskInternal.RiskApiClient
import `in`.okcredit.backend.server.riskInternal.RiskDetailsRequest
import `in`.okcredit.backend.server.riskInternal.RiskDetailsResponse
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError
import javax.inject.Inject

class RiskServerImpl @Inject constructor(
    private val apiClient: Lazy<RiskApiClient>
) {

    fun getRiskDetails(riskDetailsRequest: RiskDetailsRequest, businessId: String): Single<RiskDetailsResponse> {
        return apiClient.get().getRiskDetails(riskDetailsRequest, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map {
                if (it.isSuccessful && it.body() != null) {
                    it.body()
                } else {
                    throw it.asError()
                }
            }
    }
}
