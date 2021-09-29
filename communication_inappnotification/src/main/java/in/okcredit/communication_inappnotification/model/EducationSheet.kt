package `in`.okcredit.communication_inappnotification.model

import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class EducationSheet(
    id: String,
    screenName: String,
    delay: Int,
    minAppBuildNumber: Int,
    maxAppBuildNumber: Int,
    priority: Int,
    expiryTime: Long,
    displayStatus: DisplayStatus,
    name: String,
    source: String,

    val template: String,
    val imageWidth: Int,
    val imageHeight: Int,
    val imageUrl: String?,
    val title: String?,
    val subtitle: String?,
    val primaryBtn: ActionButton?,
    val secondaryBtn: ActionButton?,
    val tertiaryBtn: ActionButton?
) : InAppNotification(
    id = id,
    screenName = screenName,
    delay = delay,
    minAppBuildNumber = minAppBuildNumber,
    maxAppBuildNumber = maxAppBuildNumber,
    priority = priority,
    expiryTime = expiryTime,
    kind = KIND,
    displayStatus = displayStatus,
    name = name,
    source = source
) {
    companion object {
        const val KIND = "education_sheet"

        const val DEFAULT_IMAGE_SIZE = 144
        const val IMAGE_NO_TOP_MARGIN = 0
        const val IMAGE_WIDTH_MATCH_PARENT = -1
        const val BUTTON_ICON_SIZE = 20
        const val GLIDE_THUMBNAIL_SIZE_MULTIPLIER = 0.2f
    }

    override fun getTypeForAnalyticsTracking() = "$kind/$template"
}
