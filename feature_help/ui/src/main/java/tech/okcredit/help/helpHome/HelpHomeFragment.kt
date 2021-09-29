package tech.okcredit.help.helpHome

import `in`.okcredit.analytics.PropertyValue.CONTACT_US
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.iid.FirebaseInstanceId
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.help.HelpActivity
import tech.okcredit.help.R
import tech.okcredit.help.analytics.HelpEventProperties.ABOUT_US
import tech.okcredit.help.analytics.HelpEventProperties.CHAT_WTH_US
import tech.okcredit.help.analytics.HelpEventProperties.CONTACT
import tech.okcredit.help.analytics.HelpEventProperties.HELP_HOME_SCREEN
import tech.okcredit.help.analytics.HelpEventProperties.HOW_TO_USE_QUESTIONS
import tech.okcredit.help.analytics.HelpEventProperties.PRIVACY_POLICY
import tech.okcredit.help.analytics.HelpEventProperties.WHATSAPP
import tech.okcredit.help.analytics.HelpEventTracker
import tech.okcredit.help.databinding.HelpHomeFragmentBinding
import tech.okcredit.help.helpHome.HelpHomeContract.*
import zendesk.chat.Chat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HelpHomeFragment : BaseFragment<State, ViewEvent, Intent>(
    "HelpHomeScreen",
    contentLayoutId = R.layout.help_home_fragment
) {

    companion object {
        const val TAG = "HelpHomeFragment"
        const val ARGUMENT_SHOW_BACK_BUTTON = "show_back_button"

        @JvmStatic
        fun newInstance(showBackButton: Boolean = true): HelpHomeFragment {
            return HelpHomeFragment().apply {
                this.arguments = Bundle().apply {
                    putBoolean(ARGUMENT_SHOW_BACK_BUTTON, showBackButton)
                }
            }
        }
    }

    internal val binding: HelpHomeFragmentBinding by viewLifecycleScoped(HelpHomeFragmentBinding::bind)

    private val checkWhatsAppPermission: PublishSubject<Boolean> = PublishSubject.create()

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var helpEventTracker: Lazy<HelpEventTracker>

    @Inject
    internal lateinit var communicationApi: Lazy<CommunicationRepository>

    private var showBackButton: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBundleData()
        initUi()
        initClickListeners()
    }

    private fun initBundleData() {
        arguments?.let { bundle ->
            showBackButton = bundle.getBoolean(ARGUMENT_SHOW_BACK_BUTTON, true)
        }
    }

    private fun initUi() {
        if (showBackButton) {
            binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Chat.INSTANCE.init(
            requireContext(),
            tech.okcredit.help.BuildConfig.ZENDESK_ACCOUNT_KEY,
            tech.okcredit.help.BuildConfig.ZENDESK_APP_ID
        )
        val pushProvider = Chat.INSTANCE.providers()?.pushNotificationsProvider()
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            pushProvider?.registerPushToken(it.token)
        }
    }

    private fun initClickListeners() {
        binding.cardHowToUse.setOnClickListener {
            helpEventTracker.get().trackWithEventName(HOW_TO_USE_QUESTIONS)
            pushIntent(Intent.ClickHelp)
        }
        binding.tvAboutOkc.setOnClickListener {
            helpEventTracker.get().trackWithEventName(ABOUT_US)
            pushIntent(Intent.AboutUsClick)
        }
        binding.tvPrivacy.setOnClickListener {
            helpEventTracker.get().trackWithEventName(PRIVACY_POLICY)
            pushIntent(Intent.PrivacyClick)
        }
        binding.btnChatWithUs.setOnClickListener {
            helpEventTracker.get().trackWithEventName(CHAT_WTH_US, HELP_HOME_SCREEN)
            pushIntent(Intent.ChatWithUsClick)
        }
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            checkWhatsAppPermission.doOnNext {
                helpEventTracker.get().trackContactOkCredit(CONTACT_US, WHATSAPP, getCurrentState().sourceScreen)
            }
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { Intent.OnWhatsAppPermissionCheck(it) }
        )
    }

    fun openWhatsApp(okCreditNumber: String) {
        communicationApi.get().goToWhatsApp(
            ShareIntentBuilder(
                shareText = getString(R.string.help_whatsapp_msg),
                phoneNumber = okCreditNumber
            )
        ).map {
            requireActivity().finish()
            startActivity(it)
        }.doOnError {
            if (it is IntentHelper.NoWhatsAppError)
                shortToast(R.string.whatsapp_not_installed)
            else
                shortToast(it.message ?: getString(R.string.err_default))
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    override fun render(state: State) {
    }

    private fun checkWhatsAppPermission() {
        Permission.requestContactPermission(
            activity as AppCompatActivity,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                    helpEventTracker.get().trackRuntimePermission(HELP_HOME_SCREEN, CONTACT, true)
                }

                @SuppressLint("CheckResult")
                override fun onPermissionGranted() {
                    Observable.timer(500, TimeUnit.MILLISECONDS)
                        .subscribe {
                            checkWhatsAppPermission.onNext(true)
                        }
                }

                @SuppressLint("CheckResult")
                override fun onPermissionDenied() {
                    Observable.timer(500, TimeUnit.MILLISECONDS)
                        .subscribe {
                            checkWhatsAppPermission.onNext(false)
                        }
                }

                override fun onPermissionPermanentlyDenied() {
                }
            }
        )
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GoToHelp -> HelpActivity.start(
                requireContext(),
                getCurrentState().sourceScreen
            )
            is ViewEvent.GoToAboutUsScreen -> legacyNavigator.get().goToAboutScreen(requireContext())
            is ViewEvent.GoToPrivacyScreen -> legacyNavigator.get().gotoPrivacyScreen(requireContext())
            is ViewEvent.CheckWhatsAppPermission -> checkWhatsAppPermission()
            is ViewEvent.GoToManualChatScreen -> legacyNavigator.get().goToManualChatScreen(requireContext())
            is ViewEvent.OpenWhatsApp -> openWhatsApp(event.mobile)
            is ViewEvent.GoToWhatsAppScreen -> legacyNavigator.get().goToWhatsAppScreen(requireActivity())
        }
    }
}
