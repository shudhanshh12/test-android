package tech.okcredit.help.help_details

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.PropertyValue.FEEDBACK
import `in`.okcredit.analytics.PropertyValue.FEEDBACK_LIKE
import `in`.okcredit.analytics.PropertyValue.FEEDBACK_UNLIKE
import `in`.okcredit.analytics.PropertyValue.SHARE_FEEDBACK
import `in`.okcredit.analytics.PropertyValue.SUBMIT_FEEDBACK
import `in`.okcredit.analytics.PropertyValue.TEXT
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.google.android.material.snackbar.Snackbar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.youtube_video_view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.showSoftKeyboard
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.help.R
import tech.okcredit.help.databinding.HelpdetailsScreenBinding
import tech.okcredit.help.help_details.views.LikeDislikeView
import tech.okcredit.help.help_details.views.YoutubeVideoView
import tech.okcredit.userSupport.data.LikeState
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HelpDetailsFragment :
    BaseFragment<HelpDetailsContract.State, HelpDetailsContract.ViewEvent, HelpDetailsContract.Intent>("HelpDetailsScreen"),
    LikeDislikeView.ILikeDislikeInterface,
    YoutubeVideoView.YoutubeListener {

    private var alert: Snackbar? = null

    private val likePublish: BehaviorSubject<String> = BehaviorSubject.create()
    private val dislikePublish: BehaviorSubject<String> = BehaviorSubject.create()
    private var submitFeedbackPublicSubject = PublishSubject.create<Pair<String, Int>>()

    private lateinit var helpDetailsController: HelpDetailsController

    private var youTubeState: String? = null

    @Inject
    internal lateinit var tracker: Tracker

    private var backPressed: Boolean = false

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    private lateinit var helpDetailsScreenBinding: HelpdetailsScreenBinding

    private val dataObserver by lazy {
        object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                helpDetailsScreenBinding.recyclerView.scrollToPosition(0)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        helpDetailsScreenBinding = HelpdetailsScreenBinding.inflate(layoutInflater)
        return helpDetailsScreenBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        helpDetailsController = HelpDetailsController(this)
        val linearLayoutManager = LinearLayoutManager(context)
        helpDetailsScreenBinding.recyclerView.layoutManager = linearLayoutManager
        helpDetailsScreenBinding.recyclerView.adapter = helpDetailsController.adapter
        helpDetailsController.adapter.registerAdapterDataObserver(dataObserver)
        initListeners()
    }

    override fun onDestroyView() {
        helpDetailsController.adapter.unregisterAdapterDataObserver(dataObserver)
        super.onDestroyView()
    }

    private fun initListeners() {
        helpDetailsScreenBinding.backButton.setOnClickListener {
            findNavController(this).navigateUp()
        }
    }

    override fun loadIntent(): UserIntent {
        return HelpDetailsContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            likePublish.map {
                HelpDetailsContract.Intent.OnLikeClick
            },
            submitFeedbackPublicSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    HelpDetailsContract.Intent.SubmitFeedback(it.first, it.second)
                },
            dislikePublish.map {
                HelpDetailsContract.Intent.OnDisLikeClick
            }

        )
    }

    private fun goBackAfterAnimation() {
        hideSoftKeyboard()
        helpDetailsScreenBinding.llVerificationSuccess.visibility = View.VISIBLE
        helpDetailsScreenBinding.lottieHelpOtpVerifySuccess.playAnimation()

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(dispatcherProvider.get().main()) {
                delay(1500)
                findNavController(this@HelpDetailsFragment).navigateUp()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun render(state: HelpDetailsContract.State) {

        Timber.d("==> $state")

        if (state.likeState == LikeState.NORMAL) {
            helpDetailsScreenBinding.btnHelpFeedback.visibility = View.GONE
            helpDetailsScreenBinding.tvFeedbackThanks.visibility = View.GONE
        }

        if (state.likeState == LikeState.LIKE) {
            helpDetailsScreenBinding.tvFeedbackThanks.visibility = View.VISIBLE
            helpDetailsScreenBinding.btnHelpFeedback.visibility = View.GONE
            helpDetailsScreenBinding.scrollView.post {
                helpDetailsScreenBinding.scrollView.scrollTo(0, helpDetailsScreenBinding.scrollView.bottom)
            }
        } else if (state.likeState == LikeState.DISLIKE) {
            helpDetailsScreenBinding.btnHelpFeedback.visibility = View.VISIBLE
            helpDetailsScreenBinding.tvFeedbackThanks.visibility = View.GONE
            helpDetailsScreenBinding.scrollView.post {
                helpDetailsScreenBinding.scrollView.scrollTo(0, helpDetailsScreenBinding.scrollView.bottom)
            }
        }

        try {
            KeyboardVisibilityEvent.setEventListener(requireContext() as Activity) { isOpen ->
                if (!isOpen) {
                    AnimationUtils.fadeOut(helpDetailsScreenBinding.dimLayout)
                    helpDetailsScreenBinding.bottomTextContainer.visibility = View.GONE
                    helpDetailsScreenBinding.btnHelpFeedback.visibility = View.VISIBLE
                    Completable
                        .timer(300, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            helpDetailsScreenBinding.btnHelpFeedback.visible()
                            helpDetailsScreenBinding.bottomTextContainer.gone()
                            helpDetailsScreenBinding.dimLayout.gone()
                        }
                } else {
                    helpDetailsScreenBinding.btnHelpFeedback.visibility = View.GONE
                    helpDetailsScreenBinding.bottomTextContainer.visibility = View.VISIBLE
                    helpDetailsScreenBinding.dimLayout.visibility = View.VISIBLE
                    AnimationUtils.fadeIn(helpDetailsScreenBinding.dimLayout)
                }
            }
        } catch (e: java.lang.Exception) {
        }

        helpDetailsScreenBinding.btnHelpFeedback.setOnClickListener {
            tracker.trackViewHelpItem_v2(
                type = getCurrentState().helpItemV2?.id,
                source = getCurrentState().sourceScreen,
                interaction = SHARE_FEEDBACK,
                method = FEEDBACK,
                format = TEXT,
                error = null
            )
            helpDetailsScreenBinding.bottomTextContainer.visibility = View.VISIBLE
            showSoftKeyboard(helpDetailsScreenBinding.bottomHelpFeedbackText)
        }

        helpDetailsScreenBinding.btnHelpSubmitFeedback.setOnClickListener {

            if (helpDetailsScreenBinding.bottomHelpFeedbackText.text.toString().isNotEmpty()) {
                tracker.trackViewHelpItem_v2(
                    type = getCurrentState().helpItemV2?.id,
                    source = getCurrentState().sourceScreen,
                    interaction = SUBMIT_FEEDBACK,
                    method = FEEDBACK,
                    format = TEXT,
                    error = null
                )
                submitFeedbackPublicSubject.onNext(helpDetailsScreenBinding.bottomHelpFeedbackText.text.toString() to 9)
            } else {
                shortToast(getString(R.string.please_provide_feedback))
            }
        }

        val epoxyVisibilityTracker = EpoxyVisibilityTracker()
        epoxyVisibilityTracker.attach(helpDetailsScreenBinding.recyclerView)

        helpDetailsController.setState(state)

        if (state.helpItemV2 != null) {
            helpDetailsScreenBinding.toolbarTitle.text = state.helpItemV2.title
            helpDetailsScreenBinding.toolbarTitle.visibility = View.VISIBLE
        } else {
            helpDetailsScreenBinding.toolbarTitle.visibility = View.GONE
        }

        // show/hide alert
        if (state.networkError or state.error or state.isAlertVisible) {
            alert = when {
                state.networkError -> view?.snackbar(
                    getString(R.string.home_no_internet_msg),
                    Snackbar.LENGTH_INDEFINITE
                )
                state.isAlertVisible -> view?.snackbar(state.alertMessage, Snackbar.LENGTH_INDEFINITE)
                else -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_INDEFINITE)
            }
            alert?.show()
        } else {
            alert?.dismiss()
        }
    }

    /****************************************************************
     * Lifecycle methods
     ****************************************************************/

    /****************************************************************
     * Listeners (for child views)
     ****************************************************************/

    override fun onDislikeClick(helpId: String) {
        Timber.d("<<<<DisLike $helpId")
        tracker.trackViewHelpItem_v2(
            type = helpId,
            source = getCurrentState().sourceScreen,
            interaction = FEEDBACK_UNLIKE,
            method = FEEDBACK,
            format = TEXT,
            error = null
        )
        dislikePublish.onNext(helpId)
    }

    override fun onLikeClick(helpId: String) {
        tracker.trackViewHelpItem_v2(
            type = helpId,
            source = getCurrentState().sourceScreen,
            interaction = FEEDBACK_LIKE,
            method = FEEDBACK,
            format = TEXT,
            error = null
        )

        likePublish.onNext(helpId)
    }

    private fun gotoLogin() {
        legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
    }

    private fun goBack() {
        requireActivity().finish()
    }

    private fun onBackClicked() {
        backPressed = true
        if (youTubeState.isNullOrBlank().not()) {
            if (youTubeState.equals(PlayerConstants.PlayerState.PAUSED.name) || youTubeState.equals(PlayerConstants.PlayerState.PLAYING.name)) {
                tracker.trackViewHelpItem_v2(
                    type = getCurrentState().helpItemV2?.id,
                    source = getCurrentState().sourceScreen,
                    interaction = PropertyValue.CLOSE,
                    method = PropertyValue.VIDEO,
                    format = PropertyValue.TEXT,
                    error = null
                )
            }
        }

        tracker.trackViewHelpItem_v2(
            type = getCurrentState().helpItemV2?.id,
            source = getCurrentState().sourceScreen,
            interaction = PropertyValue.COMPLETED,
            method = PropertyValue.TEXT,
            format = PropertyValue.TEXT,
            error = null
        )

        if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
            hideSoftKeyboard()
        } else {
            findNavController(this).navigateUp()
        }
    }

    override fun onBackPressed(): Boolean {
        onBackClicked()
        LocaleManager.fixWebViewLocale(requireContext())
        return true
    }

    override fun videoStartedListener(youTubeState: String) {
        this.youTubeState = youTubeState
        tracker.trackViewHelpItem_v2(
            type = getCurrentState().helpItemV2?.id,
            source = getCurrentState().sourceScreen,
            interaction = PropertyValue.STARTED,
            method = PropertyValue.VIDEO,
            format = PropertyValue.TEXT,
            error = null
        )
    }

    override fun videoPlayListener(youTubeState: String) {
        this.youTubeState = youTubeState
        tracker.trackViewHelpItem_v2(
            type = getCurrentState().helpItemV2?.id,
            source = getCurrentState().sourceScreen,
            interaction = PropertyValue.PLAY,
            method = PropertyValue.VIDEO,
            format = PropertyValue.TEXT,
            error = null
        )
    }

    override fun videoPauseListener(youTubeState: String) {
        this.youTubeState = youTubeState
        if (!backPressed) {
            tracker.trackViewHelpItem_v2(
                type = getCurrentState().helpItemV2?.id,
                source = getCurrentState().sourceScreen,
                interaction = PropertyValue.PAUSED,
                method = PropertyValue.VIDEO,
                format = PropertyValue.TEXT,
                error = null
            )
        }
    }

    override fun videoCompletedListener(youTubeState: String) {
        this.youTubeState = youTubeState
        tracker.trackViewHelpItem_v2(
            type = getCurrentState().helpItemV2?.id,
            source = getCurrentState().sourceScreen,
            interaction = PropertyValue.COMPLETED,
            method = PropertyValue.VIDEO,
            format = PropertyValue.TEXT,
            error = null
        )
    }

    override fun onFullScreenPressed(videoUrl: String) {
        youtube?.let {
            youtube.exitFullScreen()
            val actionData = HelpDetailsFragmentDirections.startYoutubePlayer()
            actionData.videoUrl = videoUrl
            if (findNavController(this).currentDestination?.id == R.id.helpdetails_screen) {
                findNavController(this).navigate(actionData)
            }
        }
    }

    override fun handleViewEvent(event: HelpDetailsContract.ViewEvent) {
        when (event) {
            HelpDetailsContract.ViewEvent.GotoLogin -> gotoLogin()
            HelpDetailsContract.ViewEvent.GoBack -> goBack()
            HelpDetailsContract.ViewEvent.GoBackAfterAnimation -> goBackAfterAnimation()
        }
    }
}
