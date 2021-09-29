package tech.okcredit.feedback.feedback

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.showSoftKeyboard
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.help.R
import tech.okcredit.help.databinding.FeedbackFragmentBinding
import tech.okcredit.userSupport.analytics.FeedbackEventTracker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FeedbackFragment :
    BaseFragment<FeedbackContract.State, FeedbackContract.ViewEvent, FeedbackContract.Intent>(
        "FeedbackScreen",
        contentLayoutId = R.layout.feedback_fragment
    ) {

    private var alert: Snackbar? = null
    internal val binding: FeedbackFragmentBinding by viewLifecycleScoped(FeedbackFragmentBinding::bind)
    private var submitFeedbackPublishSubject = PublishSubject.create<String>()

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var feedbackEventTracker: Lazy<FeedbackEventTracker>

    internal var job: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showSoftKeyboard(binding.etFeedback)
        initListeners()
    }

    override fun loadIntent(): UserIntent {
        return FeedbackContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            submitFeedbackPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    FeedbackContract.Intent.SubmitFeedback(it)
                }
        )
    }

    fun initListeners() {
        binding.submit.setOnClickListener {
            if (!binding.etFeedback.text.isNullOrEmpty())
                submitFeedbackPublishSubject.onNext(binding.etFeedback.text.toString())
            else
                context?.shortToast(getString(R.string.please_provide_feedback))
        }
    }

    @SuppressLint("RestrictedApi")
    override fun render(state: FeedbackContract.State) {

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

    private fun gotoLogin() {
        legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
    }

    private fun goBack() {
        requireActivity().finish()
    }

    private fun goBackAfterAnimation() {
        hideSoftKeyboard()
        binding.submit.gone()
        binding.llVerificationSuccess.visibility = View.VISIBLE
        binding.lottieHelpOtpVerifySuccess.playAnimation()

        job = viewLifecycleOwner.lifecycleScope.launch {
            withContext(dispatcherProvider.get().main()) {
                delay(1500)
                feedbackEventTracker.get()
                    .trackViewChat(PropertyValue.DRAWER, FeedbackEventTracker.FEEDBACK_INTERACTION_SUBMIT)
                requireActivity().finish()
            }
        }
    }

    override fun handleViewEvent(event: FeedbackContract.ViewEvent) {
        when (event) {
            is FeedbackContract.ViewEvent.GotoLogin -> gotoLogin()
            is FeedbackContract.ViewEvent.GoBack -> goBack()
            is FeedbackContract.ViewEvent.GoBackAfterAnimation -> goBackAfterAnimation()
        }
    }

    override fun onBackPressed(): Boolean {
        feedbackEventTracker.get()
            .trackViewChat(PropertyValue.DRAWER, FeedbackEventTracker.FEEDBACK_INTERACTION_BACK)
        return super.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }
}
