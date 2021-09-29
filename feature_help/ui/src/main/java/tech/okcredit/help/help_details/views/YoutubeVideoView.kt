package tech.okcredit.help.help_details.views

import `in`.okcredit.fileupload._id.GlideApp
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import kotlinx.android.synthetic.main.youtube_video_view.view.*
import tech.okcredit.help.R
import tech.okcredit.userSupport.model.HelpItem

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class YoutubeVideoView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    private var player: YouTubePlayer? = null
    private var isFirstTime = true
    private lateinit var helpItem: HelpItem

    init {
        LayoutInflater.from(context).inflate(R.layout.youtube_video_view, this, true)
    }

    @ModelProp
    fun setHelpItemValue(helpItem: HelpItem) {
        this.helpItem = helpItem
    }

    @CallbackProp
    fun initYoutubePlayer(listener: YoutubeListener?) {
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

                if (helpItem.video_type == "youtube" && helpItem.video_url.isNullOrEmpty().not()) {
                    player = youTubePlayer
                    player?.cueVideo(helpItem.video_url!!, 0F)
                    player?.pause()
                    if (youtube.isFullScreen())
                        youtube.exitFullScreen()
                }

                playVideo()
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
        youtube.addFullScreenListener(object : YouTubePlayerFullScreenListener {
            override fun onYouTubePlayerEnterFullScreen() {
                helpItem.video_url?.let {
                    listener?.onFullScreenPressed(it)
                    player?.pause()
                }
            }

            override fun onYouTubePlayerExitFullScreen() {
            }
        })
    }

    interface YoutubeListener {
        fun videoStartedListener(youTubeState: String)
        fun videoPlayListener(youTubeState: String)
        fun videoPauseListener(youTubeState: String)
        fun videoCompletedListener(youTubeState: String)
        fun onFullScreenPressed(videoUrl: String)
    }

    fun playVideo() {
        if (helpItem.video_type == "youtube" && helpItem.video_url.isNullOrEmpty().not()) {
            player?.pause()
            gifImage.visibility = View.GONE
        } else if (helpItem.video_type == "gif" && helpItem.video_url.isNullOrEmpty().not()) {
            val placeHolder = ContextCompat.getDrawable(context, R.drawable.placeholder_image)
            GlideApp.with(context)
                .asGif()
                .load(helpItem.video_url)
                .placeholder(placeHolder)
                .into(gifImage)
            gifImage.clipToOutline = true
            youtube.visibility = View.GONE
        } else {
            youtube.visibility = View.GONE
            gifImage.visibility = View.GONE
        }
    }
}
