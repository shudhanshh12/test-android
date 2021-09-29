package tech.okcredit.contacts.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.device.DeviceRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.awaitFirst
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.contacts.ContactUtils.FRC_KEY_CHUNK_SIZE
import tech.okcredit.contacts.ContactUtils.FRC_KEY_CONTACT_PAGING_LIMIT
import tech.okcredit.contacts.ContactsLocalSource
import tech.okcredit.contacts.ContactsRemoteSource
import tech.okcredit.contacts.analytics.ContactsTracker
import tech.okcredit.contacts.contract.model.Contact
import javax.inject.Inject

class UploadContacts @Inject constructor(
    private val localSource: Lazy<ContactsLocalSource>,
    private val remoteSource: Lazy<ContactsRemoteSource>,
    private val deviceRepository: Lazy<DeviceRepository>,
    private val contactsTracker: Lazy<ContactsTracker>,
    firebaseRemoteConfig: FirebaseRemoteConfig,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    private val contactPagingLimit = firebaseRemoteConfig.getLong(FRC_KEY_CONTACT_PAGING_LIMIT).toInt()
    private val chunkSize = firebaseRemoteConfig.getLong(FRC_KEY_CHUNK_SIZE).toInt()

    internal suspend fun execute() {
        val businessId = getActiveBusinessId.get().execute().await()
        try {
            contactsTracker.get().trackUploadStarted()
            var totalContactSize = 0
            var repeatCount = 0

            val deviceId = deviceRepository.get().getDevice().awaitFirst().id
            do {
                val unsyncedContactList = getUnsyncedContactsFromLocal()
                if (unsyncedContactList.isNotEmpty()) {
                    uploadContactsOnRemote(deviceId, unsyncedContactList, businessId)
                    updateSyncStatusInLocal(unsyncedContactList)

                    totalContactSize += unsyncedContactList.size
                    repeatCount++
                }
            } while (unsyncedContactList.size == contactPagingLimit)
            contactsTracker.get().trackUploadComplete(totalContactSize, repeatCount)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private suspend fun getUnsyncedContactsFromLocal() = localSource.get().getUnsyncedContacts(contactPagingLimit)

    private suspend fun uploadContactsOnRemote(deviceId: String, contactList: List<Contact>, businessId: String) {
        val isLastBatch = contactList.size < contactPagingLimit
        return remoteSource.get().uploadContacts(deviceId, contactList, isLastBatch, businessId)
    }

    /**
     * Updates sync status to true in database for contacts uploaded.
     * We update statuses in CHUNKS to avoid 'SQLiteException: too many SQL variables'
     * Read more - https://sqlite.org/limits.html
     */
    private suspend fun updateSyncStatusInLocal(unsyncedContactList: List<Contact>) {
        unsyncedContactList.map { it.mobile }
            .chunked(chunkSize)
            .forEach { chunk -> localSource.get().updateSyncStatus(chunk, true) }
    }

    private fun handleException(e: Exception) {
        if (e !is CancellationException) {
            contactsTracker.get().trackException("UploadContacts", e)
            RecordException.recordException(e)
        }
    }
}
