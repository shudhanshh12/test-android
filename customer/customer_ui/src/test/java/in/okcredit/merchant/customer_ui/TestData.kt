package `in`.okcredit.merchant.customer_ui

import `in`.okcredit.backend._offline.model.DueInfo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.*
import `in`.okcredit.merchant.customer_ui.data.server.model.request.DayOfWeek
import `in`.okcredit.merchant.customer_ui.data.server.model.response.Subscription
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionStatus
import `in`.okcredit.merchant.suppliercredit.Supplier
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import tech.okcredit.contacts.contract.model.Contact

internal object TestData {

    val CURRENT_TIME = DateTime(2020, 10, 3, 0, 0, 0)

    val COLLECTION: Collection = Collection(
        id = "collection_id",
        create_time = CURRENT_TIME,
        update_time = CURRENT_TIME,
        customer_id = "1234",
        status = 2
    )

    val COLLECTION_2: Collection = Collection(
        id = "collection_id_2",
        create_time = CURRENT_TIME,
        update_time = CURRENT_TIME,
        customer_id = "1234",
        status = 5
    )

    val BUSINESS_ID = "businessId"
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

    var CUSTOMER = Customer(
        id = "1234",
        Customer.CustomerSyncStatus.CLEAN.code,
        status = Transaction.CREDIT,
        mobile = "9999999999",
        description = "Anjal",
        createdAt = DateTime(2018, 10, 2, 0, 0, 0),
        txnStartTime = 100L,
        balanceV2 = -100,
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
        lastAmount = 0,
        lastReminderSendTime = DateTime(2018, 10, 2, 0, 0, 0),
    )

    var CUSTOMER_2 = Customer(
        id = "CUSTOMER_2",
        customerSyncStatus = Customer.CustomerSyncStatus.CLEAN.code,
        status = Transaction.CREDIT,
        mobile = "9999999999",
        description = "CUSTOMER_2",
        createdAt = DateTime(2018, 10, 2, 0, 0, 0),
        txnStartTime = 100L,
        balanceV2 = -200,
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
        lastAmount = 0,
        lastReminderSendTime = DateTime(2018, 10, 2, 0, 0, 0),
    )

    var CUSTOMER_3 = Customer(
        id = "CUSTOMER_3",
        customerSyncStatus = Customer.CustomerSyncStatus.CLEAN.code,
        status = Transaction.CREDIT,
        mobile = "9999999999",
        description = "CUSTOMER_3",
        createdAt = DateTime(2018, 10, 2, 0, 0, 0),
        txnStartTime = 100L,
        balanceV2 = -400,
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
        lastAmount = 0,
        lastReminderSendTime = DateTime(2018, 10, 2, 0, 0, 0),
    )

    var CUSTOMER_4 = Customer(
        id = "CUSTOMER_4",
        customerSyncStatus = Customer.CustomerSyncStatus.CLEAN.code,
        status = Transaction.CREDIT,
        mobile = "9999999999",
        description = "CUSTOMER_4",
        createdAt = DateTime(2018, 10, 2, 0, 0, 0),
        txnStartTime = 100L,
        balanceV2 = -400,
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
        lastAmount = 0,
        lastReminderSendTime = DateTime(2018, 10, 2, 0, 0, 0),
    )

    // credit without note and image
    val TRANSACTION1 = Transaction(
        id = "xyz",
        type = Transaction.CREDIT,
        customerId = CUSTOMER.id,
        collectionId = "",
        amountV2 = 1000,
        receiptUrl = null,
        note = null,
        createdAt = CURRENT_TIME,
        isOnboarding = false,
        isDeleted = false,
        deleteTime = null,
        isDirty = false,
        billDate = DateTime(100000002),
        updatedAt = CURRENT_TIME,
        isSmsSent = true,
        isCreatedByCustomer = false,
        isDeletedByCustomer = false,
        inputType = "",
        voiceId = "",
        transactionState = 1,
        transactionCategory = Transaction.DEAFULT_CATERGORY,
        amountUpdated = false,
        amountUpdatedAt = CURRENT_TIME
    )

    // credit with note and image
    val TRANSACTION2 = Transaction(
        id = "xyz",
        type = Transaction.CREDIT,
        customerId = CUSTOMER.id,
        collectionId = "",
        amountV2 = 13223,
        receiptUrl = arrayListOf(),
        note = "this is a note",
        createdAt = CURRENT_TIME,
        isOnboarding = false,
        isDeleted = false,
        deleteTime = null,
        isDirty = false,
        billDate = DateTime(100000001),
        updatedAt = CURRENT_TIME,
        isSmsSent = true,
        isCreatedByCustomer = false,
        isDeletedByCustomer = false,
        inputType = "",
        voiceId = "",
        transactionState = 1,
        transactionCategory = Transaction.DEAFULT_CATERGORY,
        amountUpdated = true,
        amountUpdatedAt = CURRENT_TIME
    )

    // payment
    val TRANSACTION3 = Transaction(
        id = "xyz",
        type = Transaction.PAYMENT,
        customerId = CUSTOMER.id,
        collectionId = "",
        amountV2 = 13223,
        receiptUrl = arrayListOf(),
        note = "this is a note",
        createdAt = CURRENT_TIME,
        isOnboarding = false,
        isDeleted = false,
        deleteTime = null,
        isDirty = false,
        billDate = DateTime(100000000),
        updatedAt = CURRENT_TIME,
        isSmsSent = true,
        isCreatedByCustomer = false,
        isDeletedByCustomer = false,
        inputType = "",
        voiceId = "",
        transactionState = 1,
        transactionCategory = Transaction.DEAFULT_CATERGORY,
        amountUpdated = false,
        amountUpdatedAt = CURRENT_TIME
    )

    val ONLINE_TRANSACTION = Transaction(
        id = "xyz",
        type = Transaction.PAYMENT,
        customerId = CUSTOMER.id,
        collectionId = "3343324",
        amountV2 = 13223,
        receiptUrl = arrayListOf(),
        note = "this is a note",
        createdAt = CURRENT_TIME,
        isOnboarding = false,
        isDeleted = false,
        deleteTime = null,
        isDirty = false,
        billDate = DateTime(100000000),
        updatedAt = CURRENT_TIME,
        isSmsSent = true,
        isCreatedByCustomer = false,
        isDeletedByCustomer = false,
        inputType = "",
        voiceId = "",
        transactionState = 1,
        transactionCategory = Transaction.DEAFULT_CATERGORY,
        amountUpdated = false,
        amountUpdatedAt = CURRENT_TIME
    )

    val DELETED_TRANSACTION = Transaction(
        "xyz",
        Transaction.PAYMENT,
        CUSTOMER.id,
        "",
        13223,
        arrayListOf(),
        "this is a note",
        CURRENT_TIME,
        false,
        true,
        DateTime(100000000),
        false,
        DateTime(100000000),
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

    val CREATED_BY_CUSTOMER_TRANSACTION = Transaction(
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
        DateTime(100000000),
        CURRENT_TIME,
        true,
        true,
        false,
        "",
        "",
        1,
        Transaction.DEAFULT_CATERGORY,
        false,
        CURRENT_TIME
    )

    val DAILY_SUBSCRIPTION = Subscription(
        id = "1",
        amount = 1000,
        frequency = SubscriptionFrequency.DAILY.value,
        name = "Daily Subscription",
        status = SubscriptionStatus.ACTIVE.value,
        endDate = 102320130123,
        startDate = 102320130000,
        createTime = 102320130123,
        updateTime = 102320130123,
        nextSchedule = null,
        accountId = CUSTOMER.id,
        week = null
    )

    val WEEKLY_SUBSCRIPTION = Subscription(
        id = "2",
        amount = 100,
        frequency = SubscriptionFrequency.WEEKLY.value,
        name = "Daily Subscription",
        status = SubscriptionStatus.ACTIVE.value,
        endDate = null,
        startDate = 102320130123,
        createTime = 102320130123,
        updateTime = 102320130123,
        nextSchedule = null,
        accountId = CUSTOMER.id,
        week = listOf(DayOfWeek.SUNDAY.value, DayOfWeek.FRIDAY.value, DayOfWeek.WEDNESDAY.value)
    )

    val MONTHLY_SUBSCRIPTION = Subscription(
        id = "3",
        amount = 100,
        frequency = SubscriptionFrequency.MONTHLY.value,
        name = "Daily Subscription",
        status = SubscriptionStatus.ACTIVE.value,
        endDate = null,
        startDate = 102320130123,
        createTime = 102320130123,
        updateTime = 102320130123,
        nextSchedule = null,
        accountId = CUSTOMER.id,
        week = null
    )

    val DUE_INFO = DueInfo(
        customerId = "customer_id",
        isDueActive = true,
        activeDate = DateTime.now().plusDays(3),
        isCustomDateSet = false,
        isAutoGenerated = true
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

    val SUPPLIER = Supplier(
        id = "supplier_id",
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

    val CUSTOMER_COLLECTION_PROFILE = `in`.okcredit.collection.contract.CollectionCustomerProfile(
        accountId = CUSTOMER.id,
        qr_intent = "qr_intent",
        link_intent = "link"
    )

    val CUSTOMER_CORE = `in`.okcredit.merchant.core.model.Customer(
        id = "1",
        customerSyncStatus = CLEAN.code,
        status = 1,
        description = "Test 1",
        mobile = "88383748374",
        balance = 0,
        transactionCount = 2,
        registered = false,
        txnAlertEnabled = false,
        isLiveSales = false,
        createdAt = Timestamp(12312333),
        lastActivityMetaInfo = null,
        lastAmount = null,
    )
}
