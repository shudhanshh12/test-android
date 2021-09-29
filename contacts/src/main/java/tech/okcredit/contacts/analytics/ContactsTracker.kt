package tech.okcredit.contacts.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyKey.REASON
import `in`.okcredit.analytics.PropertyValue.CAUSE
import `in`.okcredit.analytics.PropertyValue.STACKTRACE
import dagger.Lazy
import dagger.Reusable
import tech.okcredit.android.base.extensions.itOrBlank
import tech.okcredit.android.base.utils.getStringStackTrace
import tech.okcredit.contacts.analytics.ContactsTracker.Event.CONTACT_SYNC_DOWNLOAD
import tech.okcredit.contacts.analytics.ContactsTracker.Event.CONTACT_SYNC_EXCEPTION
import tech.okcredit.contacts.analytics.ContactsTracker.Event.CONTACT_SYNC_UPLOAD
import tech.okcredit.contacts.analytics.ContactsTracker.PropertyKey.CLASS_NAME
import tech.okcredit.contacts.analytics.ContactsTracker.PropertyKey.OKCREDIT_CONTACT_EXIST
import tech.okcredit.contacts.analytics.ContactsTracker.PropertyKey.OKCREDIT_CONTACT_SAVED
import tech.okcredit.contacts.analytics.ContactsTracker.PropertyKey.OKCREDIT_CONTACT_SKIPPED
import tech.okcredit.contacts.analytics.ContactsTracker.PropertyKey.OKCREDIT_FAILED_TO_OPEN_PHONE_BOOK
import tech.okcredit.contacts.analytics.ContactsTracker.PropertyKey.SIZE
import tech.okcredit.contacts.analytics.ContactsTracker.PropertyKey.STATUS
import tech.okcredit.contacts.analytics.ContactsTracker.PropertyKey.STEPS
import tech.okcredit.contacts.analytics.ContactsTracker.PropertyKey.TYPE
import tech.okcredit.contacts.analytics.ContactsTracker.PropertyValue.AUTO
import tech.okcredit.contacts.analytics.ContactsTracker.PropertyValue.COMPLETED
import tech.okcredit.contacts.analytics.ContactsTracker.PropertyValue.MANUAL
import tech.okcredit.contacts.analytics.ContactsTracker.PropertyValue.OKCREDIT_CONTACT_ADD
import tech.okcredit.contacts.analytics.ContactsTracker.PropertyValue.STARTED
import javax.inject.Inject

@Reusable
class ContactsTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {
    object Event {
        const val CONTACT_SYNC_UPLOAD = "ContactSync: Upload"
        const val CONTACT_SYNC_DOWNLOAD = "ContactSync: Download"
        const val CONTACT_SYNC_EXCEPTION = "Contact Sync: Error"
    }

    object PropertyKey {
        const val TYPE = "Type"
        const val SIZE = "Size"
        const val STATUS = "Status"
        const val STEPS = "Steps"
        const val OKCREDIT_CONTACT_EXIST = "OkCredit Contact Exist"
        const val OKCREDIT_CONTACT_SAVED = "OkCredit Contact Saved"
        const val OKCREDIT_CONTACT_SKIPPED = "OkCredit Contact Skipped"
        const val OKCREDIT_FAILED_TO_OPEN_PHONE_BOOK = "Failed To Open PhoneBook"
        const val CLASS_NAME = "ClassName"
    }

    object PropertyValue {
        const val STARTED = "Started"
        const val COMPLETED = "Completed"
        const val OKCREDIT_CONTACT_ADD = "OkCredit Contact Add"
        const val AUTO = "Auto"
        const val MANUAL = "Manual"
    }

    fun trackUploadStarted() {
        val properties = mapOf(STATUS to STARTED)
        analyticsProvider.get().trackEvents(CONTACT_SYNC_UPLOAD, properties)
    }

    fun trackDownloadStarted() {
        val properties = mapOf(STATUS to STARTED)
        analyticsProvider.get().trackEvents(CONTACT_SYNC_DOWNLOAD, properties)
    }

    fun trackUploadComplete(totalContactSize: Int, steps: Int) {
        val properties = mapOf(
            STATUS to COMPLETED,
            SIZE to totalContactSize.toString(),
            STEPS to steps.toString(),
        )
        analyticsProvider.get().trackEvents(CONTACT_SYNC_UPLOAD, properties)
    }

    fun trackDownloadComplete(totalContactSize: Int, steps: Int) {
        val properties = mapOf(
            STATUS to COMPLETED,
            SIZE to totalContactSize.toString(),
            STEPS to steps.toString(),
        )
        analyticsProvider.get().trackEvents(CONTACT_SYNC_DOWNLOAD, properties)
    }

    fun trackInAppDisplayed() {
        val properties = mapOf(TYPE to OKCREDIT_CONTACT_ADD)
        analyticsProvider.get().trackEvents(`in`.okcredit.analytics.Event.IN_APP_NOTI_DISPLAYED, properties)
    }

    fun trackInAppInteracted() {
        val properties = mapOf(TYPE to OKCREDIT_CONTACT_ADD)
        analyticsProvider.get().trackEvents(`in`.okcredit.analytics.Event.IN_APP_NOTI_CLICKED, properties)
    }

    fun trackOkCreditContactSaved(method: String) {
        val properties = mapOf(`in`.okcredit.analytics.PropertyKey.METHOD to method)
        analyticsProvider.get().trackEvents(OKCREDIT_CONTACT_SAVED, properties)
    }

    fun trackOkCreditContactAlreadyExist() {
        val properties = mapOf(`in`.okcredit.analytics.PropertyKey.METHOD to AUTO)
        analyticsProvider.get().trackEvents(OKCREDIT_CONTACT_EXIST, properties)
    }

    fun trackOkCreditContactSkipped() {
        val properties = mapOf(`in`.okcredit.analytics.PropertyKey.METHOD to MANUAL)
        analyticsProvider.get().trackEvents(OKCREDIT_CONTACT_SKIPPED, properties)
    }

    fun trackFailedToOpenPhoneBook(errorMessage: String?) {
        analyticsProvider.get().trackObjectError(
            OKCREDIT_FAILED_TO_OPEN_PHONE_BOOK,
            errorMessage ?: ""
        )
    }

    fun trackException(className: String, throwable: Throwable) {
        val properties = mapOf(
            CLASS_NAME to className,
            REASON to throwable.message.itOrBlank(),
            CAUSE to throwable.cause?.message.itOrBlank(),
            STACKTRACE to throwable.getStringStackTrace(),
        )
        analyticsProvider.get().trackEngineeringMetricEvents(CONTACT_SYNC_EXCEPTION, properties)
    }
}
