package tech.okcredit.home.ui.payables_onboarding.video_backed_carousel_fragment

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.home.R
import tech.okcredit.home.databinding.FragmentVideoBackedPageBinding
import tech.okcredit.home.ui.payables_onboarding.video_backed_carousel_fragment.VideoBackedCarouselContract.*
import javax.inject.Inject

class VideoBackedCarouselFragment : BaseFragment<State, ViewEvent, Intent>(
    "VideoBackedCarouselFragment",
    R.layout.fragment_video_backed_page
) {

    @Inject
    lateinit var supplierAnalyticsEvents: Lazy<SupplierAnalyticsEvents>

    companion object {

        private const val DEFAULT_VIDEO_URL = "rDA3Dwp18dw"
        private const val URL_EXTRA = "url_extra"
        private const val STRING_RESOURCE_EXTRA = "string_resource_extra"

        fun newInstance(url: String, @StringRes string: Int): VideoBackedCarouselFragment {
            val fragment = VideoBackedCarouselFragment()
            fragment.arguments = Bundle().also {
                it.putString(URL_EXTRA, url)
                it.putInt(STRING_RESOURCE_EXTRA, string)
            }
            return fragment
        }
    }

    private val onHomeTabSelectionStateChange = MutableLiveData<Unit>()

    internal val binding: FragmentVideoBackedPageBinding by viewLifecycleScoped(FragmentVideoBackedPageBinding::bind)

    private var isYouTubePlayerLoadFirstTime = true

    private val youtubeListener = object : YouTubePlayerListener {
        private var currentSecond: Int = 0
        private var videoDuration: Int = 0

        override fun onApiChange(youTubePlayer: YouTubePlayer) {}

        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
            currentSecond = second.toInt()
        }

        override fun onError(youTubePlayer: YouTubePlayer, error: PlayerError) {}

        override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, playbackQuality: PlaybackQuality) {}

        override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: PlaybackRate) {}

        override fun onReady(youTubePlayer: YouTubePlayer) {
            if (isYouTubePlayerLoadFirstTime) {
                youTubePlayer.cueVideo(
                    arguments?.getString(URL_EXTRA) ?: DEFAULT_VIDEO_URL,
                    0F
                )
                isYouTubePlayerLoadFirstTime = false
            }

            onHomeTabSelectionStateChange.observe(viewLifecycleOwner) { youTubePlayer.pause() }
        }

        override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerState) {
            supplierAnalyticsEvents.get()
                .trackPayablesOnboardingCarouselVideoStateChanged(state.name, currentSecond, videoDuration)
        }

        override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
            videoDuration = duration.toInt()
        }

        override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {}

        override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            titleText.setText(
                arguments?.getInt(STRING_RESOURCE_EXTRA)
                    ?: R.string.payables_onboarding_carousel_string_video
            )
            videoView.addYouTubePlayerListener(youtubeListener)
            lifecycle.addObserver(videoView)
        }
    }

    override fun onPause() {
        super.onPause()
        onHomeTabSelectionStateChange.postValue(Unit)
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun render(state: State) {}

    override fun handleViewEvent(event: ViewEvent) {}
}
