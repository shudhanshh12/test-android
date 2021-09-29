package `in`.okcredit.onboarding.autolang

import `in`.okcredit.onboarding.R
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics
import `in`.okcredit.onboarding.autolang.AutoLangContract.*
import `in`.okcredit.onboarding.contract.OnboardingConstants
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import `in`.okcredit.onboarding.contract.autolang.Language
import `in`.okcredit.onboarding.contract.autolang.LanguageSelectedListener
import `in`.okcredit.onboarding.databinding.AutoLangFragmentBinding
import `in`.okcredit.onboarding.language.views.LanguageBottomSheet
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.addTo
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.truecaller.android.sdk.ITrueCallback
import com.truecaller.android.sdk.TrueError
import com.truecaller.android.sdk.TrueProfile
import com.truecaller.android.sdk.TruecallerSDK
import com.truecaller.android.sdk.TruecallerSdkScope
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.utils.MobileUtils
import tech.okcredit.app_contract.LegacyNavigator
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import android.content.Intent as AndroidIntent

class AutoLangFragment : BaseFragment<State, ViewEvent, Intent>(
    "AutoLangScreen"
) {

    private val numberReadPopupSubject = PublishSubject.create<Unit>()
    private val goToMobileScreenSubject = PublishSubject.create<Unit>()
    private val trueCallerRegisterSubject = PublishSubject.create<Pair<String, String>>()
    private val submitMobilePublishSubject = PublishSubject.create<String>()

    private var isTrueCallerInitialized = false

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var onboardingAnalytics: Lazy<OnboardingAnalytics>

    @Inject
    lateinit var onboardingPreferences: Lazy<OnboardingPreferences>

    @Inject
    lateinit var localeManager: Lazy<LocaleManager>

    private var _binding: AutoLangFragmentBinding? = null
    internal val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = AutoLangFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        initView()
        iniClickListener()
        onboardingAnalytics.get().trackViewMobileScreen()
        binding.rootView.setTracker(performanceTracker)
        binding.tvTerms.text = spannableFromHtml(getString(R.string.register_user_agreement))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().window?.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            submitMobilePublishSubject
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map { Intent.SubmitMobile(it) },

            numberReadPopupSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { Intent.NumberReadPopUp },

            trueCallerRegisterSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { Intent.TrueCallerLogin(it.first, it.second) },

            goToMobileScreenSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    showSoftKeyboard(binding.etMobileNumber)
                    Intent.LoadingState(false)
                }
        )
    }

    @SuppressLint("RestrictedApi")
    override fun render(state: State) {

        with(binding.etMobileNumber) {
            if (text.isNullOrBlank()) {
                setText(state.mobileNumber)
                setSelection(state.mobileNumber.length)
            }
        }

        // TODO : fix IO operation done on main thread
        state.languages?.apply {
            val userSelectedLanguage = onboardingPreferences.get().getUserSelectedLanguage()
                .takeIf { it.isNotEmpty() }
                ?: return@apply

            firstOrNull { it.languageCode == userSelectedLanguage }
                ?.also { setButtonLanguage(it) }
        }
    }

    private fun initView() {
        binding.tvTerms.movementMethod = LinkMovementMethod.getInstance()
        initTextWatcher()
    }

    private fun initTextWatcher() {
        // This is necessary as the views are grouped and so is their visibility
        binding.ivCancelNumber.isVisible = binding.etMobileNumber.text.toString().isNotBlank()

        binding.etMobileNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val str = editable.toString()
                binding.ivCancelNumber.isVisible = str.isNotBlank()

                if (str.length > 11) {
                    val parsedMobile = MobileUtils.parseMobile(str)
                    binding.etMobileNumber.setText(parsedMobile)
                    binding.etMobileNumber.setSelection(parsedMobile.length)
                } else if (str.length > MOBILE_NUMBER_LENGTH) {
                    var mobileString = str
                    mobileString = mobileString.replaceFirst("^0+(?!$)".toRegex(), "")

                    if (mobileString.length == MOBILE_NUMBER_LENGTH) {

                        binding.etMobileNumber.setText(mobileString)
                        binding.etMobileNumber.setSelection(MOBILE_NUMBER_LENGTH)
                    } else {

                        binding.etMobileNumber.setText(
                            str.substring(0, MOBILE_NUMBER_LENGTH)
                        )
                        binding.etMobileNumber.setSelection(MOBILE_NUMBER_LENGTH)
                    }
                } else if (str.length >= MOBILE_NUMBER_LENGTH) {
                    binding.fbNumberSubmit.elevation = resources.getDimension(R.dimen.view_4dp)
                    binding.fbNumberSubmit.backgroundTintList =
                        ColorStateList.valueOf(context!!.getColorFromAttr(R.attr.colorPrimary))
                    binding.fbNumberSubmit.isEnabled = true
                } else {
                    binding.fbNumberSubmit.elevation = 0f
                    binding.fbNumberSubmit.backgroundTintList =
                        ContextCompat.getColorStateList(context!!, R.color.grey400)
                    binding.fbNumberSubmit.isEnabled = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /****************************************************************
     * Lifecycle methods
     ****************************************************************/

    private fun iniClickListener() {
        binding.mbSelectLanguage.setOnClickListener {
            onboardingAnalytics.get().trackChangeLanguageButtonClicked()

            binding.etMobileNumber.clearFocus()
            binding.tiMobileNumber.clearFocus()
            it.requestFocus()

            lifecycleScope.launch {
                // provide some time for the keyboard to hide
                delay(300)
                showLanguageBottomSheet()
            }
        }

        binding.fbNumberSubmit.setOnClickListener {
            if (binding.etMobileNumber.text.toString().length != MOBILE_NUMBER_LENGTH) {
                shortToast(getString(R.string.invalid_mobile))
            } else {
                onboardingAnalytics.get().trackEnteredMobileScreen()
                submitMobilePublishSubject.onNext(binding.etMobileNumber.text.toString())
                hideSoftKeyboard()
            }
        }

        binding.ivCancelNumber.setOnClickListener {
            if (binding.etMobileNumber.text.isNullOrBlank().not()) {
                onboardingAnalytics.get().trackMobileNumberCleared()
                binding.etMobileNumber.text = null
            }
        }
    }

    private fun goBack() {
        if (isStateInitialized().not()) {
            requireActivity().finish()
        }

        if (getCurrentState().verifySuccess.not()) {
            requireActivity().finish()
        }
    }

    private fun showLanguageBottomSheet(
        launchPhoneNumberFlowAfter: Boolean = false,
    ) {
        val languageBottomSheet = LanguageBottomSheet()
        if (languageBottomSheet.isVisible) return

        languageBottomSheet.show(
            requireActivity().supportFragmentManager,
            LanguageBottomSheet.TAG
        )
        languageBottomSheet.setListener(object : LanguageSelectedListener {
            private var isLanguageChanged = false
            private val weekBinding = WeakReference(binding)

            override fun onDismissed() {
                if (launchPhoneNumberFlowAfter) {
                    pushIntent(Intent.NumberReadPopUp)
                } else {
                    // only focus on this if not showing TC
                    weekBinding.get()?.tiMobileNumber?.requestFocusFromTouch()
                }
            }

            override fun onSelected(language: Language, dialog: AppCompatDialogFragment) {
                if (isAdded.not()) return

                isLanguageChanged = true
                languageBottomSheet.dismiss()
                onLanguageSelected(language, isSetFromLanguageSheet = true)

                // Reload languages
                pushIntent(Intent.Load)
            }
        })
    }

    // internal to avoid syntheticAccessor
    internal fun onLanguageSelected(language: Language, isSetFromLanguageSheet: Boolean = false) {
        pushIntent(Intent.LanguageSelected(language.languageCode, isSetFromLanguageSheet))
        updateBaseLanguage(language.languageCode)

        setTextsLanguage(language)
        if (isSetFromLanguageSheet) {
            setButtonLanguage(language)
            onboardingAnalytics.get().trackSelectLanguage(language.languageCode)
        }
    }

    private fun setButtonLanguage(language: Language) {
        binding.apply {
            with(mbSelectLanguage) {
                text = language.languageTitle
                val refSize = dpToPixel(12f).toInt()
                val arrowDrawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_down_black)?.apply {
                    setBounds(0, 0, refSize, refSize)
                }
                val charDrawable = ContextCompat.getDrawable(context, language.letterDrawable)?.apply {
                    // maintain aspect ratio
                    val width = refSize.takeUnless { intrinsicHeight > 0 && intrinsicWidth > 0 }
                        ?: (intrinsicWidth * refSize) / intrinsicHeight

                    setBounds(0, 0, width, refSize)
                    setColorFilter(
                        ContextCompat.getColor(context, R.color.green_primary),
                        PorterDuff.Mode.SRC_IN
                    )
                }
                setCompoundDrawables(charDrawable, null, arrowDrawable, null)
                compoundDrawablePadding = refSize
            }
        }
    }

    private fun setTextsLanguage(language: Language) {
        binding.apply {
            tvTitle.text = getLocalisedString(language.languageCode, R.string.enter_mobile_number)
            tiMobileNumber.hint = getLocalisedString(language.languageCode, R.string.mobile_number)
            tvTerms.text = Html.fromHtml(
                getLocalisedString(language.languageCode, R.string.register_user_agreement).toString()
            )
            successMessage.text = getLocalisedString(language.languageCode, R.string.verification_success)
        }
    }

    private fun showTrueCallerDialogWithPrerequisiteCheck(trueCallerInstalled: Boolean) {
        // Early exit to otp based phone number validation as true-caller is not available
        if (!trueCallerInstalled) {
            onboardingAnalytics.get().logBreadcrumb("Skipping TrueCaller due to app not installed")
            goToMobileScreenSubject.onNext(Unit)
        } else {
            if (firebaseRemoteConfig.get().getBoolean(SKIP_TC_ONBOARDING)) {
                onboardingAnalytics.get().logBreadcrumb("Skipping TrueCaller due to FirebaseRemoteConfig")
                goToMobileScreenSubject.onNext(Unit)
            } else {
                try {
                    showTrueCallerDialogInternal(trueCallerInstalled)
                } catch (e: Exception) {
                    RecordException.recordException(e)
                    goToMobileScreenSubject.onNext(Unit)
                }
            }
        }
    }

    private fun showTrueCallerDialogInternal(isTrueCallerInstalled: Boolean) {
        onboardingAnalytics.get()
            .logBreadcrumb("Trying TrueCaller as app is installed and RemoteConfig TC skip is false")
        val sdkCallback = object : ITrueCallback {
            override fun onVerificationRequired() {
                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                    goToMobileScreenSubject.onNext(Unit)
                }
            }

            override fun onSuccessProfileShared(@NonNull trueProfile: TrueProfile) {
                onboardingAnalytics.get().setTruecallerUserProperty(
                    trueProfile.firstName,
                    trueProfile.lastName,
                    trueProfile.countryCode,
                    trueProfile.city,
                    trueProfile.email,
                    trueProfile.street,
                    trueProfile.avatarUrl,
                    trueProfile.gender
                )
                onboardingAnalytics.get()
                    .trackSelectMobile(OnboardingAnalytics.OnboardingPropertyValue.TRUE_CALLER)
                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                    pushIntent(Intent.CheckMobileStatus(trueProfile.phoneNumber))
                    trueCallerRegisterSubject.onNext(trueProfile.payload to trueProfile.signature)
                }
            }

            override fun onFailureProfileShared(@NonNull trueError: TrueError) {
                when (trueError.errorType) {
                    TrueError.ERROR_TYPE_CONTINUE_WITH_DIFFERENT_NUMBER -> {
                        onboardingAnalytics.get().trackSkip(
                            OnboardingAnalytics.OnboardingPropertyValue.TRUE_CALLER,
                            OnboardingAnalytics.OnboardingPropertyValue.BUTTON
                        )
                    }
                    TrueError.ERROR_TYPE_USER_DENIED -> {
                        onboardingAnalytics.get().trackSkip(
                            OnboardingAnalytics.OnboardingPropertyValue.TRUE_CALLER,
                            OnboardingAnalytics.OnboardingPropertyValue.SCREEN
                        )
                    }
                    else -> {
                        onboardingAnalytics.get().trackTrueCallerFailure(
                            OnboardingAnalytics.OnboardingPropertyValue.TRUE_CALLER,
                            trueError.errorType
                        )
                    }
                }
                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                    goToMobileScreenSubject.onNext(Unit)
                }
            }
        }

        val trueScope = TruecallerSdkScope.Builder(requireContext(), sdkCallback)
            .apply {
                consentMode(TruecallerSdkScope.CONSENT_MODE_BOTTOMSHEET)
                footerType(TruecallerSdkScope.FOOTER_TYPE_SKIP)
                consentTitleOption(TruecallerSdkScope.SDK_CONSENT_TITLE_VERIFY)
                buttonColor(ContextCompat.getColor(requireContext(), R.color.primary))
            }.build()

        TruecallerSDK.init(trueScope)
        isTrueCallerInitialized = true

        if (TruecallerSDK.getInstance().isUsable) {
            hideSoftKeyboard()
            onboardingAnalytics.get().trackNumberReadDisplayed(OnboardingAnalytics.OnboardingEvent.TRUECALLER_DISPLAYED)
            TruecallerSDK.getInstance().setLocale(Locale(localeManager.get().getLanguage()))
            onboardingAnalytics.get().logBreadcrumb("Truecaller ready to schedule profile fetch")

            TruecallerSDK.getInstance().getUserProfile(this)
            onboardingAnalytics.get().logBreadcrumb("Truecaller scheduled profile fetch")
            pushIntent(Intent.LoadingState(true))
        } else {
            if (isTrueCallerInstalled) {
                onboardingAnalytics.get().trackTrueCallerFailure(
                    OnboardingAnalytics.OnboardingPropertyValue.TRUE_CALLER,
                    TrueError.ERROR_TYPE_INVALID_ACCOUNT_STATE
                )
            }
            goToMobileScreenSubject.onNext(Unit)
        }
    }

    @SuppressLint("CheckResult")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: AndroidIntent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val isTrueCallerInstalled = isStateInitialized()
            .takeIf { it }?.run { getCurrentState().isTrueCallerInstalled } ?: false

        if (isTrueCallerInitialized && isTrueCallerInstalled) {
            TruecallerSDK.getInstance().onActivityResultObtained(requireActivity(), resultCode, data)
        }
    }

    private fun goToAppLockAuthentication() {
        context?.let { context ->
            legacyNavigator.goToSystemAppLockScreenFromLogin(context)
            activity?.finishAffinity()
        }
    }

    private fun goToSyncDataScreen() {
        binding.apply {
            llEnterNumber.gone()
            ivLogo.gone()
            mbSelectLanguage.gone()
            llVerificationSuccess.visible()
            lottieOtpVerifySuccess.playAnimation()
        }
        Completable.timer(1500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                activity?.let {
                    legacyNavigator.goToSyncScreen(it)
                    it.finishAffinity()
                }
            }.addTo(autoDisposable)
    }

    private fun goToEnterBusinessNameScreen() {
        binding.apply {
            llEnterNumber.gone()
            ivLogo.gone()
            mbSelectLanguage.gone()
            llVerificationSuccess.visible()
            lottieOtpVerifySuccess.playAnimation()
        }
        Completable.timer(1500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                activity?.let {
                    onboardingAnalytics.get().trackNameScreen(OnboardingAnalytics.OnboardingEvent.ENTER_NAME_SCREEN)
                    legacyNavigator.goToOnboardBusinessNameScreen(it)
                    it.finishAffinity()
                }
            }
    }

    private fun gotoLogin() {
        legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
    }

    private fun goToOtpScreen(mobile: String) {
        onboardingAnalytics.get().trackViewRequestOTP(OnboardingAnalytics.OnboardingPropertyValue.MANUAL)
        legacyNavigator.goToOtpScreen(
            requireActivity(),
            mobile,
            OnboardingConstants.FLAG_DEFAULT,
            isGooglePopupSelected = false
        )
        requireActivity().finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun makeLinkClickable(strBuilder: SpannableStringBuilder, span: URLSpan) {
        val start = strBuilder.getSpanStart(span)
        val end = strBuilder.getSpanEnd(span)
        val flags = strBuilder.getSpanFlags(span)

        val clickable: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                span.url?.also { legacyNavigator.goToWebViewScreen(requireActivity(), it) }
            }
        }
        strBuilder.setSpan(clickable, start, end, flags)
        strBuilder.removeSpan(span)
    }

    private fun spannableFromHtml(html: String): SpannableStringBuilder {
        val sequence: CharSequence = Html.fromHtml(html)
        val strBuilder = SpannableStringBuilder(sequence)

        strBuilder.getSpans(0, sequence.length, URLSpan::class.java)
            .forEach { makeLinkClickable(strBuilder, it) }

        return strBuilder
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GoToLogin -> gotoLogin()

            is ViewEvent.GoToSyncDataScreen -> goToSyncDataScreen()

            is ViewEvent.GoBack -> goBack()

            is ViewEvent.GoToAppLockAuthentication -> goToAppLockAuthentication()

            is ViewEvent.ShowTrueCallerDialog -> showTrueCallerDialogWithPrerequisiteCheck(event.isTrueCallerInstalled)

            is ViewEvent.GoToOtpScreen -> goToOtpScreen(event.mobile)

            is ViewEvent.GoToEnterNameScreen -> goToEnterBusinessNameScreen()

            is ViewEvent.ShowLanguageSelector -> showLanguageBottomSheet(
                event.launchPhoneNumberFlowAfter
            )

            is ViewEvent.SetLanguage -> onLanguageSelected(event.language)
        }
    }

    companion object {
        private const val SKIP_TC_ONBOARDING = "skip_truecaller_onboarding"
        private const val MOBILE_NUMBER_LENGTH = 10
    }
}
