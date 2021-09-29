package `in`.okcredit.communication_inappnotification.contract

import androidx.annotation.Keep
import java.util.*

abstract class InAppNotification(
    val id: String,
    val screenName: String,
    val delay: Int, // seconds
    val minAppBuildNumber: Int,
    val maxAppBuildNumber: Int,
    val priority: Int,
    val expiryTime: Long,
    val kind: String,
    val displayStatus: DisplayStatus,
    val name: String,
    val source: String
) {
    companion object {
        const val KEY_POLYMORPHISM = "kind"
        const val DEFAULT_DELAY = 3
        const val DEFAULT_PRIORITY = 1
        const val DEFAULT_TARGET_INDEX = 0
    }

    open fun getTypeForAnalyticsTracking() = kind
}

@Keep
enum class DisplayStatus {
    TO_BE_DISPLAYED, DISPLAYED, NOT_DISPLAYED
}

enum class TargetIdType {
    ID, TAG, CONTENT_DESCRIPTION, TEXT;

    companion object {
        fun String.toTargetIdType() = valueOf(this.toUpperCase(Locale.ENGLISH))
    }
}
