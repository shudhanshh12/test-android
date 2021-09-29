package `in`.okcredit.frontend.ui.know_more

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.contract.Constants.DEFAULT_SUPPLIER_TUTORIAL_INTRO_VIDEO
import `in`.okcredit.frontend.R
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.addTo
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import com.bumptech.glide.Glide
import com.google.firebase.perf.metrics.AddTrace
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.know_more_fragment.*
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class KnowMoreFragment : BaseScreen<KnowMoreContract.State>("KnowMoreScreen"), KnowMoreContract.Navigator {

    private var disposable: Disposable? = null
    private var youtubeMediaPlayer: YouTubePlayer? = null
    private var isPlayAnalyticsTracked = false

    override fun gotoLogin() {
        activity?.run {
            legacyNavigator.goToLoginScreen(requireActivity())
            activity?.finishAffinity()
        }
    }

    private var submitFeedbackPublishSubject = PublishSubject.create<Pair<String, Int>>()

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var tracker: Tracker

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.know_more_fragment, container, false)
    }

    override fun loadIntent(): UserIntent {
        return KnowMoreContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            submitFeedbackPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    KnowMoreContract.Intent.SubmitFeedback(it.first, it.second)
                }
        )
    }

    override fun onDetach() {
        disposable?.dispose()
        super.onDetach()
    }

    @AddTrace(name = Traces.RENDER_KNOW_MORE)
    override fun render(state: KnowMoreContract.State) {

        try {
            KeyboardVisibilityEvent.setEventListener(requireContext() as Activity) { isOpen ->
                if (!isOpen) {
                    AnimationUtils.fadeOut(dimLayout)
                    bottom_text_container.visibility = View.GONE
                    btn_feedback.visibility = View.VISIBLE
                    Completable
                        .timer(300, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            btn_feedback.visibility = View.VISIBLE
                            bottom_text_container.visibility = View.GONE
                            dimLayout.visibility = View.GONE
                        }.addTo(autoDisposable)
                } else {
                    btn_feedback.visibility = View.GONE
                    bottom_text_container.visibility = View.VISIBLE
                    dimLayout.visibility = View.VISIBLE
                    AnimationUtils.fadeIn(dimLayout)
                }
            }
        } catch (e: java.lang.Exception) {
            tracker.trackError("Know More Screen", "Keyboard", e)
        }

        btn_feedback.setOnClickListener {
            bottom_text_container.visibility = View.VISIBLE
            KeyboardVisibilityEvent.showKeyboard(context, bottom_feedback_text, root_view)
            youtubeMediaPlayer?.pause()

            tracker.trackSelectFeedback(
                getCurrentState().accountID ?: "",
                getCurrentState().accountType?.toUpperCase() ?: ""
            )
        }

        btn_submit_feedback.setOnClickListener {
            tracker.trackSubmitFeedback(
                getCurrentState().accountID ?: "",
                getCurrentState().accountType?.toUpperCase() ?: ""
            )
            submitFeedbackPublishSubject.onNext(bottom_feedback_text.text.toString() to 10)
        }

        customer_name.text = state.customerName
        merchant_name.text = state.merchantName

        Glide.with(this).load(state.customerPic)
            .circleCrop()
            .placeholder(R.drawable.ic_contacts_placeholder)
            .error(R.drawable.ic_contacts_placeholder)
            .into(customer_iv)

        Glide.with(this).load(state.merchantPic)
            .circleCrop()
            .placeholder(R.drawable.ic_contacts_placeholder)
            .error(R.drawable.ic_contacts_placeholder)
            .into(merchant_iv)

        if (state.accountType == KnowMoreContract.Constants.Customer && state.commonLedgerSellerVideo.isNullOrBlank()
            .not()
        ) {
            setupYoutubeVideo(state.commonLedgerSellerVideo!!)
        } else if (state.accountType == KnowMoreContract.Constants.Supplier && state.commonLedgerBuyerVideo.isNullOrBlank()
            .not()
        ) {
            setupYoutubeVideo(state.commonLedgerBuyerVideo!!)
        }
    }

    fun setupYoutubeVideo(videoId: String) {
        this.lifecycle.addObserver(youtubePlayerView)
        youtubePlayerView.getPlayerUiController().showMenuButton(false)
        youtubePlayerView.addYouTubePlayerListener(object : YouTubePlayerListener {
            override fun onApiChange(youTubePlayer: YouTubePlayer) {
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                tracker.trackYoutube(
                    PropertyValue.COMMON_LEDGER,
                    PropertyValue.FAIL,
                    videoId,
                    "onInitializationFailure"
                )
                youTubePlayer.loadVideo(DEFAULT_SUPPLIER_TUTORIAL_INTRO_VIDEO, 0F)
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

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {

                when (state) {
                    PlayerConstants.PlayerState.PLAYING -> {
                        if (isPlayAnalyticsTracked.not()) {
                            tracker.trackYoutube(PropertyValue.COMMON_LEDGER, PropertyValue.STARTED, videoId, "")
                            isPlayAnalyticsTracked = true
                        }
                    }
                    PlayerConstants.PlayerState.ENDED -> {
                        tracker.trackYoutube(PropertyValue.COMMON_LEDGER, PropertyValue.ENDED, videoId, "")
                    }
                }
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
            }

            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
            }

            override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {
            }

            override fun onReady(youTubePlayer: YouTubePlayer) {
                youtubeMediaPlayer = youTubePlayer
                youtubeMediaPlayer?.cueVideo(videoId, 0F)
            }
        })
    }

    @UiThread
    override fun goBackAfterAnimation() {
        activity?.runOnUiThread {
            KeyboardVisibilityEvent.hideKeyboard(activity)
            llVerificationSuccess.visibility = View.VISIBLE
            lottieOtpVerifySuccess.playAnimation()
            disposable = Completable.timer(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    activity?.let {
                        requireActivity().finish()
                    }
                }
        }
    }

    override fun onBackPressed(): Boolean {
        if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
            KeyboardVisibilityEvent.hideKeyboard(activity)
        } else {
            requireActivity().finish()
        }
        LocaleManager.fixWebViewLocale(requireContext())
        return super.onBackPressed()
    }
}
