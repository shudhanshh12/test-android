package tech.okcredit.contacts.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.device.DeviceRepository
import dagger.Lazy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.awaitFirst
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.contacts.ContactsLocalSource
import tech.okcredit.contacts.ContactsRemoteSource
import tech.okcredit.contacts.analytics.ContactsTracker
import tech.okcredit.contacts.server.data.Contact
import tech.okcredit.contacts.store.preference.ContactPreference
import javax.inject.Inject

class CheckForContactsInOkcNetwork @Inject constructor(
    private val localSource: Lazy<ContactsLocalSource>,
    private val remoteSource: Lazy<ContactsRemoteSource>,
    private val contactPreference: Lazy<ContactPreference>,
    private val deviceRepository: Lazy<DeviceRepository>,
    private val contactsTracker: Lazy<ContactsTracker>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    suspend fun execute() {
        val businessId = getActiveBusinessId.get().execute().await()
        try {
            contactsTracker.get().trackDownloadStarted()
            var startTimestamp = contactPreference.get().getStartTime()
            var lastCheckedContactId: String? = contactPreference.get().getLastId()
            val deviceId = deviceRepository.get().getDevice().awaitFirst().id
            var totalContactSize = 0
            var repeatCount = 0
            do {
                val response =
                    getContactsInOkcNetworkFromRemote(deviceId, startTimestamp, lastCheckedContactId!!, businessId)
                updateFoundStatusInLocal(response.contacts)

                startTimestamp = response.next.startTimestamp
                lastCheckedContactId = response.next.lastId
                totalContactSize += response.contacts.size
                repeatCount++
            } while (response.next.hasMore)

            contactPreference.get().setStartTime(startTimestamp)
            lastCheckedContactId?.let { contactPreference.get().setLastId(it) }
            contactsTracker.get().trackDownloadComplete(totalContactSize, repeatCount)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private suspend fun getContactsInOkcNetworkFromRemote(
        deviceId: String,
        startTimestamp: Long,
        lastCheckedContactId: String,
        businessId: String,
    ) = remoteSource.get().getCheckedContacts(deviceId, startTimestamp, lastCheckedContactId, businessId)

    private suspend fun updateFoundStatusInLocal(contacts: List<Contact>) = contacts.forEach { contact ->
        localSource.get().updateFoundStatusAndType(contact.mobile, contact.found, contact.type)
    }

    private fun handleException(e: Exception) {
        if (e !is CancellationException) {
            contactsTracker.get().trackException("CheckForContactsInOkcNetwork", e)
            RecordException.recordException(e)
        }
    }
}
