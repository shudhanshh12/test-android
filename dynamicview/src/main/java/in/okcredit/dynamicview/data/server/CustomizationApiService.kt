package `in`.okcredit.dynamicview.data.server

import `in`.okcredit.dynamicview.data.model.Customization
import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface CustomizationApiService {

    @POST("v2/ListCustomizations")
    suspend fun listCustomizations(
        @Body req: GetCustomizationRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): List<Customization>
}
