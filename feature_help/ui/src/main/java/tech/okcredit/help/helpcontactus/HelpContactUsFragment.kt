package tech.okcredit.help.helpcontactus

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.contract.Constants
import `in`.okcredit.merchant.device.DeviceRepository
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.setGroupOnClickListener
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.help.R
import tech.okcredit.help.databinding.HelpContactUsFragmentBinding
import zendesk.chat.Chat
import zendesk.chat.ChatConfiguration
import zendesk.chat.ChatEngine
import zendesk.chat.PreChatFormFieldStatus
import zendesk.messaging.MessagingActivity
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HelpContactUsFragment :
    BaseScreen<HelpContactUsContract.State>("HelpContactUsScreen", R.layout.help_contact_us_fragment),
    HelpContactUsContract.Navigator {

    private var alert: Snackbar? = null

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var tracker: Tracker

    @Inject
    internal lateinit var deviceRepository: Lazy<DeviceRepository>

    private var whatsAppUsPublishSubject = PublishSubject.create<Boolean>()
    private val whatsAppPublisher = PublishSubject.create<Unit>()
    private val chatWithUsPublisher = PublishSubject.create<Unit>()
    private val emailUsPublisher = PublishSubject.create<Unit>()

    private val binding: HelpContactUsFragmentBinding by viewLifecycleScoped(HelpContactUsFragmentBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.help_contact_us_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.whatsappusContainer.setGroupOnClickListener {
            whatsAppPublisher.onNext(Unit)
        }
        binding.chatContainer.setGroupOnClickListener {
            chatWithUsPublisher.onNext(Unit)
        }
        binding.emailusContainer.setGroupOnClickListener {
            emailUsPublisher.onNext(Unit)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Chat.INSTANCE.init(requireContext(), tech.okcredit.help.BuildConfig.ZENDESK_ACCOUNT_KEY, tech.okcredit.help.BuildConfig.ZENDESK_APP_ID)
        val pushProvider = Chat.INSTANCE.providers()?.pushNotificationsProvider()
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            pushProvider?.registerPushToken(it.token)
        }
    }

    override fun loadIntent(): UserIntent {
        return HelpContactUsContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            whatsAppPublisher
                .doOnNext {
                    tracker.trackContactOkCredit(
                        PropertyValue.CONTACT_US,
                        PropertyValue.WHATSAPP,
                        getCurrentState().sourceScreen
                    )
                }
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { HelpContactUsContract.Intent.ContactUs },

            chatWithUsPublisher
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { HelpContactUsContract.Intent.ContactUs },

            emailUsPublisher
                .doOnNext {
                    tracker.trackContactOkCredit(
                        PropertyValue.CONTACT_US,
                        PropertyValue.EMAIL,
                        getCurrentState().sourceScreen
                    )
                }
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { HelpContactUsContract.Intent.EmailUs },

            whatsAppUsPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    HelpContactUsContract.Intent.WhatsApp(it)
                }
        )
    }

    @SuppressLint("RestrictedApi")
    override fun render(state: HelpContactUsContract.State) {
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
        binding.chatContainer.isVisible = state.isManualChatEnabled
        binding.whatsappusContainer.isVisible = state.isManualChatEnabled.not()
    }

    override fun onEmailClicked() {
        val emailIntent = Intent(
            Intent.ACTION_SENDTO,
            Uri.fromParts(
                "mailto", Constants.HELP_EMAIL, null
            )
        )
        startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)))
    }

    override fun openWhatsApp(helpNumber: String) {
        val uri = Uri.parse("whatsapp://send")
            .buildUpon()
            .appendQueryParameter("text", getString(R.string.help_whatsapp_msg))
            .appendQueryParameter("phone", "91$helpNumber")
            .build()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        val packageManager = activity?.packageManager
        if (packageManager != null && intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            shortToast(getString(R.string.whatsapp_not_installed))
        }
    }

    override fun goToWhatsAppOptIn() {
        legacyNavigator.goToWhatsAppScreen(requireActivity())
    }

    override fun goToManualChatScreen() {
        legacyNavigator.goToManualChatScreen(requireContext())
    }

    override fun onContactUsClicked() {
        Permission.requestContactPermission(
            activity as AppCompatActivity,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                    tracker.trackRuntimePermission(PropertyValue.HELP_SCREEN, PropertyValue.CONTACT, true)
                }

                @SuppressLint("CheckResult")
                override fun onPermissionGranted() {
                    Observable.timer(500, TimeUnit.MILLISECONDS)
                        .subscribe {
                            whatsAppUsPublishSubject.onNext(true)
                        }
                }

                @SuppressLint("CheckResult")
                override fun onPermissionDenied() {
                    Observable.timer(500, TimeUnit.MILLISECONDS)
                        .subscribe {
                            whatsAppUsPublishSubject.onNext(false)
                        }
                }
            }
        )
    }

    /****************************************************************
     * Navigation
     ****************************************************************/
    @UiThread
    override fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    @UiThread
    override fun goBack() {
        activity?.runOnUiThread {
            requireActivity().finish()
        }
    }

    companion object {
        fun getManualChatIntent(context: Context): Intent {
            val chatConfiguration = ChatConfiguration.builder()
                .withNameFieldStatus(PreChatFormFieldStatus.OPTIONAL)
                .withEmailFieldStatus(PreChatFormFieldStatus.HIDDEN)
                .build()
            val manualChatIntent = MessagingActivity.builder()
                .withEngines(ChatEngine.engine())
                .withToolbarTitle(context.getString(R.string.help_contact_us_screen_title))
                .withBotLabelString(context.getString(R.string.help_contact_us_screen_label))
                .intent(context, chatConfiguration)
            manualChatIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY
            return manualChatIntent
        }
    }
}
