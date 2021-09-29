package `in`.okcredit.frontend

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import tech.okcredit.contacts.contract.model.Contact

internal object FrontendTestData {

    val CURRENT_TIME = DateTime(2018, 10, 3, 0, 0, 0)

    val MERCHANT = Business(
        "abc",
        "abc Store",
        "8888888888",
        "",
        "",
        0.0,
        0.0,
        "",
        "",
        "",
        CURRENT_TIME,
        null,
        false,
        null,
        null,
        false,
        null,
        null
    )

    val CUSTOMER = Customer(
        "1234",
        0,
        Transaction.CREDIT,
        "9999999999",
        "Anjal",
        DateTime(2018, 10, 2, 0, 0, 0),
        100L,
        2,
        0,
        DateTime(2018, 10, 2, 0, 0, 0),
        DateTime(2018, 10, 2, 0, 0, 0),
        "http://okcredit.in",
        null,
        null,
        null,
        0L,
        DateTime(2018, 10, 2, 0, 0, 0),
        true,
        DateTime(2018, 10, 2, 0, 0, 0),
        false,
        "en",
        "sms",
        false,
        false,
        null,
        false,
        false, false, DateTime(2018, 10, 2, 0, 0, 0), false, 0, 0
    )

    // credit without note and image
    val TRANSACTION1 = Transaction(
        "xyz",
        Transaction.CREDIT,
        CUSTOMER.id,
        "",
        1000,
        null,
        null,
        CURRENT_TIME,
        false,
        false,
        null,
        false,
        CURRENT_TIME,
        CURRENT_TIME,
        true,
        false,
        false,
        "",
        "",
        1,
        Transaction.DEAFULT_CATERGORY,
        false,
        CURRENT_TIME
    )

    // credit with note and image
    val TRANSACTION2 = Transaction(
        "xyz",
        Transaction.CREDIT,
        CUSTOMER.id,
        "",
        13223,
        arrayListOf(),
        "this is a note",
        CURRENT_TIME,
        false,
        false,
        null,
        false,
        CURRENT_TIME,
        CURRENT_TIME,
        true,
        false,
        false,
        "",
        "",
        1,
        Transaction.DEAFULT_CATERGORY,
        true,
        CURRENT_TIME
    )

    // payment
    val TRANSACTION3 = Transaction(
        "xyz",
        Transaction.PAYMENT,
        CUSTOMER.id,
        "",
        13223,
        arrayListOf(),
        "this is a note",
        CURRENT_TIME,
        false,
        false,
        null,
        false,
        CURRENT_TIME,
        CURRENT_TIME,
        true,
        false,
        false,
        "",
        "",
        1,
        Transaction.DEAFULT_CATERGORY,
        false,
        CURRENT_TIME
    )

    val CONTACT1 = Contact(
        "31234",
        "XYZ",
        "9876543210",
        "",
        false,
        1590578649L,
        false,
        1
    )

    val CONTACT2 = Contact(
        "232",
        "Xyz",
        "",
        "",
        false,
        1590578649L,
        false,
        1
    )

    val CONTACT3 = Contact(
        "1234",
        "xyz",
        "9999999999",
        "",
        false,
        1590578649L,
        false,
        1
    )

    val referralTarget = ReferralTargetBanner(
        id = "1234",
        referrerMerchantPrize = 2500L,
        referralMerchantPrize = 2500L,
        isActivated = listOf(),
        title = "add_transaction",
        description = "Get Rs. 15/- directly in your bank account.",
        icon = "Add first Transaction",
        deepLink = "https://okcredit.app/merchant/v1/home/add_customer",
        bannerPlace = listOf(1, 0),
        howDoesItWorks = ""
    )

    val SUPPLIER = Supplier(
        "supplier_id",
        registered = true,
        deleted = false,
        createTime = DateTime(),
        txnStartTime = 1L,
        name = "supplier_name",
        mobile = "9999999999",
        address = "address",
        profileImage = "profile_image",
        balance = 1,
        newActivityCount = 0L,
        lastActivityTime = DateTime(),
        lastViewTime = DateTime(),
        txnAlertEnabled = true,
        lang = "eng",
        syncing = true,
        lastSyncTime = DateTime(),
        addTransactionRestricted = true,
        state = Supplier.ACTIVE,
        blockedBySupplier = false,
        restrictContactSync = false
    )
}
