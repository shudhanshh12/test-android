package `in`.okcredit.merchant.collection.store.database

import `in`.okcredit.collection.contract.ApiMessages
import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection.contract.CustomerAdditionalInfo
import `in`.okcredit.collection.contract.KycExternalInfo
import com.google.common.base.Converter

object DbEntityMapper {

    fun COLLECTION(businessId: String): Converter<`in`.okcredit.collection.contract.Collection, Collection> =
        object :
            Converter<`in`.okcredit.collection.contract.Collection, Collection>() {
            override fun doForward(collection: `in`.okcredit.collection.contract.Collection): Collection {
                return Collection(
                    id = collection.id,
                    create_time = collection.create_time,
                    update_time = collection.update_time,
                    status = collection.status,
                    payment_link = collection.payment_link ?: "",
                    amount_requested = collection.amount_requested,
                    amount_collected = collection.amount_collected,
                    fee = collection.fee,
                    expire_time = collection.expire_time,
                    customer_id = collection.customer_id,
                    discount = collection.discount,
                    fee_category = collection.fee_category,
                    settlement_category = collection.settlement_category,
                    lastSyncTime = collection.lastSyncTime,
                    lastViewTime = collection.lastViewTime,
                    merchantName = collection.merchantName,
                    paymentOriginName = collection.paymentOriginName,
                    paymentId = collection.paymentId,
                    errorCode = collection.errorCode,
                    errorDescription = collection.errorDescription,
                    blindPay = collection.blindPay,
                    cashbackGiven = collection.cashbackGiven,
                    businessId = businessId
                )
            }

            override fun doBackward(collection: Collection): `in`.okcredit.collection.contract.Collection {
                return `in`.okcredit.collection.contract.Collection(
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
                    lastSyncTime = collection.lastSyncTime,
                    lastViewTime = collection.lastViewTime,
                    merchantName = collection.merchantName,
                    paymentOriginName = collection.paymentOriginName,
                    paymentId = collection.paymentId,
                    errorCode = collection.errorCode,
                    errorDescription = collection.errorDescription,
                    blindPay = collection.blindPay,
                    cashbackGiven = collection.cashbackGiven,
                )
            }
        }

    var collectionMerchantProfile: Converter<`in`.okcredit.collection.contract.CollectionMerchantProfile, CollectionProfile> =
        object :
            Converter<`in`.okcredit.collection.contract.CollectionMerchantProfile, CollectionProfile>() {
            override fun doForward(collectionMerchantProfile: `in`.okcredit.collection.contract.CollectionMerchantProfile): CollectionProfile {
                return CollectionProfile(
                    merchant_id = collectionMerchantProfile.merchant_id,
                    name = collectionMerchantProfile.name,
                    payment_address = collectionMerchantProfile.payment_address,
                    type = collectionMerchantProfile.type,
                    merchant_vpa = collectionMerchantProfile.merchant_vpa,
                    limit_type = collectionMerchantProfile.limitType,
                    kyc_limit = collectionMerchantProfile.limit,
                    remaining_limit = collectionMerchantProfile.remainingLimit,
                    merchant_qr_enabled = collectionMerchantProfile.merchantQrEnabled,
                )
            }

            override fun doBackward(collectionProfile: CollectionProfile): `in`.okcredit.collection.contract.CollectionMerchantProfile {
                return `in`.okcredit.collection.contract.CollectionMerchantProfile(
                    merchant_id = collectionProfile.merchant_id,
                    name = collectionProfile.name,
                    payment_address = collectionProfile.payment_address,
                    type = collectionProfile.type,
                    merchant_vpa = collectionProfile.merchant_vpa,
                    limit = collectionProfile.kyc_limit,
                    limitType = collectionProfile.limit_type,
                    remainingLimit = collectionProfile.remaining_limit,
                    merchantQrEnabled = collectionProfile.merchant_qr_enabled,
                )
            }
        }

    fun COLLECTION_CUSTOMER_PROFILE(businessId: String): Converter<`in`.okcredit.collection.contract.CollectionCustomerProfile, CustomerCollectionProfile> =
        object :
            Converter<`in`.okcredit.collection.contract.CollectionCustomerProfile, CustomerCollectionProfile>() {
            override fun doForward(collectionProfile: `in`.okcredit.collection.contract.CollectionCustomerProfile): CustomerCollectionProfile {
                return CustomerCollectionProfile(
                    customerId = collectionProfile.accountId,
                    messageLink = collectionProfile.message_link,
                    message = collectionProfile.message,
                    qrIntent = collectionProfile.qr_intent,
                    showImage = collectionProfile.show_image,
                    linkId = collectionProfile.linkId,
                    googlePayEnabled = collectionProfile.googlePayEnabled,
                    paymentIntent = collectionProfile.paymentIntent,
                    destinationUpdateAllowed = collectionProfile.destinationUpdateAllowed,
                    cashbackEligible = collectionProfile.cashbackEligible,
                    businessId = businessId
                )
            }

            override fun doBackward(collectionProfile: CustomerCollectionProfile): `in`.okcredit.collection.contract.CollectionCustomerProfile {
                return `in`.okcredit.collection.contract.CollectionCustomerProfile(
                    accountId = collectionProfile.customerId,
                    message_link = collectionProfile.messageLink,
                    message = collectionProfile.message,
                    qr_intent = collectionProfile.qrIntent,
                    show_image = collectionProfile.showImage,
                    linkId = collectionProfile.linkId,
                    googlePayEnabled = collectionProfile.googlePayEnabled,
                    paymentIntent = collectionProfile.paymentIntent,
                    destinationUpdateAllowed = collectionProfile.destinationUpdateAllowed,
                    cashbackEligible = collectionProfile.cashbackEligible,
                )
            }
        }

    fun SUPPLIER_COLLECTION_PROFILE(businessId: String): Converter<`in`.okcredit.collection.contract.CollectionCustomerProfile, SupplierCollectionProfile> =
        object :
            Converter<`in`.okcredit.collection.contract.CollectionCustomerProfile, SupplierCollectionProfile>() {
            override fun doForward(collectionProfile: `in`.okcredit.collection.contract.CollectionCustomerProfile): SupplierCollectionProfile {
                return SupplierCollectionProfile(
                    accountId = collectionProfile.accountId,
                    messageLink = collectionProfile.message_link,
                    name = collectionProfile.name,
                    type = collectionProfile.type,
                    paymentAddress = collectionProfile.paymentAddress,
                    linkId = collectionProfile.linkId,
                    destinationUpdateAllowed = collectionProfile.destinationUpdateAllowed,
                    businessId = businessId
                )
            }

            override fun doBackward(collectionProfile: SupplierCollectionProfile): `in`.okcredit.collection.contract.CollectionCustomerProfile {
                return `in`.okcredit.collection.contract.CollectionCustomerProfile(
                    accountId = collectionProfile.accountId,
                    message_link = collectionProfile.messageLink,
                    name = collectionProfile.name,
                    type = collectionProfile.type,
                    paymentAddress = collectionProfile.paymentAddress,
                    linkId = collectionProfile.linkId,
                    destinationUpdateAllowed = collectionProfile.destinationUpdateAllowed,
                )
            }
        }

    fun COLLECTION_SHARE_INFO(businessId: String): Converter<`in`.okcredit.collection.contract.CollectionShareInfo, CollectionShareInfo> =
        object :
            Converter<`in`.okcredit.collection.contract.CollectionShareInfo, CollectionShareInfo>() {
            override fun doForward(collectionProfile: `in`.okcredit.collection.contract.CollectionShareInfo): CollectionShareInfo {
                return CollectionShareInfo(
                    collectionProfile.customer_id,
                    collectionProfile.shared_time,
                    businessId
                )
            }

            override fun doBackward(collectionProfile: CollectionShareInfo): `in`.okcredit.collection.contract.CollectionShareInfo {
                return `in`.okcredit.collection.contract.CollectionShareInfo(
                    collectionProfile.customer_id,
                    collectionProfile.shared_time
                )
            }
        }

    var KYC_EXTERNAL_MAPPER: Converter<KycExternalInfo, KycExternalEntity> =
        object :
            Converter<KycExternalInfo, KycExternalEntity>() {
            override fun doForward(info: KycExternalInfo): KycExternalEntity {
                return KycExternalEntity(
                    merchantId = info.merchantId,
                    kyc = info.kyc,
                    upiDailyLimit = info.upiDailyLimit,
                    nonUpiDailyLimit = info.nonUpiDailyLimit,
                    upiDailyTransactionAmount = info.upiDailyTransactionAmount,
                    nonUpiDailyTransactionAmount = info.nonUpiDailyTransactionAmount,
                    category = info.category
                )
            }

            override fun doBackward(it: KycExternalEntity): KycExternalInfo {
                return KycExternalInfo(
                    merchantId = it.merchantId,
                    kyc = it.kyc,
                    upiDailyLimit = it.upiDailyLimit,
                    nonUpiDailyLimit = it.nonUpiDailyLimit,
                    upiDailyTransactionAmount = it.upiDailyTransactionAmount,
                    nonUpiDailyTransactionAmount = it.nonUpiDailyTransactionAmount,
                    category = it.category
                )
            }
        }

    fun ONLINE_PAYMENTS_MAPPER(businessId: String): Converter<CollectionOnlinePayment, CollectionOnlinePaymentEntity> =
        object : Converter<CollectionOnlinePayment, CollectionOnlinePaymentEntity>() {
            override fun doForward(obj: CollectionOnlinePayment): CollectionOnlinePaymentEntity {
                return CollectionOnlinePaymentEntity(
                    id = obj.id,
                    createdTime = obj.createdTime,
                    updatedTime = obj.updatedTime,
                    status = obj.status,
                    accountId = obj.accountId,
                    amount = obj.amount,
                    paymentId = obj.paymentId,
                    merchantId = obj.merchantId ?: "",
                    payoutId = obj.payoutId ?: "",
                    paymentSource = obj.paymentSource ?: "",
                    paymentMode = obj.paymentMode ?: "",
                    type = obj.type ?: "",
                    read = obj.read,
                    errorCode = obj.errorCode,
                    errorDescription = obj.errorDescription,
                    businessId = businessId
                )
            }

            override fun doBackward(db: CollectionOnlinePaymentEntity): CollectionOnlinePayment {
                return CollectionOnlinePayment(
                    id = db.id,
                    createdTime = db.createdTime,
                    updatedTime = db.updatedTime,
                    status = db.status,
                    merchantId = db.merchantId,
                    accountId = db.accountId,
                    amount = db.amount,
                    paymentId = db.paymentId,
                    payoutId = db.payoutId,
                    paymentSource = db.paymentSource,
                    paymentMode = db.paymentMode,
                    type = db.type,
                    read = db.read,
                    errorCode = db.errorCode,
                    errorDescription = db.errorDescription,
                )
            }
        }

    fun convertToCustomerAdditionalInfoEntity(targetedReferralResponse: ApiMessages.TargetedReferralResponse, businessId: String): List<CustomerAdditionalInfoEntity> {
        val customerAdditionalInfo = arrayListOf<CustomerAdditionalInfoEntity>()
        targetedReferralResponse.customers.forEach {
            customerAdditionalInfo.add(
                CustomerAdditionalInfoEntity(
                    id = it.customerId,
                    link = it.link,
                    status = it.status,
                    amount = targetedReferralResponse.amount,
                    message = targetedReferralResponse.message,
                    youtubeLink = targetedReferralResponse.youtubeLink,
                    customerMerchantId = it.customerMerchantId,
                    businessId = businessId
                )
            )
        }
        return customerAdditionalInfo
    }

    fun convertToCustomerAdditionalInfo(entity: CustomerAdditionalInfoEntity): CustomerAdditionalInfo {
        return CustomerAdditionalInfo(
            id = entity.id,
            link = entity.link,
            status = entity.status,
            amount = entity.amount,
            message = entity.message,
            youtubeLink = entity.youtubeLink,
            customerMerchantId = entity.customerMerchantId,
            ledgerSeen = entity.ledgerSeen
        )
    }
}
