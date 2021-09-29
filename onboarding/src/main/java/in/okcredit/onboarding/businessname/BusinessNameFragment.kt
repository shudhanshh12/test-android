package `in`.okcredit.onboarding.businessname

import `in`.okcredit.onboarding.R
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics
import `in`.okcredit.onboarding.data.OnboardingPreferencesImpl
import `in`.okcredit.onboarding.databinding.OnboardingBusinessNameBinding
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.android.synthetic.main.onboarding_business_name.*
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.extensions.*
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BusinessNameFragment :
    BaseScreen<BusinessNameContract.State>("BusinessNameScreen", R.layout.onboarding_business_name),
    BusinessNameContract.Navigator {

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    lateinit var onboardingAnalytics: Lazy<OnboardingAnalytics>

    @Inject
    internal lateinit var dispatcherProvider: Lazy<DispatcherProvider>

    private val binding: OnboardingBusinessNameBinding by viewLifecycleScoped(OnboardingBusinessNameBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        initView()
        showSoftKeyboard(binding.etBusinessName)
        binding.rootView.setTracker(performanceTracker)
    }

    private fun initView() {
        binding.apply {
            if (etBusinessName.text.toString().isEmpty()) {
                fbBusinessNameSubmit.isEnabled = false
            }
        }
    }

    private fun initListeners() {
        binding.etBusinessName.onChange { name ->
            if (name.isNotEmpty()) {
                binding.apply {
                    ivCancelName.visible()
                    fbBusinessNameSubmit.elevation = resources.getDimension(R.dimen.view_4dp)
                    fbBusinessNameSubmit.backgroundTintList =
                        ColorStateList.valueOf(getColorFromAttr(R.attr.colorPrimary))
                    fbBusinessNameSubmit.isEnabled = true
                }
            } else {
                binding.apply {
                    ivCancelName.gone()
                    fbBusinessNameSubmit.elevation = 0f
                    fbBusinessNameSubmit.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.grey400)
                    fbBusinessNameSubmit.isEnabled = false
                }
            }
        }
        binding.ivCancelName.setOnClickListener {
            binding.etBusinessName.text = null
        }
    }

    override fun loadIntent(): UserIntent {
        return BusinessNameContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            businessNameButtonClick(),

            skipButtonClick()
        )
    }

    @SuppressLint("RestrictedApi")
    @AddTrace(name = Traces.RENDER_BUSINESS_NAME)
    override fun render(state: BusinessNameContract.State) {

        if (state.isLoading) {
            binding.apply {
                fbBusinessNameSubmit.hide()
                pbLoader.visible()
            }
        } else {
            binding.apply {
                fbBusinessNameSubmit.show()
                pbLoader.invisible()
            }
        }

        if (state.isMerchantFromCollectionCampaign) {
            tiBusinessName.hint = getString(R.string.your_name)
            tvTitle.text = getString(R.string.enter_your_name)
        } else {
            tiBusinessName.hint = getString(R.string.title_business_name)
            tvTitle.text = getString(R.string.enter_business_name)
        }

        when {
            state.networkError -> shortToast(R.string.no_internet_msg)
            state.error -> shortToast(R.string.err_default)
        }
    }

    @UiThread
    override fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    override fun goToHome() {
        activity?.runOnUiThread {
            activity?.let {
                legacyNavigator.goToHome(it)
                it.finishAffinity()
            }
        }
    }

    private fun skipButtonClick(): Observable<BusinessNameContract.Intent.NameSkipped>? {
        return binding.mbSkip.clicks()
            .throttleFirst(50, TimeUnit.MILLISECONDS)
            .doOnNext {
                onboardingAnalytics.get()
                    .trackNameScreen(OnboardingAnalytics.OnboardingEvent.ENTER_NAME_SKIPPED)
            }
            .map {
                BusinessNameContract.Intent.NameSkipped(OnboardingPreferencesImpl.PREF_INDIVIDUAL_KEY_NAME_SKIPPED, true)
            }
    }

    private fun businessNameButtonClick(): Observable<BusinessNameContract.Intent.BusinessName>? {
        return binding.fbBusinessNameSubmit.clicks()
            .throttleFirst(50, TimeUnit.MILLISECONDS)
            .doOnNext {
                onboardingAnalytics.get()
                    .trackNameScreen(OnboardingAnalytics.OnboardingEvent.NAME_ENTERED)
            }
            .map {
                BusinessNameContract.Intent.BusinessName(etBusinessName.text.toString().trim())
            }
    }
}
