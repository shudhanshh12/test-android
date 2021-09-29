package `in`.okcredit.merchant.collection.server

import `in`.okcredit.collection.contract.*
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.merchant.collection.CollectionProfiles
import `in`.okcredit.merchant.collection.server.internal.ApiClientRiskV2
import `in`.okcredit.merchant.collection.server.internal.ApiEntityMapper
import `in`.okcredit.merchant.collection.server.internal.CollectionApiClient
import `in`.okcredit.merchant.collection.server.internal.CollectionBillingApiClient
import dagger.Lazy
import tech.okcredit.base.network.ApiError
import javax.inject.Inject

class CollectionRemoteSource @Inject constructor(
    private val collectionApiClient: Lazy<CollectionApiClient>,
    private val apiClientRiskV2: Lazy<ApiClientRiskV2>,
    private val collectionBillingApiClient: Lazy<CollectionBillingApiClient>,
) {

    suspend fun getCustomerCollections(customerId: String?, timestamp: Long?, businessId: String): List<Collection> {
        return collectionApiClient.get().listCustomerCollections(customerId, timestamp, businessId)
            .map { ApiEntityMapper.COLLECTION.convert(it)!! }
    }

    suspend fun getSupplierCollections(customerId: String?, timestamp: Long?, businessId: String): List<Collection> {
        return collectionApiClient.get().listSupplierCollections(customerId, timestamp, businessId)
            .map { ApiEntityMapper.COLLECTION.convert(it)!! }
    }

    suspend fun getCollectionProfiles(businessId: String): CollectionProfiles {
        val response = collectionApiClient.get()
            .getMerchantCollectionProfile(ApiMessages.MerchantCollectionProfileRequest(""), true, businessId)
        if (response.destination == null) {
            throw CollectionServerErrors.AddressNotFound()
        }
        return ApiEntityMapper.COLLECTION_MERCHANT_PROFILE.convert(
            response
        )!!
    }

    suspend fun getCollectionCustomerProfile(customerId: String, businessId: String): CollectionCustomerProfile {
        return ApiEntityMapper.COLLECTION_CUSTOMER_PROFILE.convert(

            collectionApiClient.get().getCustomerCollectionProfile(
                ApiMessages.CustomerCollectionProfileRequest(
                    "",
                    customerId
                ),
                businessId
            )
        )!!
    }

    suspend fun setActiveDestination(
        collectionMerchantProfile: CollectionMerchantProfile,
        async: Boolean,
        referralMerchant: String,
        businessId: String,
    ): ApiMessages.MerchantCollectionProfileResponse {
        val apiMessages = ApiMessages.DestinationRequest(
            "",
            collectionMerchantProfile.name,
            collectionMerchantProfile.payment_address,
            collectionMerchantProfile.type
        )

        return try {

            collectionApiClient.get().setActiveDestination(
                ApiMessages.SetActiveDestinationRequest(
                    collectionMerchantProfile.merchant_id,
                    apiMessages,
                    async,
                    referralMerchant
                ),
                businessId
            )
        } catch (error: ApiError) {
            if (error.code == 400) {
                when (error.message) {
                    CollectionConstants.INVALID_ACCOUNT_NUMBER -> {
                        throw CollectionServerErrors.InvalidAccountNumber()
                    }
                    CollectionConstants.INVALID_IFSC -> {
                        throw CollectionServerErrors.InvalidIFSCcode()
                    }
                    else -> {
                        throw error
                    }
                }
            } else {
                throw error
            }
        }
    }

    suspend fun validatePaymentAddress(
        payment_address_type: String,
        payment_address: String,
        businessId: String,
    ): Pair<Boolean, String> {
        try {
            val response =
                collectionApiClient.get().validatePaymentAddress(
                    ApiMessages.ValidatePaymentAddressRequest(
                        payment_address_type,
                        payment_address
                    ),
                    businessId
                )
            if (response.valid) {
                return response.valid to response.name!!
            } else {
                throw CollectionServerErrors.InvalidAPaymentAddress()
            }
        } catch (error: ApiError) {
            if (error.code == 400) {
                when (error.message) {
                    CollectionConstants.INVALID_ACCOUNT_NUMBER -> {
                        throw CollectionServerErrors.InvalidAccountNumber()
                    }
                    CollectionConstants.INVALID_IFSC -> {
                        throw CollectionServerErrors.InvalidIFSCcode()
                    }
                    else -> {
                        throw error
                    }
                }
            } else {
                throw error
            }
        }
    }

    suspend fun createBatchCollection(
        merchantId: String,
        customerIds: List<String>,
        businessId: String,
    ): List<Collection> {
        val requests = mutableListOf<ApiMessages.Request>()
        customerIds.map {
            requests.add(ApiMessages.Request(it))
        }

        return collectionApiClient.get().createBatchCollection(
            ApiMessages.BatchCreateCollectionsRequest(
                merchantId,
                requests as List<ApiMessages.Request>
            ),
            businessId
        ).map { ApiEntityMapper.COLLECTION.convert(it)!! }
    }

    suspend fun getPredictedCollectionMerchantProfile(businessId: String): CollectionMerchantProfile {
        val response =
            collectionApiClient.get().getPredictedCollectionMerchantProfile(businessId)
        return if (response.success) {
            ApiEntityMapper.convert(response)
        } else {
            CollectionMerchantProfile.empty()
        }
    }

    suspend fun getCollectionSupplierProfile(accountId: String, businessId: String): CollectionCustomerProfile {
        return ApiEntityMapper.COLLECTION_SUPPLIER_PROFILE.convert(

            collectionApiClient.get().getSupplierCollectionProfile(
                ApiMessages.GetSupplierCollectionProfileRequest(
                    accountId
                ),
                businessId
            )
        )!!
    }

    suspend fun enableCustomerPayment(businessId: String) {

        collectionApiClient.get()
            .enableCustomerPayment(ApiMessages.EnableCustomerPayment("customer_collection"), businessId)
    }

    suspend fun collectionEvent(customerId: String?, eventName: String, businessId: String) {
        collectionApiClient.get()
            .collectionEvent(ApiMessages.CollectionEventRequest(customerId, eventName), businessId)
    }

    suspend fun getKycRiskAttributes(businessId: String): ApiMessages.GetRiskAttributesResponse {
        return apiClientRiskV2.get()
            .getRiskAttributes(ApiMessages.GetRiskAttributesRequest(merchantId = businessId), businessId)
    }

    suspend fun getKycExternal(businessId: String): ApiMessages.GetKycExternalResponse {
        return collectionApiClient.get().getKycExternal(ApiMessages.GetKycExternalRequest(merchantId = ""), businessId)
    }

    suspend fun getKycRiskCategory(businessId: String): ApiMessages.GetKycRiskCategoryResponse {
        return collectionApiClient.get()
            .getRiskCategory(ApiMessages.GetKycRiskCategoryRequest(merchantId = businessId), businessId)
    }

    suspend fun getOnlinePaymentsList(startTime: Long, businessId: String): ApiMessages.GetOnlinePaymentResponse {
        return collectionApiClient.get()
            .getOnlinePaymentList(ApiMessages.GetOnlinePaymentsRequest(startTime), businessId)
    }

    suspend fun tagMerchantPaymentWithCustomer(customerId: String, paymentId: String, businessId: String) {

        collectionApiClient.get()
            .tagMerchantPaymentWithCustomer(
                ApiMessages.TagMerchantPaymentRequest(customerId, paymentId),
                businessId
            )
    }

    suspend fun setPaymentOutDestination(
        accountId: String,
        accountType: String,
        paymentType: String,
        paymentAddress: String,
        businessId: String,
    ) {
        try {

            collectionApiClient.get().setPaymentOutDestination(
                ApiMessages.SetPaymentOutDestinationRequest(
                    accountId,
                    ApiMessages.PaymentOutDestination(
                        type = paymentType,
                        paymentAddress = paymentAddress
                    ),
                    accountType
                ),
                businessId
            )
        } catch (error: ApiError) {
            if (error.code == 400) {
                when (error.message) {
                    CollectionConstants.INVALID_ACCOUNT_NUMBER -> {
                        throw CollectionServerErrors.InvalidAccountNumber()
                    }
                    CollectionConstants.INVALID_IFSC -> {
                        throw CollectionServerErrors.InvalidIFSCcode()
                    }
                    CollectionConstants.INVALID_PAYMENT_ADDRESS -> {
                        throw CollectionServerErrors.InvalidAPaymentAddress()
                    }
                    else -> {
                        throw error
                    }
                }
            } else {
                throw error
            }
        }
    }

    suspend fun getPaymentOutLinkDetail(
        accountId: String,
        accountType: String,
        businessId: String,
    ): ApiMessages.PaymentOutLinkDetailResponse {
        return try {

            collectionApiClient.get().getPaymentOutLinkDetail(
                ApiMessages.GetPaymentOutLinkDetailRequest(
                    accountId,
                    accountType
                ),
                businessId
            )
        } catch (error: ApiError) {
            if (error.code == 400) {
                when (error.message) {
                    CollectionConstants.DESTINATION_NOT_SET -> {
                        throw CollectionServerErrors.DestinationNotSet()
                    }
                    else -> {
                        throw error
                    }
                }
            } else {
                throw error
            }
        }
    }

    suspend fun triggerMerchantPayout(
        paymentType: String,
        collectionType: String,
        payoutId: String,
        paymentId: String,
        businessId: String,
    ) {

        collectionApiClient.get().triggerMerchantPayout(
            ApiMessages.TriggerMerchantPayoutRequest(
                paymentType,
                collectionType,
                payoutId,
                paymentId
            ),
            businessId
        )
    }

    suspend fun getBlindPayLinkId(accountId: String, businessId: String): ApiMessages.BlindPayCreateLinkResponse {
        return collectionApiClient.get().getBlindPayLink(ApiMessages.BlindPayCreateLinkRequest(accountId), businessId)
    }

    suspend fun customerPaymentIntent(businessId: String, customerId: String, paymentIntent: Boolean) {
        return collectionApiClient.get().customerPaymentIntent(
            ApiMessages.CustomerPaymentIntentRequest(
                merchantId = businessId,
                customerId = customerId,
                paymentIntent = paymentIntent.toString()
            ),
            businessId
        )
    }

    suspend fun getTargetedReferrals(businessId: String): ApiMessages.TargetedReferralResponse {
        return collectionApiClient.get().getTargetedReferrals(businessId)
    }

    suspend fun shareTargetedReferral(customerMerchantId: String, businessId: String) {
        collectionApiClient.get().shareTargetedReferral(
            ApiMessages.ShareTargetedReferralRequest(
                customerMerchantId = customerMerchantId
            ),
            businessId
        )
    }

    suspend fun setPaymentTag(timestamp: Long, businessId: String) {
        collectionApiClient.get().setPaymentTag(
            ApiMessages.PaymentTagRequest(
                tags = ApiMessages.TagRequest("true"),
                timestamp = timestamp
            ),
            businessId
        )
    }

    suspend fun getBlindPayShareLink(paymentId: String, businessId: String): ApiMessages.BlindPayShareLinkResponse {
        return collectionApiClient.get()
            .getBlindPayShareLink(ApiMessages.BlindPayShareLinkRequest(paymentId = paymentId), businessId)
    }

    suspend fun createBillingItem(inventoryItem: InventoryItem, businessId: String) {
        collectionBillingApiClient.get()
            .createBillingItem(
                request = inventoryItem,
                businessId = businessId,
            )
    }

    suspend fun getBillingItems(businessId: String): InventoryItemResponse {
        return collectionBillingApiClient.get()
            .getBillingItems(
                request = GetInventoryItemsRequest(businessId),
                businessId = businessId,
            )
    }

    suspend fun createBill(
        listBillItem: List<InventoryItem>,
        businessId: String,
    ): CreateInventoryBillsResponse {
        return collectionBillingApiClient.get()
            .createBill(
                request = GetInventoryItemRequest(merchantId = businessId, items = listBillItem),
                businessId = businessId,
            )
    }

    suspend fun getBills(businessId: String): GetInventoryBillsResponse {
        return collectionBillingApiClient.get()
            .getBills(
                request = GetInventoryItemsRequest(businessId),
                businessId = businessId,
            )
    }
}
