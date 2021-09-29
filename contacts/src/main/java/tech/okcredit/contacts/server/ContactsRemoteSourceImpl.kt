package tech.okcredit.contacts.server

import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError
import tech.okcredit.contacts.BuildConfig
import tech.okcredit.contacts.ContactsRemoteSource
import tech.okcredit.contacts.contract.model.Contact
import tech.okcredit.contacts.server.ContactsApiClient.Companion.ACKNOWLEDGE_CONTACT_SAVED_END_POINT
import tech.okcredit.contacts.server.ContactsApiClient.Companion.OKC_CONTACT_END_POINT
import javax.inject.Inject

class ContactsRemoteSourceImpl @Inject constructor(
    private val contactsApiClient: ContactsApiClient,
) : ContactsRemoteSource {

    override suspend fun uploadContacts(
        deviceId: String,
        contacts: List<Contact>,
        lastBatch: Boolean,
        businessId: String
    ) {
        val request = UploadContactRequest(deviceId, contacts.map { it.toApiModel() }, lastBatch)
        return contactsApiClient.uploadContact(request, businessId)
    }

    override suspend fun getCheckedContacts(deviceId: String, startTime: Long, lastId: String, businessId: String) =
        contactsApiClient.getCheckedResponse(deviceId, startTime, lastId, businessId)

    override fun getOkCreditContact(businessId: String): Single<OkCreditContactResponse> {
        return contactsApiClient.getOkCreditContact(BuildConfig.CONTACT_BASE_URL + OKC_CONTACT_END_POINT, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map {
                if (it.isSuccessful) {
                    return@map it.body()
                } else {
                    throw it.asError()
                }
            }
    }

    override fun acknowledgeContactSaved(businessId: String): Completable {
        return contactsApiClient.acknowledgeContactSaved(BuildConfig.CONTACT_BASE_URL + ACKNOWLEDGE_CONTACT_SAVED_END_POINT, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .flatMapCompletable {
                if (it.isSuccessful) {
                    Completable.complete()
                } else {
                    Completable.error(it.asError())
                }
            }
    }
}
