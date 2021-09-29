package `in`.okcredit.merchant.customer_ui.data.server

import `in`.okcredit.merchant.customer_ui.data.server.model.request.*
import `in`.okcredit.merchant.customer_ui.data.server.model.response.ActiveStaffLinkResponse
import `in`.okcredit.merchant.customer_ui.data.server.model.response.CreateStaffLinkResponse
import `in`.okcredit.merchant.customer_ui.data.server.model.response.GooglePayPaymentResponse
import `in`.okcredit.merchant.customer_ui.data.server.model.response.Subscription
import dagger.Lazy
import retrofit2.HttpException
import tech.okcredit.base.network.ApiError
import javax.inject.Inject

class CustomerRemoteDataSource @Inject constructor(
    private val customerApiService: Lazy<CustomerApiService>,
    private val googlePayApiService: Lazy<GooglePayApiService>,
    private val staffLinkApiService: Lazy<StaffLinkApiService>,
) {

    suspend fun addSubscription(addSubscriptionRequest: AddSubscriptionRequest, businessId: String) =
        customerApiService.get().addSubscription(addSubscriptionRequest, businessId)

    suspend fun getSubscriptionList(merchantRequest: MerchantRequest, businessId: String) =
        customerApiService.get().listSubscription(merchantRequest, businessId)

    suspend fun updateSubscription(deleteSubscriptionRequest: Subscription, businessId: String) =
        customerApiService.get().updateSubscription(deleteSubscriptionRequest, businessId)

    suspend fun getSubscription(getSubscriptionRequest: GetSubscriptionRequest, businessId: String) =
        customerApiService.get().getSubscription(getSubscriptionRequest, businessId)

    suspend fun initiateGooglePayPayment(
        request: GooglePayPaymentRequest,
        businessId: String,
    ): GooglePayPaymentResponse {
        try {
            val response = googlePayApiService.get().initiateGooglePayPayment(request, businessId)
            if (response.code.toIntOrNull() != 200) {
                throw ApiError(response.code.toInt(), error = response.status)
            } else {
                return response
            }
        } catch (e: HttpException) {
            throw ApiError(e.code(), error = e.message())
        } catch (exception: Exception) {
            throw ApiError()
        }
    }

    suspend fun createCustomerStaffLink(
        staffLinkRequest: CreateStaffLinkRequest,
        businessId: String,
    ): CreateStaffLinkResponse {
        return staffLinkApiService.get().createStaffLink(staffLinkRequest, businessId)
    }

    suspend fun activeStaffLinkDetails(businessId: String): ActiveStaffLinkResponse {
        return staffLinkApiService.get().activeStaffLink(businessId)
    }

    suspend fun deleteCollectionStaffLink(businessId: String) {
        staffLinkApiService.get().deleteStaffLink(businessId)
    }

    suspend fun editCollectionStaffLink(request: EditStaffLinkRequest, businessId: String) {
        staffLinkApiService.get().editStaffLink(request, businessId)
    }
}
