package `in`.okcredit.merchant.collection.server.internal

import `in`.okcredit.collection.contract.ApiMessages
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.merchant.collection.CollectionProfiles
import `in`.okcredit.merchant.collection.utils.Utils
import com.google.common.base.Converter
import org.joda.time.DateTime

object ApiEntityMapper {

    private const val KEY_BLIND_PAY = "blind_pay"

    var COLLECTION: Converter<ApiMessages.Collection, Collection> =
        object : Converter<ApiMessages.Collection, Collection>() {
            override fun doForward(collection: ApiMessages.Collection): Collection {

                return Collection(
                    id = collection.id,
                    create_time = collection.create_time,
                    update_time = collection.update_time,
                    status = collection.status,
                    payment_link = collection.payment_link,
                    amount_requested = collection.amount_requested,
                    amount_collected = collection.amount_collected,
                    fee = collection.fee,
                    expire_time = collection.expire_time,
                    customer_id = collection.customer_id,
                    discount = collection.discount,
                    fee_category = collection.fee_category,
                    settlement_category = collection.settlement_category,
                    lastSyncTime = DateTime.now(),
                    lastViewTime = null,
                    merchantName = collection.payout.destination.name,
                    paymentOriginName = collection.payment.origin?.name,
                    paymentId = collection.paymentId,
                    errorCode = collection.errorCode ?: "",
                    errorDescription = collection.errorDescription ?: "",
                    blindPay = collection.labels?.get(KEY_BLIND_PAY)?.toBoolean() == true,
                    cashbackGiven = collection.cashbackGiven ?: false,
                )
            }

            override fun doBackward(b: Collection): ApiMessages.Collection {
                throw RuntimeException("illegal operation: cannot convert Collection domain entity to api entity")
            }
        }

    var COLLECTION_MERCHANT_PROFILE: Converter<ApiMessages.MerchantCollectionProfileResponse, CollectionProfiles> =
        object :
            Converter<ApiMessages.MerchantCollectionProfileResponse, CollectionProfiles>() {
            override fun doForward(collectionProfile: ApiMessages.MerchantCollectionProfileResponse): CollectionProfiles {
                return CollectionProfiles(
                    CollectionMerchantProfile(
                        merchant_id = collectionProfile.merchantId ?: "",
                        name = collectionProfile.destination?.name,
                        payment_address = collectionProfile.destination?.paymentAddress ?: "",
                        type = collectionProfile.destination?.type ?: "",
                        merchant_vpa = collectionProfile.merchantVpa,
                        limit = collectionProfile.limit ?: 0L,
                        limitType = collectionProfile.limitType,
                        remainingLimit = collectionProfile.remainingLimit ?: 0L,
                        merchantQrEnabled = collectionProfile.merchantQrEnabled ?: false
                    ),
                    getCustomerCollectionProfiles(
                        collectionProfile
                    ),
                    getSupplierCollectionProfiles(
                        collectionProfile
                    )
                )
            }

            override fun doBackward(b: CollectionProfiles): ApiMessages.MerchantCollectionProfileResponse {
                throw RuntimeException("illegal operation: cannot convert Collection domain entity to api entity")
            }
        }

    internal fun getSupplierCollectionProfiles(collectionProfile: ApiMessages.MerchantCollectionProfileResponse): List<CollectionCustomerProfile> {
        return Utils.mapList(
            collectionProfile.suppliers,
            COLLECTION_SUPPLIER_PROFILE
        )
    }

    internal fun getCustomerCollectionProfiles(collectionProfile: ApiMessages.MerchantCollectionProfileResponse): List<CollectionCustomerProfile> {
        return Utils.mapList(
            collectionProfile.customers,
            COLLECTION_CUSTOMER_PROFILE
        )
    }

    var COLLECTION_CUSTOMER_PROFILE: Converter<ApiMessages.CustomerCollectionProfileResponse, CollectionCustomerProfile> =
        object :
            Converter<ApiMessages.CustomerCollectionProfileResponse, CollectionCustomerProfile>() {
            override fun doForward(collectionProfile: ApiMessages.CustomerCollectionProfileResponse): CollectionCustomerProfile {
                return CollectionCustomerProfile(
                    accountId = collectionProfile.customer_id ?: "",
                    message_link = collectionProfile.profile?.message_link,
                    message = collectionProfile.profile?.message,
                    link_intent = collectionProfile.profile?.link_intent,
                    qr_intent = collectionProfile.profile?.qr_intent,
                    show_image = collectionProfile.profile?.show_image ?: false,
                    isSupplier = false,

                    // these properties are added for single list feature
                    linkVpa = collectionProfile.destination?.upiVpa,
                    paymentAddress = collectionProfile.destination?.paymentAddress,
                    type = collectionProfile.destination?.type,
                    mobile = collectionProfile.destination?.mobile,
                    name = collectionProfile.destination?.name,
                    upiVpa = collectionProfile.destination?.upiVpa,
                    fromMerchantPaymentLink = collectionProfile.profile?.from_merchant_payment_link,
                    fromMerchantUpiIntent = collectionProfile.profile?.from_merchant_upi_intent,
                    linkId = collectionProfile.profile?.linkId,
                    googlePayEnabled = collectionProfile.gpay_enabled ?: true,
                    paymentIntent = collectionProfile.profile?.paymentIntent ?: false,
                    cashbackEligible = collectionProfile.cashbackEligible ?: false,
                )
            }

            override fun doBackward(b: CollectionCustomerProfile): ApiMessages.CustomerCollectionProfileResponse {
                throw RuntimeException("illegal operation: cannot convert Collection domain entity to api entity")
            }
        }

    var COLLECTION_SUPPLIER_PROFILE: Converter<ApiMessages.SupplierCollectionProfileResponse, CollectionCustomerProfile> =
        object :
            Converter<ApiMessages.SupplierCollectionProfileResponse, CollectionCustomerProfile>() {
            override fun doForward(supplier: ApiMessages.SupplierCollectionProfileResponse): CollectionCustomerProfile {
                return CollectionCustomerProfile(
                    accountId = supplier.accountId ?: "",
                    message_link = supplier.supplierProfile?.messageLink,
                    link_intent = supplier.supplierProfile?.linkIntent,
                    isSupplier = true,
                    linkVpa = supplier.destination?.upiVpa,
                    paymentAddress = supplier.destination?.paymentAddress,
                    type = supplier.destination?.type,
                    mobile = supplier.destination?.mobile,
                    name = supplier.destination?.name,
                    upiVpa = supplier.destination?.upiVpa,
                    linkId = supplier.supplierProfile?.linkId,
                    destinationUpdateAllowed = supplier.destinationUpdateAllowed ?: true,
                )
            }

            override fun doBackward(b: CollectionCustomerProfile): ApiMessages.SupplierCollectionProfileResponse {
                throw java.lang.RuntimeException("illegal operation: cannot convert Collection domain entity to api entity ")
            }
        }

    var ONLINE_PAYMENT_MAPPER: Converter<ApiMessages.CollectionOnlinePaymentApi, CollectionOnlinePayment> =
        object : Converter<ApiMessages.CollectionOnlinePaymentApi, CollectionOnlinePayment>() {
            override fun doForward(api: ApiMessages.CollectionOnlinePaymentApi): CollectionOnlinePayment {
                return CollectionOnlinePayment(
                    id = api.id,
                    createdTime = api.createdTime,
                    updatedTime = api.updatedTime,
                    status = api.status,
                    merchantId = api.merchantId,
                    accountId = api.accountId ?: "",
                    amount = api.amount,
                    paymentId = api.paymentId ?: "",
                    payoutId = api.payoutId ?: "",
                    paymentSource = api.paymentSource ?: "",
                    paymentMode = api.paymentMode ?: "",
                    type = api.type,
                    errorCode = api.errorCode ?: "",
                    errorDescription = api.errorDescription ?: "",
                    read = api.tags?.isViewed ?: false
                )
            }

            override fun doBackward(obj: CollectionOnlinePayment): ApiMessages.CollectionOnlinePaymentApi {
                return ApiMessages.CollectionOnlinePaymentApi(
                    id = obj.id,
                    createdTime = obj.createdTime,
                    updatedTime = obj.updatedTime,
                    status = obj.status,
                    merchantId = obj.merchantId ?: "",
                    accountId = obj.accountId,
                    amount = obj.amount,
                    paymentId = obj.paymentId,
                    payoutId = obj.payoutId,
                    paymentSource = obj.paymentSource,
                    paymentMode = obj.paymentMode,
                    type = obj.type ?: "",
                    errorCode = obj.errorCode,
                    errorDescription = obj.errorDescription,
                )
            }
        }

    fun convert(merchantProfile: ApiMessages.PredictedMerchantCollectionProfileResponse): CollectionMerchantProfile {
        return CollectionMerchantProfile(
            merchant_id = merchantProfile.merchant_id,
            name = merchantProfile.destination.name,
            payment_address = merchantProfile.destination.paymentAddress,
            type = merchantProfile.destination.type,
            merchant_vpa = "" // In Predicted API we don't get merchant_vpa in future maybe we can remove it
        )
    }
}
