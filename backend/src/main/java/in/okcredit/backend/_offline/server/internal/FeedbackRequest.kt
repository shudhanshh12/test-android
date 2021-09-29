package `in`.okcredit.backend._offline.server.internal

import androidx.annotation.Keep

@Keep
data class FeedbackRequest(
    val rating: Int,
    val message: String,
    val feedback_type: String,
) {
    companion object {
        const val FEEDBACK_TYPE = "SUGGESTION"
    }
}
