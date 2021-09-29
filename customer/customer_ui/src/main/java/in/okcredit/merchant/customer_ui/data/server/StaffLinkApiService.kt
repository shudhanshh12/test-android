package `in`.okcredit.merchant.customer_ui.data.server

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import `in`.okcredit.merchant.customer_ui.data.server.model.request.CreateStaffLinkRequest
import `in`.okcredit.merchant.customer_ui.data.server.model.request.EditStaffLinkRequest
import `in`.okcredit.merchant.customer_ui.data.server.model.response.ActiveStaffLinkResponse
import `in`.okcredit.merchant.customer_ui.data.server.model.response.CreateStaffLinkResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface StaffLinkApiService {

    @POST("/v1/SetStaffLink")
    suspend fun createStaffLink(
        @Body request: CreateStaffLinkRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): CreateStaffLinkResponse

    @GET("/v1/GetStaffLink")
    suspend fun activeStaffLink(@Header(BUSINESS_ID_HEADER) businessId: String): ActiveStaffLinkResponse

    @POST("/v1/EditStaffLink")
    suspend fun editStaffLink(
        @Body request: EditStaffLinkRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    )

    @DELETE("/v1/DeleteStaffLink")
    suspend fun deleteStaffLink(@Header(BUSINESS_ID_HEADER) businessId: String)
}
