package merchant.okcredit.gamification.ipl.game.ui.youtube

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.YoutubeFragmentBinding
import tech.okcredit.android.base.extensions.setStatusBarColor

class YoutubeFragment() : Fragment() {

    companion object {

        fun newInstance() = YoutubeFragment()
    }

    private var _binding: YoutubeFragmentBinding? = null
    internal val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = YoutubeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStatusBarColor(R.color.grey800)
        val youtubeLink = arguments?.getString(YoutubeActivity.EXTRA_YOUTUBE_LINK)

        binding.layout.setOnClickListener {
            requireActivity().finish()
        }
        youtubeLink?.let { initializePlayer(it) }
    }

    private fun initializePlayer(link: String) {
        binding.youtubePlayerView.apply {
            lifecycle.addObserver(this)
            this.clipToOutline = true
            getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.loadVideo(link, 0f)
                    youTubePlayer.addListener(object : YouTubePlayerListener {
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
                        }

                        override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                            if (state == PlayerConstants.PlayerState.ENDED) {
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
            })
        }
    }
}
