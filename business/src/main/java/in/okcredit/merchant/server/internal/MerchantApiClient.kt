package `in`.okcredit.merchant.server.internal

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface MerchantApiClient {

    @GET("v1.0/categories")
    fun getCategory(
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.CategoryResponse>>

    @GET("v2.0/business-types")
    fun getBusinessTypes(
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.BusinessTypeResponse>>
}
