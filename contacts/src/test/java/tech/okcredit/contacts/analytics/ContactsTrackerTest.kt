package tech.okcredit.contacts.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class ContactsTrackerTest {
    private val analyticsProvider: AnalyticsProvider = mock()

    private val contactsTracker = ContactsTracker { analyticsProvider }

    @Test
    fun `trackUploadStarted() should call trackEvents`() {
        contactsTracker.trackUploadStarted()

        verify(analyticsProvider).trackEvents(
            "ContactSync: Upload",
            mapOf(
                "Status" to "Started"
            )
        )
    }

    @Test
    fun `trackDownloadStarted() should call trackEvents`() {
        contactsTracker.trackDownloadStarted()

        verify(analyticsProvider).trackEvents(
            "ContactSync: Download",
            mapOf(
                "Status" to "Started"
            )
        )
    }

    @Test
    fun `trackUploadComplete() should call trackEvents with correct values`() {
        contactsTracker.trackUploadComplete(15, 10)

        verify(analyticsProvider).trackEvents(
            "ContactSync: Upload",
            mapOf(
                "Status" to "Completed",
                "Size" to "15",
                "Steps" to "10"
            )
        )
    }

    @Test
    fun `trackDownloadComplete() should call trackEvents when contact size is less than 1000`() {
        contactsTracker.trackDownloadComplete(911, 10)

        verify(analyticsProvider).trackEvents(
            "ContactSync: Download",
            mapOf(
                "Status" to "Completed",
                "Size" to "911",
                "Steps" to "10"
            )
        )
    }

    @Test
    fun `trackDownloadComplete() should call trackEvents when contact size is more than 1000`() {
        contactsTracker.trackDownloadComplete(1004, 10)

        verify(analyticsProvider).trackEvents(
            "ContactSync: Download",
            mapOf(
                "Status" to "Completed",
                "Size" to "1004",
                "Steps" to "10"
            )
        )
    }

    @Test
    fun `trackInAppDisplayed() should call trackEvents`() {
        contactsTracker.trackInAppDisplayed()

        verify(analyticsProvider).trackEvents(
            "InAppNotification Displayed",
            mapOf(
                "Type" to "OkCredit Contact Add"
            )
        )
    }

    @Test
    fun `trackInAppInteracted() should call trackEvents`() {
        contactsTracker.trackInAppInteracted()

        verify(analyticsProvider).trackEvents(
            "InAppNotification Clicked",
            mapOf(
                "Type" to "OkCredit Contact Add"
            )
        )
    }

    @Test
    fun `trackOkCreditContactSaved() should call trackEvents`() {
        contactsTracker.trackOkCreditContactSaved("method")

        verify(analyticsProvider).trackEvents(
            "OkCredit Contact Saved",
            mapOf(
                "Method" to "method"
            )
        )
    }

    @Test
    fun `trackOkCreditContactAlreadyExist() should call trackEvents`() {
        contactsTracker.trackOkCreditContactAlreadyExist()

        verify(analyticsProvider).trackEvents(
            "OkCredit Contact Exist",
            mapOf(
                "Method" to "Auto"
            )
        )
    }

    @Test
    fun `trackOkCreditContactSkipped() should call trackEvents`() {
        contactsTracker.trackOkCreditContactSkipped()

        verify(analyticsProvider).trackEvents(
            "OkCredit Contact Skipped",
            mapOf(
                "Method" to "Manual"
            )
        )
    }

    @Test
    fun `trackFailedToOpenPhoneBook() should call trackEvents`() {
        contactsTracker.trackFailedToOpenPhoneBook("Fake_Error_Message")

        verify(analyticsProvider).trackObjectError(
            "Failed To Open PhoneBook",
            "Fake_Error_Message",
            null
        )
    }
}
