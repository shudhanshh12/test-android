package `in`.okcredit.communication_inappnotification.remote

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface InAppNotificationApiClient {

    @POST("ListInAppNotification")
    suspend fun getNotifications(
        @Body request: GetInAppNotificationsRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): GetInAppNotificationsResponse

    @POST("AcknowledgeInAppNotification")
    suspend fun acknowledgeNotifications(
        @Body request: AckNotificationsRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): AckNotificationsResponse
}
