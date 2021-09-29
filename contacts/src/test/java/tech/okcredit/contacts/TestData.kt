package tech.okcredit.contacts

import `in`.okcredit.merchant.device.Device
import org.joda.time.DateTime
import tech.okcredit.contacts.contract.model.Contact

internal object TestData {
    const val ID = "lubalubadubdub"

    const val DEVICE_ID = "lubalubadubdub lubalubadubdub"
    val DEVICE = Device(DEVICE_ID, 12, 12, "", "", mutableListOf(), DateTime.now(), DateTime())

    val TEST_CONTACTS = mutableListOf<Contact>()
}
