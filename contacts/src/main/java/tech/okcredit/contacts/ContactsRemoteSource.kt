package tech.okcredit.contacts

import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.contacts.contract.model.Contact
import tech.okcredit.contacts.server.OkCreditContactResponse
import tech.okcredit.contacts.server.data.CheckedResponse

interface ContactsRemoteSource {
    suspend fun uploadContacts(
        deviceId: String,
        contacts: List<Contact>,
        lastBatch: Boolean,
        businessId: String,
    )

    suspend fun getCheckedContacts(
        deviceId: String,
        startTime: Long,
        lastId: String,
        businessId: String,
    ): CheckedResponse

    fun getOkCreditContact(businessId: String): Single<OkCreditContactResponse>

    fun acknowledgeContactSaved(businessId: String): Completable
}
