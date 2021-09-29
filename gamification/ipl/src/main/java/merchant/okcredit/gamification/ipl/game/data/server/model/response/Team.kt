package merchant.okcredit.gamification.ipl.game.data.server.model.response

import com.google.gson.annotations.SerializedName

data class Team(
    @SerializedName("logo_link")
    val logoLink: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("short_name")
    val shortName: String = "",
    @SerializedName("id")
    val id: String = "",
    @SerializedName("color_code")
    val colorCode: String = "",
    @SerializedName("prediction")
    val prediction: Int
)
