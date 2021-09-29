package `in`.okcredit.merchant.collection

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.ApiMessages
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.CustomerAdditionalInfo
import `in`.okcredit.merchant.collection.store.database.CollectionProfile
import `in`.okcredit.merchant.collection.store.database.CollectionShareInfo
import `in`.okcredit.merchant.collection.store.database.CustomerCollectionProfile
import org.joda.time.DateTime

internal object CollectionTestData {

    val CURRENT_TIME = DateTime(2018, 10, 3, 0, 0, 0)

    val BUSINESS_ID = "business-id"

    val API_ORIGIN = ApiMessages.Origin(
        mobile = "8882946897",
        name = "Bali",
        payment_address = "8882946897@ybl",
        type = "type"
    )

    val API_PAYMENT = ApiMessages.Payment(
        provider = "provider",

        id = "cccc",

        UTR = "utr",

        reference = "reference",

        status = 1,

        amount = 1002,

        fee = 87484,

        tax = 7377,

        payment_link = "payment_link",

        description = "description",

        request_type = 1,

        labels = mapOf("pyaar" to "h", "ya" to "saja"),

        origin = API_ORIGIN
    )

    val API_DESTINATION = ApiMessages.Destination(
        mobile = "8882946897",

        name = "Bali",

        paymentAddress = "8882946897@ybl",

        type = "type",

        upiVpa = null,
    )

    val API_PROFILE = ApiMessages.Profile(
        customer_id = "abc",

        message_link = null,

        message = null,

        link_intent = null,

        qr_intent = null,

        show_image = false,

        from_merchant_payment_link = null,

        from_merchant_upi_intent = null,

        linkId = "link_id",

        paymentIntent = false
    )

    val COLLECTION_MERCHANT_PROFILE = CollectionMerchantProfile(
        merchant_id = "merchant_id",
        name = "name",
        payment_address = "payment",
        type = "type",
        merchant_vpa = "vpa"
    )

    val CUSTOMER_COLLECTION_PROFILE_1 = `in`.okcredit.collection.contract.CollectionCustomerProfile(
        accountId = "account_id_1",
        qr_intent = "qr_intent",
        link_intent = "link"
    )

    val CUSTOMER_COLLECTION_PROFILE_2 = `in`.okcredit.collection.contract.CollectionCustomerProfile(
        accountId = "account_id_2",
        qr_intent = "qr_intent",
        link_intent = "link"
    )

    val SUPPLIER_COLLECTION_PROFILE_1 = `in`.okcredit.collection.contract.CollectionCustomerProfile(
        accountId = "supplier_account_id_1",
        paymentAddress = "paymentAddress",
        linkId = "link"
    )

    val SUPPLIER_COLLECTION_PROFILE_2 = `in`.okcredit.collection.contract.CollectionCustomerProfile(
        accountId = "supplier_account_id_2",
        paymentAddress = "paymentAddress_2",
        linkId = "link"
    )

    val API_COLLECTION_CUSTOMER_PROFILE = ApiMessages.CustomerCollectionProfileResponse(
        customer_id = "abc",

        profile = API_PROFILE,

        destination = API_DESTINATION,

        gpay_enabled = true,

        cashbackEligible = false,

        destinationUpdateAllowed = false,
    )

    val API_SUPPLIER_PROFILE = ApiMessages.SupplierProfile(
        messageLink = "abc",

        linkIntent = null,

        linkVpa = null,

        linkId = null
    )

    val API_SUPPLIER_DESTINATION = ApiMessages.SupplierDestination(
        mobile = "8882946897",

        name = "Bali",

        paymentAddress = "8882946897@ybl",

        type = "type",

        upiVpa = null,
    )

    val API_SUPPLIER_COLLECTION_PROFILE = ApiMessages.SupplierCollectionProfileResponse(
        accountId = "abc",

        supplierProfile = API_SUPPLIER_PROFILE,

        destination = API_SUPPLIER_DESTINATION,

        destinationUpdateAllowed = false,
    )

    val API_ONLINE_COLLECTION = ApiMessages.CollectionOnlinePaymentApi(
        id = "id",
        createdTime = CURRENT_TIME,
        updatedTime = CURRENT_TIME,
        status = 5,
        merchantId = "merchantId",
        accountId = "accountId",
        amount = 10.0,
        paymentId = "paymentId",
        payoutId = "payoutId",
        paymentSource = "upi@asd",
        paymentMode = "upi",
        type = "QR"
    )

    val API_GET_ONLINE_RESPONSE = ApiMessages.GetOnlinePaymentResponse(
        listOf(
            API_ONLINE_COLLECTION,
            API_ONLINE_COLLECTION,
            API_ONLINE_COLLECTION

        )
    )

    val COLLECTION1 = Collection(
        id = "id_1",
        create_time = CURRENT_TIME,
        update_time = CURRENT_TIME,
        status = 1,
        payment_link = "payment_link",
        amount_requested = 1000L,
        amount_collected = 1000L,
        fee = 1000L,
        expire_time = null,
        customer_id = "customer_id",
        discount = null,
        fee_category = 1,
        settlement_category = 1,
        lastSyncTime = CURRENT_TIME,
        lastViewTime = null,
        merchantName = null,
        paymentOriginName = null,
        paymentId = null,
        blindPay = false,
    )

    val COLLECTION2 = Collection(
        id = "id_2",
        create_time = CURRENT_TIME,
        update_time = CURRENT_TIME.plusDays(1),
        status = 1,
        payment_link = "payment_link_2",
        amount_requested = 1000L,
        amount_collected = 1000L,
        fee = 1000L,
        expire_time = null,
        customer_id = "customer_id",
        discount = null,
        fee_category = 1,
        settlement_category = 1,
        lastSyncTime = CURRENT_TIME,
        lastViewTime = null,
        merchantName = null,
        paymentOriginName = null,
        paymentId = null,
        blindPay = true,
    )

    val COLLECTION_ENTITY1 = `in`.okcredit.merchant.collection.store.database.Collection(
        id = "id_1",
        create_time = CURRENT_TIME,
        update_time = CURRENT_TIME,
        status = 1,
        payment_link = "payment_link",
        amount_requested = 1000L,
        amount_collected = 1000L,
        fee = 1000L,
        expire_time = null,
        customer_id = "customer_id_1",
        discount = null,
        fee_category = 1,
        settlement_category = 1,
        lastSyncTime = CURRENT_TIME,
        lastViewTime = null,
        merchantName = null,
        paymentOriginName = null,
        paymentId = null,
        errorCode = "",
        errorDescription = "",
        businessId = BUSINESS_ID
    )

    val COLLECTION_ENTITY2 = `in`.okcredit.merchant.collection.store.database.Collection(
        id = "id_2",
        create_time = CURRENT_TIME,
        update_time = CURRENT_TIME,
        status = 1,
        payment_link = "payment_link",
        amount_requested = 1000L,
        amount_collected = 1000L,
        fee = 1000L,
        expire_time = null,
        customer_id = "customer_id_2",
        discount = null,
        fee_category = 1,
        settlement_category = 1,
        lastSyncTime = CURRENT_TIME,
        lastViewTime = null,
        merchantName = null,
        paymentOriginName = null,
        paymentId = null,
        errorCode = "",
        errorDescription = "",
        businessId = BUSINESS_ID
    )

    val COLLECTION_CUSTOMER_PROFILE_ENTITY1 = CustomerCollectionProfile(
        customerId = "id",
        messageLink = "",
        message = "",
        qrIntent = "",
        showImage = false,
        linkId = "",
        googlePayEnabled = false,
        paymentIntent = false,
        businessId = BUSINESS_ID
    )

    val COLLECTION_CUSTOMER_PROFILE_ENTITY2 = CustomerCollectionProfile(
        customerId = "id2",
        messageLink = "",
        message = "",
        qrIntent = "",
        showImage = false,
        linkId = "",
        googlePayEnabled = false,
        paymentIntent = false,
        businessId = BUSINESS_ID
    )

    val COLLECTION_PROFILE_ENTITY1 = CollectionProfile(
        merchant_id = "businessId",
        name = "",
        payment_address = "",
        type = "",
        merchant_vpa = "",
    )

    val COLLECTION_SHARE_INFO = CollectionShareInfo(
        customer_id = "id",
        shared_time = CURRENT_TIME,
        businessId = BUSINESS_ID
    )
    val COLLECTION_SHARE_INFO2 = CollectionShareInfo(
        customer_id = "id_2",
        shared_time = CURRENT_TIME,
        businessId = BUSINESS_ID
    )

    val PAYMENT_ID = "1245sdgasda3242"

    val SHARE_LINK = "http:://okcredit.testsharelink"

    val BLIND_PAY_SHARE_LINK_WITH_PAYMENT_ID_REQUEST =
        ApiMessages.BlindPayShareLinkRequest(paymentId = PAYMENT_ID)

    val BLIND_PAY_SHARE_LINK_WITH_PAYMENT_ID_RESPONSE =
        ApiMessages.BlindPayShareLinkResponse(shareLink = SHARE_LINK)

    val CUSTOMER_ADDITIONAL_INFO = CustomerAdditionalInfo(
        id = "1234",
        link = "link",
        status = 1,
        amount = 1,
        message = "msg",
        youtubeLink = "youtube_link",
        customerMerchantId = "merchant_id",
        ledgerSeen = false
    )

    var CUSTOMER = Customer(
        id = "1234",
        status = 1,
        mobile = "9999999999",
        description = "Customer 1",
        createdAt = DateTime(2018, 10, 2, 0, 0, 0),
        txnStartTime = 100L,
        balanceV2 = 2,
        transactionCount = 0,
        lastActivity = DateTime(2018, 10, 2, 0, 0, 0),
        lastPayment = DateTime(2018, 10, 2, 0, 0, 0),
        accountUrl = "http://okcredit.in",
        profileImage = null,
        address = null,
        email = null,
        newActivityCount = 0L,
        lastViewTime = DateTime(2018, 10, 2, 0, 0, 0),
        registered = true,
        lastBillDate = DateTime(2018, 10, 2, 0, 0, 0),
        txnAlertEnabled = false,
        lang = "en",
        reminderMode = "sms",
        isLiveSales = false,
        addTransactionPermissionDenied = false,
        state = null,
        blockedByCustomer = false,
        restrictContactSync = false,
        dueActive = false,
        dueInfo_activeDate = DateTime(2018, 10, 2, 0, 0, 0),
        customDateSet = false,
        lastActivityMetaInfo = 0,
        lastAmount = 0
    )
}
