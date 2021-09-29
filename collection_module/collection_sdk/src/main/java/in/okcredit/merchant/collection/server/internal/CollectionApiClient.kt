package `in`.okcredit.merchant.collection.server.internal

import `in`.okcredit.collection.contract.ApiMessages
import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface CollectionApiClient {

    @GET("v1/ListCustomerCollections")
    suspend fun listCustomerCollections(
        @Query("customer_id") customerId: String?,
        @Query("after") fromTimestamp: Long?,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): List<ApiMessages.Collection>

    @GET("v1/ListSupplierCollections")
    suspend fun listSupplierCollections(
        @Query("customer_id") customerId: String?,
        @Query("after") fromTimestamp: Long?,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): List<ApiMessages.Collection>

    // Get Merchant Collection Profile
    @POST("v1/GetMerchantCollectionProfile")
    suspend fun getMerchantCollectionProfile(
        @Body request: ApiMessages.MerchantCollectionProfileRequest,
        @Query("isUpdated") isUpdated: Boolean,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ApiMessages.MerchantCollectionProfileResponse

    // Get Customer Collection Profile
    @POST("v1/GetCustomerCollectionProfile")
    suspend fun getCustomerCollectionProfile(
        @Body request: ApiMessages.CustomerCollectionProfileRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ApiMessages.CustomerCollectionProfileResponse

    // Batch Create Collection
    @POST("v1/BatchCreateCollections")
    suspend fun createBatchCollection(
        @Body request: ApiMessages.BatchCreateCollectionsRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): List<ApiMessages.Collection>

    // Set Active Destination
    @POST("v1/SetActiveDestination")
    suspend fun setActiveDestination(
        @Body request: ApiMessages.SetActiveDestinationRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ApiMessages.MerchantCollectionProfileResponse

    // Validate Payment Address
    @POST("v1/ValidatePaymentAddress")
    suspend fun validatePaymentAddress(
        @Body request: ApiMessages.ValidatePaymentAddressRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ApiMessages.ValidatePaymentAddressResponse

    // Get predicted Collection Merchant Profile
    @POST("v1/GetPredictedDestination")
    suspend fun getPredictedCollectionMerchantProfile(
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ApiMessages.PredictedMerchantCollectionProfileResponse

    // Get Customer Collection Profile
    @POST("v1/GetSupplierCollectionProfile")
    suspend fun getSupplierCollectionProfile(
        @Body request: ApiMessages.GetSupplierCollectionProfileRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ApiMessages.SupplierCollectionProfileResponse

    @POST("v1/enableCustomerPayment")
    suspend fun enableCustomerPayment(
        @Body request: ApiMessages.EnableCustomerPayment,
        @Header(BUSINESS_ID_HEADER) businessId: String
    )

    @POST("v1/GetKycExternal")
    suspend fun getKycExternal(
        @Body request: ApiMessages.GetKycExternalRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): ApiMessages.GetKycExternalResponse

    @POST("v1/getRiskCategory")
    suspend fun getRiskCategory(
        @Body request: ApiMessages.GetKycRiskCategoryRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): ApiMessages.GetKycRiskCategoryResponse

    @POST("v2/ListMerchantPayment")
    suspend fun getOnlinePaymentList(
        @Body request: ApiMessages.GetOnlinePaymentsRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ApiMessages.GetOnlinePaymentResponse

    @POST("v1/TagMerchantPayment")
    suspend fun tagMerchantPaymentWithCustomer(
        @Body request: ApiMessages.TagMerchantPaymentRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    )

    @POST("v1/SetPaymentOutDestination")
    suspend fun setPaymentOutDestination(
        @Body request: ApiMessages.SetPaymentOutDestinationRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ApiMessages.SetPaymentOutDestinationResponse

    @POST("v1/GetPaymentOutLinkDetail")
    suspend fun getPaymentOutLinkDetail(
        @Body request: ApiMessages.GetPaymentOutLinkDetailRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ApiMessages.PaymentOutLinkDetailResponse

    @POST("v1/TriggerMerchantPayout")
    suspend fun triggerMerchantPayout(
        @Body request: ApiMessages.TriggerMerchantPayoutRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    )

    @POST("v1/collectionEvent")
    suspend fun collectionEvent(
        @Body request: ApiMessages.CollectionEventRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    )

    @POST("v1/BlindPay/CreateLink")
    suspend fun getBlindPayLink(
        @Body request: ApiMessages.BlindPayCreateLinkRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ApiMessages.BlindPayCreateLinkResponse

    @GET("v1/GetOrCreateTargetedReferrals")
    suspend fun getTargetedReferrals(
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ApiMessages.TargetedReferralResponse

    @POST("v1/ShareTargetedReferral")
    suspend fun shareTargetedReferral(
        @Body request: ApiMessages.ShareTargetedReferralRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    )

    @POST("v1/CustomerPaymentIntent")
    suspend fun customerPaymentIntent(
        @Body request: ApiMessages.CustomerPaymentIntentRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    )

    @POST("v1/PaymentTags")
    suspend fun setPaymentTag(
        @Body request: ApiMessages.PaymentTagRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    )

    @POST("v1/BlindPay/ResendReceiverCommunication")
    suspend fun getBlindPayShareLink(
        @Body request: ApiMessages.BlindPayShareLinkRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ApiMessages.BlindPayShareLinkResponse
}
