package `in`.okcredit.supplier.supplier_limit_warning_bottomsheet

import `in`.okcredit.supplier.BuildConfig.ZENDESK_ACCOUNT_KEY
import `in`.okcredit.supplier.BuildConfig.ZENDESK_APP_ID
import `in`.okcredit.supplier.R
import `in`.okcredit.supplier.databinding.PaymentLimitWarningBottomSheetBinding
import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.iid.FirebaseInstanceId
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.withClickableSpan
import tech.okcredit.app_contract.LegacyNavigator
import zendesk.chat.Chat
import javax.inject.Inject

class PaymentLimitWarningBottomSheet : BottomSheetDialogFragment() {

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    private val binding: PaymentLimitWarningBottomSheetBinding by viewLifecycleScoped(
        PaymentLimitWarningBottomSheetBinding::bind
    )

    override fun onStart() {
        super.onStart()
        val deviceWidth = requireContext().resources.displayMetrics.widthPixels
        dialog?.window?.setLayout(deviceWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return PaymentLimitWarningBottomSheetBinding.inflate(inflater, container, false).root
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        initiateChat()
    }

    private fun initiateChat() {
        Chat.INSTANCE.init(
            requireContext(),
            ZENDESK_ACCOUNT_KEY,
            ZENDESK_APP_ID
        )
        val pushProvider = Chat.INSTANCE.providers()?.pushNotificationsProvider()
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            pushProvider?.registerPushToken(it.token)
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogThemeWithKeyboard)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi()
    }

    private fun setUi() {
        binding.apply {
            mbDone.setOnClickListener {
                dismiss()
            }
            tvWarningMsg.text = getWarningMsg()
            tvWarningMsg.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun getWarningMsg(): SpannableStringBuilder {
        val clickHereString =
            SpannableStringBuilder(getString(R.string.click_here))
        clickHereString.withClickableSpan(0, clickHereString.length) {
            goToManualChatScreen()
        }
        clickHereString.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.indigo_primary
                )
            ),
            0,
            clickHereString.length,
            0
        )
        return SpannableStringBuilder(getString(R.string.supplier_txn_limit_reached_warning)).append(clickHereString)
            .append(" ").append(getString(R.string.click_here_reach_customer_support))
    }

    private fun goToManualChatScreen() {
        legacyNavigator.get().goToManualChatScreen(requireActivity())
        dismiss()
    }
}
