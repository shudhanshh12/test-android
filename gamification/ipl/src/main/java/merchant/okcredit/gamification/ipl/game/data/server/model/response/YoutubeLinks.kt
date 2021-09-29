package merchant.okcredit.gamification.ipl.game.data.server.model.response

import com.google.gson.annotations.SerializedName

data class YoutubeLinks(
    @SerializedName("lang")
    val language: String = "",
    @SerializedName("link")
    val link: String = ""
)
