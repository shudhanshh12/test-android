package merchant.okcredit.user_stories.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import dagger.Lazy
import merchant.okcredit.user_stories.analytics.UserStoriesTracker.Event.USER_STORY_VIEW
import javax.inject.Inject

class UserStoriesTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {
    object Key {

        const val MY_STATUS_COUNT = "my_status_count"
        const val OTHER_STATUS_COUNT = "other_status_count"
        const val STEP = "step"
        const val MERCHANT_ID = "merchant_id"
        const val SCREEN = "screen"
        const val TYPE = "type"
        const val COUNT = "count"
        const val SEEN_COUNT = "seen"
        const val STORY_ID = "story_id"
        const val GROUP_COUNT = "group_count"
        const val TYPE_USER_STATUS = "user_story"
        const val POSITION = "position"
        const val RELATIONSHIP = "relationship"
        const val ACCOUNT_ID = "account_id"
        const val PERMISSION_TYPE = "permission_type"
        const val VALUE = "value"
        const val ADD_STORY = "add_story"
        const val SOURCE = "source"
        const val IMAGE_COUNT = "image_count"
        const val CAMERA_TYPE = "camera_type"
        const val FLASH = "flash"
        const val EXPIRY_TIME = "expiry_time"
        const val CAPTION = "caption"
    }

    object Event {
        const val SYNC_MY_STATUS = "my_status_sync_and_save_locally"
        const val SYNC_OTHER_STATUS = "other_status_sync_and_save_locally"
        const val USER_STORY_VIEW = "story_view"
        const val USER_STORY_ADD_STORY = "add_story_clicked"
        const val USER_STORY_PERMISSION_CLICKED = "user_story_permission_clicked"
        const val USER_STORY_PERMISSION_LOADED = "user_story_permission_loaded"
        const val USER_STORY_CHOOSE_IMAGE = "user_story_choose_image"
        const val USER_STORY_CHOOSE_IMAGE_SUCCESS = "user_story_choose_image_success"
        const val USER_STORY_ADD_MORE_IMAGE = "user_story_add_more_image"
        const val USER_STORY_DELETE_IMAGE = "user_story_delete_image"
        const val USER_STORY_ADD_CAPTION_STARTED = "user_story_add_caption_started"
        const val USER_STORY_ADD_CAPTION_SUCCESS = "user_story_add_caption_success"
        const val USER_STORY_ADD_STORY_UPLOAD_CLICKED = "user_story_add_story_upload_clicked"
        const val USER_STORY_ADD_STORY_UPLOAD_CLICKED_SUCCESS = "user_story_add_story_upload_clicked_success"
    }

    fun trackMyStatusSync(step: String, count: Int = 0) {
        val properties = HashMap<String, Any>().apply {
            this[Key.STEP] = step
            this[Key.MY_STATUS_COUNT] = count
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.SYNC_MY_STATUS, properties)
    }

    fun trackOthersStatusSync(step: String, count: Int = 0) {
        val properties = HashMap<String, Any>().apply {
            this[Key.STEP] = step
            this[Key.OTHER_STATUS_COUNT] = count
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.SYNC_OTHER_STATUS, properties)
    }

    fun trackEventUserStoryClick(
        storyId: String,
        storyGroupCount: Int,
        seenCount: Int,
        merchantId: String,
        screen: String,
        accountId: String,
        relationship: String,
        position: Int,
        storyType: String,
    ) {
        val event = mapOf<String, Any>(
            Key.STORY_ID to storyId,
            Key.MERCHANT_ID to merchantId,
            Key.SEEN_COUNT to seenCount,
            Key.GROUP_COUNT to storyGroupCount,
            Key.SCREEN to screen,
            Key.TYPE to storyType,
            Key.RELATIONSHIP to relationship,
            Key.ACCOUNT_ID to accountId,
            Key.POSITION to position
        )

        analyticsProvider.get().trackEvents(USER_STORY_VIEW, event)
    }

    fun trackEventAddStoryClick(merchantId: String, screen: String, position: String) {

        val event = mapOf<String, Any>(
            Key.SCREEN to screen,
            Key.MERCHANT_ID to merchantId,
            Key.POSITION to position,
            Key.TYPE to Key.TYPE_USER_STATUS
        )

        analyticsProvider.get().trackEvents(Event.USER_STORY_ADD_STORY, event)
    }

    fun trackEventStoryPermissionClicked(merchantId: String?, permissionType: String, value: String) {
        merchantId?.let {
            val event = mapOf<String, Any>(
                Key.MERCHANT_ID to merchantId,
                Key.TYPE to Key.TYPE_USER_STATUS,
                Key.SCREEN to "Access Permission",
                Key.PERMISSION_TYPE to permissionType,
                Key.VALUE to value
            )
            analyticsProvider.get().trackEvents(Event.USER_STORY_PERMISSION_CLICKED, event)
        }
    }

    fun trackEventStoryPermissionLoaded(merchantId: String?, permissionType: String) {
        merchantId?.let {
            val event = mapOf<String, Any>(
                Key.MERCHANT_ID to it,
                Key.TYPE to Key.TYPE_USER_STATUS,
                Key.SCREEN to "Access Permission",
                Key.PERMISSION_TYPE to permissionType,
            )
            analyticsProvider.get().trackEvents(Event.USER_STORY_PERMISSION_LOADED, event)
        }
    }

    fun trackEventStoryChooseImage(merchantId: String?, source: String, cameraType: String, flash: String) {
        merchantId?.let {
            val event = mapOf<String, Any>(
                Key.MERCHANT_ID to it,
                Key.SCREEN to Key.ADD_STORY,
                Key.TYPE to Key.TYPE_USER_STATUS,
                Key.SOURCE to source,
                Key.CAMERA_TYPE to cameraType,
                Key.FLASH to flash
            )
            analyticsProvider.get().trackEvents(Event.USER_STORY_CHOOSE_IMAGE, event)
        }
    }

    fun trackEventStoryChooseImageSuccess(merchantId: String?, source: String, imageCount: Int) {
        merchantId?.let {
            val event = mapOf<String, Any>(
                Key.MERCHANT_ID to it,
                Key.SCREEN to Key.ADD_STORY,
                Key.TYPE to Key.TYPE_USER_STATUS,
                Key.SOURCE to source,
                Key.IMAGE_COUNT to imageCount
            )
            analyticsProvider.get().trackEvents(Event.USER_STORY_CHOOSE_IMAGE_SUCCESS, event)
        }
    }

    fun trackEventStoryAddMore(merchantId: String?) {
        merchantId?.let {
            val event = mapOf<String, Any>(
                Key.MERCHANT_ID to it,
                Key.SCREEN to Key.ADD_STORY,
                Key.TYPE to Key.TYPE_USER_STATUS,
            )
            analyticsProvider.get().trackEvents(Event.USER_STORY_ADD_MORE_IMAGE, event)
        }
    }

    fun trackEventStoryDeleteImages(merchantId: String?) {
        merchantId?.let {
            val event = mapOf<String, Any>(
                Key.MERCHANT_ID to it,
                Key.SCREEN to Key.ADD_STORY,
                Key.TYPE to Key.TYPE_USER_STATUS,
            )
            analyticsProvider.get().trackEvents(Event.USER_STORY_DELETE_IMAGE, event)
        }
    }

    fun trackEventStoryAddCaptionStarted(merchantId: String?, storyCount: Int, position: Int) {
        merchantId?.let {
            val event = mapOf<String, Any>(
                Key.MERCHANT_ID to it,
                Key.SCREEN to Key.ADD_STORY,
                Key.TYPE to Key.TYPE_USER_STATUS,
                Key.COUNT to storyCount,
                Key.POSITION to position
            )
            analyticsProvider.get().trackEvents(Event.USER_STORY_ADD_CAPTION_STARTED, event)
        }
    }

    fun trackEventStoryAddCaptionSuccess(merchantId: String?, storyCount: Int, position: Int) {
        merchantId?.let {
            val event = mapOf<String, Any>(
                Key.MERCHANT_ID to it,
                Key.SCREEN to Key.ADD_STORY,
                Key.TYPE to Key.TYPE_USER_STATUS,
                Key.COUNT to storyCount,
                Key.POSITION to position
            )
            analyticsProvider.get().trackEvents(Event.USER_STORY_ADD_CAPTION_SUCCESS, event)
        }
    }

    fun trackEventStoryUploadStory(merchantId: String?, storyCount: Int) {
        merchantId?.let {
            val event = mapOf<String, Any>(
                Key.MERCHANT_ID to it,
                Key.SCREEN to Key.ADD_STORY,
                Key.TYPE to Key.TYPE_USER_STATUS,
                Key.COUNT to storyCount,
            )
            analyticsProvider.get().trackEvents(Event.USER_STORY_ADD_STORY_UPLOAD_CLICKED, event)
        }
    }

    fun trackEventStoryUploadStorySuccess(merchantId: String?, expiryTime: String, caption: String) {
        merchantId?.let {
            val event = mapOf<String, Any>(
                Key.MERCHANT_ID to it,
                Key.SCREEN to Key.ADD_STORY,
                Key.TYPE to Key.TYPE_USER_STATUS,
                Key.CAPTION to caption,
                Key.EXPIRY_TIME to expiryTime
            )
            analyticsProvider.get().trackEvents(Event.USER_STORY_ADD_STORY_UPLOAD_CLICKED_SUCCESS, event)
        }
    }
}
