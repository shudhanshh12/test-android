package `in`.okcredit.merchant.core

import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.CLEAN
import `in`.okcredit.merchant.core.store.database.Customer
import `in`.okcredit.merchant.core.store.database.Transaction

object TestData {

    val BUSINESS_ID = "business-id"

    val CUSTOMER_DB_1 = Customer(
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
        businessId = BUSINESS_ID
    )

    val CUSTOMER_DB_2 = Customer(
        id = "2",
        customerSyncStatus = CLEAN.code,
        status = 0,
        description = "Test 2",
        mobile = "883837484",
        balance = 0,
        transactionCount = 3,
        registered = false,
        txnAlertEnabled = false,
        isLiveSales = false,
        createdAt = Timestamp(12312334),
        businessId = BUSINESS_ID
    )

    val CUSTOMER_DB_3 = Customer(
        id = "3",
        customerSyncStatus = CLEAN.code,
        status = 1,
        description = "Test 3",
        balance = 0,
        transactionCount = 3,
        registered = false,
        txnAlertEnabled = false,
        isLiveSales = false,
        createdAt = Timestamp(12312334),
        businessId = BUSINESS_ID
    )

    val TRANSACTION_DB_1 = Transaction(
        id = "122",
        type = 1,
        customerId = "1",
        amount = 50000,
        collectionId = null,
        images = null,
        note = null,
        createdAt = Timestamp(12312334),
        isDeleted = false,
        isDirty = false,
        billDate = Timestamp(12312334),
        updatedAt = Timestamp(12312334),
        smsSent = false,
        createdByCustomer = false,
        deletedByCustomer = false,
        state = 1,
        category = 0,
        amountUpdated = false,
        businessId = BUSINESS_ID
    )

    val TRANSACTION_DB_2 = Transaction(
        id = "123",
        type = 1,
        customerId = "1",
        amount = 10000,
        collectionId = null,
        images = null,
        note = null,
        createdAt = Timestamp(12312334),
        isDeleted = false,
        isDirty = false,
        billDate = Timestamp(12312334),
        updatedAt = Timestamp(12312334),
        smsSent = false,
        createdByCustomer = false,
        deletedByCustomer = false,
        state = 1,
        category = 0,
        amountUpdated = false,
        businessId = BUSINESS_ID
    )

    val TRANSACTION_DB_3 = Transaction(
        id = "124",
        type = 2,
        customerId = "2",
        amount = 50000,
        collectionId = null,
        images = null,
        note = null,
        createdAt = Timestamp(12312334),
        isDeleted = false,
        isDirty = false,
        billDate = Timestamp(12312334),
        updatedAt = Timestamp(12312334),
        smsSent = false,
        createdByCustomer = false,
        deletedByCustomer = false,
        state = 1,
        category = 0,
        amountUpdated = false,
        businessId = BUSINESS_ID
    )

    val TRANSACTION_DB_4 = Transaction(
        id = "125",
        type = 1,
        customerId = "2",
        amount = 50000,
        collectionId = null,
        images = null,
        note = null,
        createdAt = Timestamp(12312334),
        isDeleted = false,
        isDirty = false,
        billDate = Timestamp(12312334),
        updatedAt = Timestamp(12312334),
        smsSent = false,
        createdByCustomer = false,
        deletedByCustomer = false,
        state = 1,
        category = 0,
        amountUpdated = false,
        businessId = BUSINESS_ID
    )
}
