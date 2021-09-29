package `in`.okcredit.user_migration.presentation.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.InteractionType
import `in`.okcredit.analytics.PropertyKey
import dagger.Lazy
import javax.inject.Inject

class UserMigrationEventTracker @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>
) {

    object PropertyKey {
        const val ITEM = "Item"
        const val TYPE = "Type"
    }

    object Objects {
        const val STORAGE_PERMISSION = "Storage Permission "
        const val User_Migration_Bottom_Sheet = "Upload Pdf Entry Point"
        const val User_Migration_Local_File_Listing_Screen = "Local File Listing Screen"
        const val User_Migration_File_Uploading_Status_Screen = "File Uploading Status Screen"
        const val User_Migration_Display_Parsed_Customer_Screen = "Display Parsed Customer Screen"
    }

    fun trackObjectViewed(objects: String) {
        analyticsProvider.get().trackObjectViewed(objects)
    }

    fun trackBottomSheetInteracted(
        item: String,
        interactionType: InteractionType = InteractionType.CLICK
    ) {
        val properties = mapOf<String, Any>(
            PropertyKey.ITEM to item
        )
        analyticsProvider.get().trackObjectInteracted(Objects.User_Migration_Bottom_Sheet, interactionType, properties)
    }

    fun trackLocalFileListingScreenInteracted(
        item: String,
        interactionType: InteractionType = InteractionType.CLICK
    ) {
        val properties = mapOf<String, Any>(
            PropertyKey.ITEM to item
        )
        analyticsProvider.get()
            .trackObjectInteracted(Objects.User_Migration_Local_File_Listing_Screen, interactionType, properties)
    }

    fun trackFileUploadingStatusScreenInteracted(
        item: String,
        interactionType: InteractionType = InteractionType.CLICK
    ) {
        val properties = mapOf<String, Any>(
            PropertyKey.ITEM to item
        )
        analyticsProvider.get()
            .trackObjectInteracted(Objects.User_Migration_File_Uploading_Status_Screen, interactionType, properties)
    }

    fun trackDisplayParsedCustomerScreenInteracted(
        item: String,
        interactionType: InteractionType = InteractionType.CLICK
    ) {
        val properties = mapOf<String, Any>(
            PropertyKey.ITEM to item
        )
        analyticsProvider.get()
            .trackObjectInteracted(Objects.User_Migration_Display_Parsed_Customer_Screen, interactionType, properties)
    }

    fun trackStoragePermissionInteracted(
        item: String,
        interactionType: InteractionType = InteractionType.CLICK
    ) {
        val properties = mapOf<String, Any>(
            `in`.okcredit.analytics.PropertyKey.Item to item,
            `in`.okcredit.analytics.PropertyKey.SCREEN to Objects.User_Migration_Local_File_Listing_Screen

        )
        analyticsProvider.get().trackObjectInteracted(Objects.STORAGE_PERMISSION, interactionType, properties)
    }
}
