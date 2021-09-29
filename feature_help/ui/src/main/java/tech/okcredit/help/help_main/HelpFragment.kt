package tech.okcredit.help.help_main

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.addTo
import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.help.R
import tech.okcredit.help.analytics.HelpEventProperties.CHAT_WTH_US
import tech.okcredit.help.analytics.HelpEventProperties.HELP_MAIN_SCREEN
import tech.okcredit.help.databinding.HelpFragmentBinding
import tech.okcredit.help.help_main.HelpContract.*
import tech.okcredit.help.help_main.views.HelpMainItem
import tech.okcredit.help.help_main.views.HelpSectionView
import zendesk.chat.Chat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HelpFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "Help Main Screen",
        R.layout.help_fragment
    ),
    HelpMainItem.IHelpExpandClick,
    HelpSectionView.OnHelpSectionItemTitleClick {

    private var alert: Snackbar? = null

    private val sectionItemClickPublishSubject: PublishSubject<String> = PublishSubject.create()

    private val sectionSubject: PublishSubject<String> = PublishSubject.create()

    private val checkWhatsAppPermission: PublishSubject<Boolean> = PublishSubject.create()

    private val helpController: HelpController = HelpController(this)

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    @Inject
    internal lateinit var communicationApi: Lazy<CommunicationRepository>

    private val binding: HelpFragmentBinding by viewLifecycleScoped(HelpFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.grey50
                )
            )
        )
        initListeners()
    }

    private val dataObserver by lazy {
        object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.recyclerView.scrollToPosition(0)
            }
        }
    }

    private fun initListeners() {
        binding.btnChatWithUs.setOnClickListener {
            tracker.get().trackViewHelpItem_v2(
                type = CHAT_WTH_US,
                screen = HELP_MAIN_SCREEN,
                source = getCurrentState().sourceScreen,
                interaction = PropertyValue.STARTED,
                method = PropertyValue.TEXT,
                format = PropertyValue.TEXT
            )
            pushIntent(Intent.ChatWithUsClick)
        }

        Chat.INSTANCE.init(
            requireContext(),
            tech.okcredit.help.BuildConfig.ZENDESK_ACCOUNT_KEY,
            tech.okcredit.help.BuildConfig.ZENDESK_APP_ID
        )
        val pushProvider = Chat.INSTANCE.providers()?.pushNotificationsProvider()
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            pushProvider?.registerPushToken(it.token)
        }

        binding.backButton.setOnClickListener { requireActivity().finish() }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = helpController.adapter
            EpoxyVisibilityTracker().attach(this)
            helpController.adapter.registerAdapterDataObserver(dataObserver)
        }
    }

    override fun onDestroyView() {
        helpController.adapter.unregisterAdapterDataObserver(dataObserver)
        super.onDestroyView()
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            sectionSubject
                .map {
                    if (getCurrentState().expandedId != it) {
                        Intent.MainItemClick(it, true)
                    } else {
                        if (getCurrentState().isExpanded)
                            Intent.MainItemClick(it, false)
                        else
                            Intent.MainItemClick(it, true)
                    }
                },

            sectionItemClickPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { Intent.OnSectionItemClick(it) },

            checkWhatsAppPermission
                .doOnNext {
                    tracker.get().trackContactOkCredit(
                        PropertyValue.CONTACT_US,
                        PropertyValue.WHATSAPP,
                        getCurrentState().sourceScreen
                    )
                }
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { Intent.OnWhatsAppPermissionCheck(it) }
        )
    }

    @SuppressLint("RestrictedApi")
    override fun render(state: State) {

        helpController.setState(state)

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

    private fun goToHelpItem(helpItemId: String) {
        val actionData = HelpFragmentDirections.actionStartHelpDetailScreen()
        actionData.helpItemId = helpItemId
        actionData.source = getCurrentState().sourceScreen
        findNavController(this).navigate(actionData)
    }

    override fun onItemClick(helpItemId: String) {
        tracker.get().trackViewHelpItem_v2(
            type = helpItemId,
            source = getCurrentState().sourceScreen,
            interaction = PropertyValue.STARTED,
            method = PropertyValue.TEXT,
            format = PropertyValue.TEXT
        )
        sectionItemClickPublishSubject.onNext(helpItemId)
    }

    override fun OnExpandClick(item: String) {
        if (getCurrentState().expandedId != item)
            tracker.get().trackViewHelpTopic_v2(item, getCurrentState().sourceScreen, PropertyValue.EXPAND)
        else
            tracker.get().trackViewHelpTopic_v2(item, getCurrentState().sourceScreen, PropertyValue.COLLAPSE)
        sectionSubject.onNext(item)
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
                shortToast(it.message ?: getString(R.string.default_error_msg))
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    private fun goToWhatsAppOptIn() {
        legacyNavigator.get().goToWhatsAppScreen(requireActivity())
    }

    private fun openDefaultFaq() {
        Observable
            .timer(500, TimeUnit.MILLISECONDS)
            .subscribe {
                if (!getCurrentState().isExpanded) {
                    getCurrentState().help?.let {
                        tracker.get()
                            .trackViewHelpTopic_v2(it[0].id, getCurrentState().sourceScreen, PropertyValue.EXPAND)
                        sectionSubject.onNext(it[0].id)
                    }
                }
            }.addTo(autoDisposable)
    }

    private fun goToManualChatScreen() {
        legacyNavigator.get().goToManualChatScreen(requireActivity())
    }

    private fun checkWhatsAppPermission() {
        Permission.requestContactPermission(
            activity as AppCompatActivity,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                    tracker.get().trackRuntimePermission(PropertyValue.HELP_SCREEN, PropertyValue.CONTACT, true)
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
            }
        )
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.OpenWhatsApp -> openWhatsApp(event.helpNumber)
            is ViewEvent.GoToWhatsAppOptIn -> goToWhatsAppOptIn()
            is ViewEvent.GoToManualChatScreen -> goToManualChatScreen()
            is ViewEvent.CheckWhatsAppPermission -> checkWhatsAppPermission()
            is ViewEvent.OpenDefaultFaq -> openDefaultFaq()
            is ViewEvent.GoBack -> requireActivity().finish()
            is ViewEvent.GotoHelpItem -> goToHelpItem(event.secId)
        }
    }
}
