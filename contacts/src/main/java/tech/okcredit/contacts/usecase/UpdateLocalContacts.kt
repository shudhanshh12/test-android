package tech.okcredit.contacts.usecase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import dagger.Reusable
import kotlinx.coroutines.CancellationException
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.contacts.ContactUtils
import tech.okcredit.contacts.ContactsLocalSource
import tech.okcredit.contacts.analytics.ContactsTracker
import tech.okcredit.contacts.contract.model.Contact
import javax.inject.Inject

/**
 * Updates contacts in local as per changes in user's phone book (contact list)
 * - Inserts new contacts
 * - Replaces updated contacts
 * - Removes deleted contacts
 */
@Reusable
class UpdateLocalContacts @Inject constructor(
    private val getPhoneBookContacts: Lazy<GetPhoneBookContacts>,
    private val localSource: Lazy<ContactsLocalSource>,
    firebaseRemoteConfig: FirebaseRemoteConfig,
    private val contactsTracker: Lazy<ContactsTracker>,
) {

    private val chunkSize = firebaseRemoteConfig.getLong(ContactUtils.FRC_KEY_CHUNK_SIZE).toInt()

    suspend fun execute() {
        try {
            val databaseLastUpdatedTimestamp = getUpdatedTimestampInLocal()
            val phoneBookContactList = getContactsFromPhonebook()
            insertNewContacts(phoneBookContactList, databaseLastUpdatedTimestamp)
            removeDeleteContactsInLocal(phoneBookContactList)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private suspend fun getUpdatedTimestampInLocal() = localSource.get().getLastUpdatedTimestamp()

    private fun getContactsFromPhonebook() = getPhoneBookContacts.get().execute()

    /**
     * Inserts new contacts into the local database.
     * We insert contacts in CHUNKS to avoid 'SQLiteException: too many SQL variables'
     * Read more - https://sqlite.org/limits.html
     */
    private suspend fun insertNewContacts(
        phoneBookContactList: List<Contact>,
        databaseLastUpdatedTimestamp: Long?,
    ) {
        buildNewContactsList(phoneBookContactList, databaseLastUpdatedTimestamp)
            .chunked(chunkSize)
            .forEach { chunk -> localSource.get().insertContacts(chunk) }
    }

    private fun buildNewContactsList(
        phoneBookContactList: List<Contact>,
        databaseLastUpdatedTimestamp: Long?,
    ): List<Contact> {
        val contactsToBeInserted = mutableListOf<Contact>()
        if (databaseLastUpdatedTimestamp == null) {
            // add all contacts
            contactsToBeInserted.addAll(phoneBookContactList)
        } else {
            val phoneBookLastUpdatedTimestamp = phoneBookContactList.lastOrNull()?.timestamp
            if (phoneBookLastUpdatedTimestamp != null && phoneBookLastUpdatedTimestamp > databaseLastUpdatedTimestamp) {
                // filter new contacts
                val newContacts = phoneBookContactList.filter { it.timestamp > databaseLastUpdatedTimestamp }
                contactsToBeInserted.addAll(newContacts)
            } // else - no new contacts found
        }
        return contactsToBeInserted
    }

    /**
     * Deleted removed contacts from the local database.
     * We delete contacts in CHUNKS to avoid 'SQLiteException: too many SQL variables'
     * Read more - https://sqlite.org/limits.html
     */
    private suspend fun removeDeleteContactsInLocal(phoneBookMobileList: List<Contact>) {
        val databaseMobileList = localSource.get().getContactMobileNumbers()
        buildDeletedContactsList(phoneBookMobileList.map { it.mobile }, databaseMobileList)
            .chunked(chunkSize)
            .forEach { chunk -> localSource.get().deleteContacts(chunk) }
    }

    private fun buildDeletedContactsList(
        phoneBookContactNumberList: List<String>,
        databaseContactNumberList: List<String>,
    ) = databaseContactNumberList - phoneBookContactNumberList

    private fun handleException(e: Exception) {
        if (e !is CancellationException) {
            contactsTracker.get().trackException("UpdateLocalContacts", e)
            RecordException.recordException(e)
        }
    }
}
