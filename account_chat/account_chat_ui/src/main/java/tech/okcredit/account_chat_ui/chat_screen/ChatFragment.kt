package tech.okcredit.account_chat_ui.chat_screen

import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.addTo
import `in`.okcredit.shared.utils.exhaustive
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.google.android.material.snackbar.Snackbar
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import it.sephiroth.android.library.xtooltip.Tooltip
import kotlinx.android.synthetic.main.chat_fragment.*
import tech.okcredit.account_chat_sdk.AccountChatTracker
import tech.okcredit.account_chat_ui.R
import tech.okcredit.account_chat_ui.databinding.ChatFragmentBinding
import tech.okcredit.account_chat_ui.message_layout.SendMessageContract
import tech.okcredit.account_chat_ui.message_list_layout.MessageListContract
import tech.okcredit.android.base.extensions.snackbar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChatFragment :
    BaseFragment<ChatContract.State, ChatContract.ViewEvent, ChatContract.Intent>(
        "ChatScreen",
        R.layout.chat_fragment
    ),
    SendMessageContract.Callback,
    MessageListContract.Callback {

    companion object {
        private const val REQUEST_CALL_PHONE: Int = 1
    }

    private var toolTip: Tooltip? = null
    private var alert: Snackbar? = null
    lateinit var binding: ChatFragmentBinding
    private val callButtonClicks: PublishSubject<Unit> = PublishSubject.create()
    private val pageViewedSubject: BehaviorSubject<String> = BehaviorSubject.create()

    @Inject
    internal lateinit var tracker: Lazy<AccountChatTracker>

    @Inject
    internal lateinit var imageLoader: IImageLoader

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ChatFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val shareAppPromotion: PublishSubject<Pair<Bitmap, String>> = PublishSubject.create()

    override fun loadIntent(): UserIntent {
        return ChatContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            shareAppPromotion
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map {
                    ChatContract.Intent.ShareAppPromotion(
                        it.first,
                        it.second
                    )
                },
            callButtonClicks
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map { ChatContract.Intent.GoToPhoneDialer },
            pageViewedSubject.map {
                ChatContract.Intent.PageViewed(it)
            }
        )
    }

    override fun render(state: ChatContract.State) {
        setDataForChildViews(state)
        setProfileForAccount(state, state.accountName)
        setBannerForUnregisteredAccount(state)
        showAlertForErrors(state)
        canShowChatToolTip(state)
    }

    private fun canShowChatToolTip(state: ChatContract.State) {
        if (state.canShowChatTooltip) {
            tracker.get().trackTipShown(
                getCurrentState().accountId,
                AccountChatTracker.Values.CHAT_SCREEN,
                getCurrentState().role
            )
            toolTip = ChatHelper.makeToolTip(requireActivity(), binding.sendMessageLayout, state.accountName)
            toolTip?.doOnFailure {
                toolTip = null
            }?.doOnHidden {
                toolTip = null
            }?.show(binding.sendMessageLayout, Tooltip.Gravity.TOP, true)
        }
    }

    private fun setBannerForUnregisteredAccount(state: ChatContract.State) {
        if (state.isRegistered.not()) {
            binding.registeredAccountContainer.visibility = View.VISIBLE
            binding.bannerUnderline.visibility = View.VISIBLE
            binding.bannerText.visibility = View.VISIBLE
            binding.bannerText.text =
                Html.fromHtml(getString(R.string.chat_send_invite, state.accountName))
            binding.registeredAccountContainer.setOnClickListener {
                takeScreenshot()
            }
        } else {
            binding.registeredAccountContainer.visibility = View.GONE
            binding.bannerUnderline.visibility = View.GONE
        }
    }

    private fun setProfileForAccount(
        state: ChatContract.State,
        accountName: String?,
    ) {
        state.accountName?.let {
            binding.profileName.text = it
            val defaultPic = TextDrawable
                .builder()
                .buildRound(
                    it.substring(0, 1).toUpperCase(),
                    ColorGenerator.MATERIAL.getColor(state.accountName)
                )
            if (state.accountPic != null) {
                imageLoader.context(this)
                    .load(state.accountPic)
                    .placeHolder(defaultPic)
                    .scaleType(IImageLoader.CIRCLE_CROP)
                    .into(binding.profileImage)
                    .build().addTo(autoDisposable = autoDisposable)
            } else {
                binding.profileImage.setImageDrawable(defaultPic)
            }
        }
    }

    private fun setDataForChildViews(state: ChatContract.State) {
        state.accountId?.let { accountId ->
            send_message_layout.setData(accountId, state.role, state.accountName, state.recevierRole, this)
            message_list_layout.setData(
                accountId,
                state.unreadMessageCount,
                state.firstUnseenMessageId,
                this
            )
        }
    }

    private fun showAlertForErrors(state: ChatContract.State) {
        if (state.error or state.isAlertVisible) {
            alert = when {
                state.isAlertVisible -> view?.snackbar(state.alertMessage, Snackbar.LENGTH_INDEFINITE)
                state.error -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_INDEFINITE)
                else -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_INDEFINITE)
            }
            alert?.show()
        } else {
            alert?.dismiss()
        }
    }

    private fun takeScreenshot() {
        Completable.fromAction {
            val screenShot = requireActivity().window.decorView.rootView
            val bitmap =
                Bitmap.createBitmap(screenShot.width, screenShot.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            screenShot.draw(canvas)
            tracker.get().trackInviteClicked(
                getCurrentState().accountId,
                AccountChatTracker.Values.CHAT_SCREEN,
                getCurrentState().role
            )
            shareAppPromotion.onNext(bitmap to requireContext().getString(R.string.whats_app_link_promotion))
        }.subscribeOn(Schedulers.io()).subscribe().addTo(autoDisposable)
    }

    fun gotoLogin() {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.callToolbar.setOnClickListener {
            makeCall()
        }
        pageViewedSubject.onNext(AccountChatTracker.Values.CHAT_SCREEN)
        binding.rootView.setTracker(performanceTracker)
    }

    private fun makeCall() {
        if (!isStateInitialized()) {
            return
        }
        callButtonClicks.onNext(Unit)
    }

    private fun openWhatsAppPromotionShare(intent: Intent) {
        activity?.runOnUiThread {
            startActivity(intent)
        }
    }

    private fun gotoCallCustomer(mobile: String?) {
        if (context?.let {
            ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.CALL_PHONE
                )
        } != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_CALL_PHONE
            )
        } else {
            redirectToCallIntent(mobile)
        }
    }

    private fun redirectToCallIntent(mobile: String?) {
        activity?.let {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse(getString(R.string.call_template, mobile))
            activity?.startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PHONE) {
            if (permissions[0] == Manifest.permission.CALL_PHONE &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                redirectToCallIntent(getCurrentState().mobile)
            }
        }
    }

    override fun onMessageSent() {
        message_list_layout.onMessageSent()
    }

    override fun handleViewEvent(event: ChatContract.ViewEvent) {
        when (event) {
            ChatContract.ViewEvent.GotoLogin -> gotoLogin()
            is ChatContract.ViewEvent.OpenWhatsAppPromotionShare -> openWhatsAppPromotionShare(intent = event.intent)
            is ChatContract.ViewEvent.GotoCallCustomer -> gotoCallCustomer(mobile = event.mobile)
        }.exhaustive
    }

    override fun isMessageListEmpty(isEmpty: Boolean) {
    }
}
