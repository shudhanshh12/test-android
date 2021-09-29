package tech.okcredit.home.ui.supplier_tab.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import kotlinx.android.synthetic.main.supplier_youtube_view.view.*
import tech.okcredit.home.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class YoutubeView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    private var listener: YoutubeListener? = null
    private var isFirstTime = true
    private var isYouTubePlayerLoadFirstTime = false

    init {
        LayoutInflater.from(context).inflate(R.layout.supplier_youtube_view, this, true)
    }

    @CallbackProp
    fun initYoutubePlayer(listener: YoutubeListener?) {
        this.listener = listener
    }

    interface YoutubeListener {
        fun videoStartedListener(youTubeState: String)
        fun videoPlayListener(youTubeState: String)
        fun videoPauseListener(youTubeState: String)
        fun videoCompletedListener(youTubeState: String)
    }

    @ModelProp
    fun playVideo(videoUrl: String) {

        youtube.clipToOutline = true
        youtube.addYouTubePlayerListener(object : YouTubePlayerListener {
            override fun onApiChange(youTubePlayer: YouTubePlayer) {
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
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
                if (isYouTubePlayerLoadFirstTime.not()) {
                    youTubePlayer.loadVideo(videoUrl, 0F)
                    youTubePlayer.pause()
                    isYouTubePlayerLoadFirstTime = true
                }
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
