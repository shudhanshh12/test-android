package tech.okcredit.home

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.CLEAN
import `in`.okcredit.merchant.suppliercredit.Supplier
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import tech.okcredit.contacts.contract.model.Contact
import tech.okcredit.home.ui.homesearch.HomeSearchContract

internal object TestData {
    val source_customer = HomeSearchContract.SOURCE.HOME_CUSTOMER_TAB
    val source_supplier = HomeSearchContract.SOURCE.HOME_SUPPLIER_TAB

    const val SEARCH_QUERY = "John Lennon"
    const val BUSINESS_ID = "business_id"

    val CONTACT = Contact(
        "john_lennon_46464656", "John Lennon", "9999999999", "", false, 0, false, 1
    )

    val ERROR = Exception("BHOOM BHOOM BHOOM")

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
        DateTime.now(),
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
        CLEAN.code,
        Transaction.CREDIT,
        "9999999999",
        "John Lennon",
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
        "9999999999",
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

    val CUSTOMER_2 = Customer(
        "43453454",
        CLEAN.code,
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

    val COLLECTION_CUSTOMER_PROFILE = CollectionCustomerProfile(
        CUSTOMER.id,
        null,
        null,
        null,
        null,
        false
    )

    val SUPPLIER_1 = Supplier(
        "1",
        true,
        false,
        DateTime(2018, 10, 2, 0, 0, 0),
        0L,
        "Time",
        "9999999999",
        "",
        "",
        -1000,
        0,
        DateTime(2020, 10, 2, 0, 0, 0),
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

    val SUPPLIER_2 = Supplier(
        "2",
        true,
        false,
        DateTime(2018, 10, 2, 0, 0, 0),
        0L,
        "Money",
        "8888888888",
        "",
        "",
        1000,
        0,
        DateTime(2019, 10, 2, 0, 0, 0),
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

    val SUPPLIER_3 = Supplier(
        "3",
        true,
        false,
        DateTime(2018, 10, 2, 0, 0, 0),
        0L,
        "Brain",
        "8989898989",
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

    val SUPPLIER_4 = Supplier(
        "4",
        true,
        false,
        DateTime(2017, 10, 2, 0, 0, 0),
        0L,
        "Breathe",
        "9876543210",
        "",
        "",
        1,
        0,
        DateTime(2017, 10, 2, 0, 0, 0),
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
