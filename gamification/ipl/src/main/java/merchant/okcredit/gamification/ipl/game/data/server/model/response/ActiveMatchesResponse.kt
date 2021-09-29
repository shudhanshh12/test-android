package merchant.okcredit.gamification.ipl.game.data.server.model.response

import com.google.gson.annotations.SerializedName

data class ActiveMatches(
    @SerializedName("matches")
    val matches: List<Match>,
    @SerializedName("youtube_links")
    val youtubeLinks: List<YoutubeLinks>
)
