package `in`.okcredit.frontend.ui.expense_manager.views

import `in`.okcredit.frontend.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.android.synthetic.main.youtube_video_view.view.*

class ExpenseYoutubeView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attributeSet, defStyle) {

    private var listener: YoutubeListener? = null
    private var isFirstTime = true

    init {
        LayoutInflater.from(context).inflate(R.layout.supplier_youtube_view, this, true)
        initYouTube()
    }

    fun initYoutubePlayer(listener: YoutubeListener?) {
        this.listener = listener
        initYouTube()
    }

    interface YoutubeListener {
        fun onYouTubeReady(youTubePlayerView: YouTubePlayerView, youTubePlayer: YouTubePlayer)
        fun videoStartedListener(youTubeState: String)
        fun videoPlayListener(youTubeState: String)
        fun videoPauseListener(youTubeState: String)
        fun videoCompletedListener(youTubeState: String)
        fun videoOnError(youTubeState: String)
    }

    private fun initYouTube() {
        youtube.clipToOutline = true
        youtube.addYouTubePlayerListener(object : YouTubePlayerListener {
            override fun onApiChange(youTubePlayer: YouTubePlayer) {
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                listener?.videoOnError("Error")
            }

            override fun onPlaybackQualityChange(
                youTubePlayer: YouTubePlayer,
                playbackQuality: PlayerConstants.PlaybackQuality
            ) {
            }

            override fun onPlaybackRateChange(
                youTubePlayer: YouTubePlayer,
                playbackRate: PlayerConstants.PlaybackRate
            ) {
            }

            override fun onReady(youTubePlayer: YouTubePlayer) {
                listener?.onYouTubeReady(youtube, youTubePlayer)
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                if (state.name == "PLAYING") {
                    if (!isFirstTime) {
                        listener?.videoPlayListener(state.name)
                    }
                    if (isFirstTime) {
                        listener?.videoStartedListener(state.name)
                        isFirstTime = false
                    }
                } else if (state.name == "PAUSED") {
                    listener?.videoPauseListener(state.name)
                } else if (state.name == "ENDED") {
                    listener?.videoCompletedListener(state.name)
                }
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
            }

            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
            }

            override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {
            }
        })
    }
}
