package `in`.okcredit.backend.server.riskInternal

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface RiskApiClient {

    @POST("v2/GetPaymentInstruments")
    fun getRiskDetails(
        @Body request: RiskDetailsRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<RiskDetailsResponse>>
}
