package merchant.okcredit.gamification.ipl.game.data.server.model.response

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import merchant.okcredit.gamification.ipl.utils.IplUtils

@Keep
data class BoosterQuestion(
    @SerializedName("question")
    val question: Question,
    @SerializedName("status")
    val status: Boolean
) {
    @Keep
    @Parcelize
    data class Question(
        @SerializedName("id")
        val id: String,
        @SerializedName("mcqs")
        val mcqs: List<String>,
        @SerializedName("multiplier")
        val multiplier: Int,
        @SerializedName("question_type")
        val questionType: QuestionType, //  MCQ = 1; OpenEnded = 2; ClientTrackedTask = 3; BackendTrackedTask = 4;
        @SerializedName("question_sub_type")
        val questionSubType: Int,
        @SerializedName("symbolic_id")
        val symbolicId: String?,
        @SerializedName("text")
        val text: String,
        @SerializedName("cta")
        val cta: String?,
        @SerializedName("youtube_link")
        val youtubeLink: String?,
        @SerializedName("expiry_time")
        val expiryTime: Long,
        @SerializedName("app_link")
        val appLink: String
    ) : Parcelable {
        fun isExpired(): Boolean {
            return IplUtils.hasGameExpired(expiryTime)
        }

        fun formattedQuestionType(): String {
            return when (questionType) {
                QuestionType.MCQ -> "MCQ"
                QuestionType.OpenEnded -> "Open Ended"
                QuestionType.ClientTrackedTask -> "Client Tracked Task"
                QuestionType.BackendTrackedTask -> "Backend Tracked Task"
            }
        }

        fun formattedQuestionSubtypeType(): String {
            return when (questionType) {
                QuestionType.BackendTrackedTask -> {
                    when (questionSubType) {
                        1 -> "Fill Business Type"
                        2 -> "Fill Business Category"
                        3 -> "Fill Address details"
                        4 -> "Set-up bank account"
                        5 -> "Update Email"
                        6 -> "Update Merchant Update"
                        7 -> "Update Merchant Name"
                        8 -> "Update Business Name"
                        9 -> "Add Customer"
                        10 -> "Add First Transaction"
                        11 -> "Add Second Transaction"
                        else -> "$questionSubType"
                    }
                }
                QuestionType.ClientTrackedTask -> {
                    when (questionSubType) {
                        1 -> "Rate us on Play store"
                        2 -> "Give feedback on Play store"
                        3 -> "Share business card"
                        4 -> "Refer the App"
                        5 -> "Add OkCredit contact"
                        6 -> "Install OkShop"
                        7 -> "Voice data collection"
                        else -> "$questionSubType"
                    }
                }
                else -> {
                    "$questionSubType"
                }
            }
        }
    }
}

enum class QuestionType(val value: Int) {
    @SerializedName("1")
    MCQ(1),

    @SerializedName("2")
    OpenEnded(2),

    @SerializedName("3")
    ClientTrackedTask(3),

    @SerializedName("4")
    BackendTrackedTask(4)
}
