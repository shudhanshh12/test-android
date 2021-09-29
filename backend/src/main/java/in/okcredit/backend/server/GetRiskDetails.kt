package `in`.okcredit.backend.server

import `in`.okcredit.backend.server.riskInternal.RiskDetailsRequest
import `in`.okcredit.backend.server.riskInternal.RiskDetailsResponse
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class GetRiskDetails @Inject constructor(
    private val supplierServerImpl: Lazy<RiskServerImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(serviceName: String, client: String): Single<RiskDetailsResponse> {
        return getActiveBusinessId.get().execute().flatMap { _businessId ->
            supplierServerImpl.get().getRiskDetails(RiskDetailsRequest(serviceName, client), _businessId)
        }
    }
}
