package `in`.okcredit.user_migration.presentation.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.InteractionType
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import dagger.Lazy
import org.junit.Test

class UserMigrationEventTrackerTest {
    private val ab: AnalyticsProvider = mock()
    private val userMigrationEventTracker = UserMigrationEventTracker(Lazy { ab })

    @Test
    fun `should call track event with correct name when trackObjectView is called`() {
        userMigrationEventTracker.trackObjectViewed("User Migration")

        verify(ab).trackObjectViewed("User Migration")
    }

    @Test
    fun `should call track event with correct name when trackShareAppInteracted is called`() {
        userMigrationEventTracker.trackBottomSheetInteracted("Upload Button", InteractionType.CLICK)

        verify(ab).trackObjectInteracted(
            "Upload Pdf Entry Point",
            InteractionType.CLICK,
            mapOf("Item" to "Upload Button")
        )
    }

    @Test
    fun `should call track event with correct name when trackUserMigrationLocalFileListingScreenInteracted is called`() {
        userMigrationEventTracker.trackLocalFileListingScreenInteracted("Submit Button", InteractionType.CLICK)

        verify(ab).trackObjectInteracted(
            "Local File Listing Screen",
            InteractionType.CLICK,
            mapOf("Item" to "Submit Button")
        )
    }

    @Test
    fun `should call track event with correct name when trackUserMigrationFileUploadingStatusScreenInteracted is called`() {
        userMigrationEventTracker.trackFileUploadingStatusScreenInteracted("Cancel File Upload", InteractionType.CLICK)

        verify(ab).trackObjectInteracted(
            "File Uploading Status Screen",
            InteractionType.CLICK,
            mapOf("Item" to "Cancel File Upload")
        )
    }

    @Test
    fun `should call track event with correct name when FileUploadingStatusScreenInteracted and  Submit button is called`() {
        userMigrationEventTracker.trackFileUploadingStatusScreenInteracted("Submit Button", InteractionType.CLICK)

        verify(ab).trackObjectInteracted(
            "File Uploading Status Screen",
            InteractionType.CLICK,
            mapOf("Item" to "Submit Button")
        )
    }

    @Test
    fun `should call track event with correct name when DisplayParsedCustomerScreenInteracted is called`() {
        userMigrationEventTracker.trackDisplayParsedCustomerScreenInteracted("Submit Button", InteractionType.CLICK)

        verify(ab).trackObjectInteracted(
            "Display Parsed Customer Screen",
            InteractionType.CLICK,
            mapOf("Item" to "Submit Button")
        )
    }
}
