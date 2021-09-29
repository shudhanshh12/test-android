package tech.okcredit.web

import org.joda.time.DateTime
import tech.okcredit.contacts.contract.model.Contact

internal object TestData {
    val FEATURE = "collection"

    val EXPERIMENT = "exp1"

    val MERCHANT = `in`.okcredit.merchant.contract.Business(
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
        DateTime(),
        null,
        false,
        null,
        null,
        false,
        null,
        null
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
}
