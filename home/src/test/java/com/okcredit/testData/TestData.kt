package com.okcredit.testData

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.CLEAN
import `in`.okcredit.merchant.suppliercredit.Supplier
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime

internal object TestData {

    val CUSTOMER = Customer(
        "1234",
        CLEAN.code,
        Transaction.CREDIT,
        "9999999999",
        "ohn Lennon",
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

    val CUSTOMER_2 = Customer(
        "1234",
        CLEAN.code,
        Transaction.CREDIT,
        "8888888888",
        "ohn Lennon",
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

    val SUPPLIER = Supplier(
        "24234afad3432",
        true,
        false,
        DateTime(2018, 10, 2, 0, 0, 0),
        0L,
        "John Lennon",
        "9898989898",
        "",
        "",
        0,
        0,
        DateTime(2018, 10, 2, 0, 0, 0),
        DateTime(2018, 10, 2, 0, 0, 0),
        true,
        "",
        true,
        null,
        true,
        0,
        false,
        restrictContactSync = false
    )
}
