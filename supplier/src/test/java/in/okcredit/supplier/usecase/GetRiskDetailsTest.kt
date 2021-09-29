package `in`.okcredit.supplier.usecase

import `in`.okcredit.backend.server.GetRiskDetails
import `in`.okcredit.backend.server.RiskServerImpl
import `in`.okcredit.backend.server.riskInternal.RiskDetailsRequest
import `in`.okcredit.backend.server.riskInternal.RiskDetailsResponse
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.base.network.NetworkError

class GetRiskDetailsTest {
    private val supplierServerImpl: RiskServerImpl = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getRiskDetails = GetRiskDetails({ supplierServerImpl }, { getActiveBusinessId })

    @Test
    fun `GetRiskDetails returns successfully`() {
        val riskDetailsRequest = RiskDetailsRequest("serviceName", "client")
        val riskDetailsResponse: RiskDetailsResponse = mock()
        val businessId = "business-id"
        whenever(supplierServerImpl.getRiskDetails(riskDetailsRequest, businessId))
            .thenReturn(Single.just(riskDetailsResponse))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        val testObserver = getRiskDetails.execute("serviceName", "client").test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(riskDetailsResponse)
        )

        verify(supplierServerImpl).getRiskDetails(riskDetailsRequest, businessId)
    }

    @Test
    fun `GetRiskDetails returns  error`() {

        val mockError = NetworkError("network_error", Throwable("network werror"))
        val businessId = "business-id"

        val riskDetailsRequest = RiskDetailsRequest("serviceName", "client")

        whenever(supplierServerImpl.getRiskDetails(riskDetailsRequest, businessId))
            .thenReturn(Single.error(mockError))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        val testObserver = getRiskDetails.execute("serviceName", "client").test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Failure(mockError)
        )

        verify(supplierServerImpl).getRiskDetails(riskDetailsRequest, businessId)
    }
}
