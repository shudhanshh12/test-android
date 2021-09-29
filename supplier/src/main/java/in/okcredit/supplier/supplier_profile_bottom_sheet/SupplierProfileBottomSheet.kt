package `in`.okcredit.supplier.supplier_profile_bottom_sheet

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.backend.utils.SmsHelper
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.supplier.R
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents
import `in`.okcredit.supplier.databinding.SupplierProfileBottomSheetBinding
import `in`.okcredit.supplier.utils.getPaidDayText
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.account_chat_contract.ChatNavigator
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.setGroupOnClickListener
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.TextDrawableUtils
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import javax.inject.Inject

class SupplierProfileBottomSheet :
    BaseBottomSheetWithViewEvents<SupplierProfileContract.State, SupplierProfileContract.ViewEvents, SupplierProfileContract.Intent>(
        "SupplierProfileBottomSheet"
    ) {

    private val binding: SupplierProfileBottomSheetBinding by viewLifecycleScoped(SupplierProfileBottomSheetBinding::bind)

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var chatNavigator: Lazy<ChatNavigator>

    @Inject
    lateinit var communicationApi: Lazy<CommunicationRepository>

    @Inject
    lateinit var smsHelper: Lazy<SmsHelper>

    @Inject
    lateinit var supplierAnalyticsEvents: Lazy<SupplierAnalyticsEvents>

    companion object {
        const val ARG_SUPPLIER_ID_PROFILE_PAGE = "supplier_id"

        fun newInstance(
            supplierId: String
        ): SupplierProfileBottomSheet {
            val args = Bundle()
            args.putString(ARG_SUPPLIER_ID_PROFILE_PAGE, supplierId)
            val fragment = SupplierProfileBottomSheet()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return SupplierProfileBottomSheetBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.apply {
            grpCall.setGroupOnClickListener {
                pushIntent(SupplierProfileContract.Intent.ActionOnCall)
            }

            grpWhatsapp.setGroupOnClickListener {
                pushIntent(SupplierProfileContract.Intent.SendWhatsAppReminder)
            }

            grpChat.setGroupOnClickListener {
                pushIntent(SupplierProfileContract.Intent.RedirectToChatScreen)
            }

            grpPayOnline.setGroupOnClickListener {
                getCurrentState().let { state ->
                    supplierAnalyticsEvents.get().trackSupplierOnlinePaymentClick(
                        accountId = state.supplier?.id ?: "",
                        dueAmount = state.supplier?.balance.toString(),
                        screen = SupplierAnalyticsEvents.SupplierPropertyValue.PROFILE_POP_UP,
                        relation = SupplierAnalyticsEvents.SupplierPropertyValue.SUPPLIER,
                        riskType = "",
                        isCashbackMessageVisible = false
                    )
                }
                pushIntent(SupplierProfileContract.Intent.GoToSupplierPaymentScreen)
            }
        }
    }

    private fun setUpCall(mobile: String) {
        Permission.requestCallPermission(
            activity as AppCompatActivity,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                }

                override fun onPermissionGranted() {
                    val intent = Intent(Intent.ACTION_CALL)
                    intent.data = Uri.parse(getString(R.string.call_template, mobile))
                    startActivity(intent)
                    dismissAllowingStateLoss()
                }

                override fun onPermissionDenied() {}
            }
        )
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            Observable.just(SupplierProfileContract.Intent.Load)
        )
    }

    override fun render(state: SupplierProfileContract.State) {
        state.supplier?.let {
            binding.name.text = state.supplier.name
            setAmountInfo(it)
            setLastPaymentInfoText(it)
            setProfileIcon(state, it.profileImage)

            if (state.supplier.mobile.isNullOrEmpty()) {
                setUiWhenMobileIsNotAdded(state)
            } else {
                setUiWhenMobileExist(state)
            }
        }
    }

    private fun setProfileIcon(state: SupplierProfileContract.State, profileImage: String?) {
        val defaultPic = TextDrawableUtils.getRoundTextDrawable(state.supplier?.name ?: "A")
        Glide.with(this)
            .load(profileImage)
            .circleCrop()
            .placeholder(defaultPic)
            .placeholder(defaultPic)
            .into(binding.profileImage)
    }

    private fun setUiWhenMobileIsNotAdded(state: SupplierProfileContract.State) {
        state.supplier?.let {
            supplierAnalyticsEvents.get().trackSupplierProfilePopUpDisplayed(
                accountId = state.supplier.id,
                screen = SupplierAnalyticsEvents.SupplierPropertyValue.SUPPLIER,
                relation = SupplierAnalyticsEvents.SupplierPropertyValue.SUPPLIER,
                numberAvailable = false,
                paymentDue = state.supplier.balance < 0
            )
            binding.apply {
                callIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_icon_add_phone
                    )
                )

                callText.text = requireContext().getString(R.string.custpr_add_mobile)

                chatIcon.gone()
                chatText.gone()

                whatsappIcon.gone()
                whatsappText.gone()

                ivPayOnline.gone()
                tvPayOnline.gone()
            }
        }
    }

    private fun setUiWhenMobileExist(state: SupplierProfileContract.State) {
        state.supplier?.let {
            supplierAnalyticsEvents.get().trackSupplierProfilePopUpDisplayed(
                accountId = state.supplier.id,
                screen = SupplierAnalyticsEvents.SupplierPropertyValue.SUPPLIER,
                relation = SupplierAnalyticsEvents.SupplierPropertyValue.SUPPLIER,
                numberAvailable = true,
                paymentDue = state.supplier.balance < 0
            )
            binding.apply {
                callIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_call
                    )
                )
                callText.text = requireContext().getString(R.string.call)

                chatIcon.isVisible = state.isChatEnabled
                chatText.isVisible = state.isChatEnabled

                whatsappIcon.visible()
                whatsappText.visible()

                ivPayOnline.isVisible = state.isPaymentEnabled
                tvPayOnline.isVisible = state.isPaymentEnabled
            }
        }
    }

    private fun setLastPaymentInfoText(supplier: Supplier) {
        binding.lastPaymentDate.text = requireContext().getPaidDayText(supplier)
    }

    private fun setAmountInfo(supplier: Supplier) {
        var color = R.color.tx_payment
        var dueLabel = requireContext().getString(R.string.advance)
        if (supplier.balance < 0L) {
            color = R.color.red_primary
            dueLabel = requireContext().getString(R.string.due)
        }
        binding.balanceInfo.text = String.format("%s ₹%s", dueLabel, CurrencyUtil.formatV2(supplier.balance))
        binding.balanceInfo.setTextColor(ContextCompat.getColor(requireContext(), color))
    }

    private fun goToLogin() {
        legacyNavigator.get().goToLoginScreenForAuthFailure(requireContext())
    }

    override fun handleViewEvent(event: SupplierProfileContract.ViewEvents) {
        when (event) {
            is SupplierProfileContract.ViewEvents.GoToLogin -> goToLogin()
            is SupplierProfileContract.ViewEvents.ShowToast -> shortToast(event.msg)
            is SupplierProfileContract.ViewEvents.AddSupplierMobile -> {
                legacyNavigator.get()
                    .gotoSupplierProfileForAddingMobile(requireActivity(), event.supplierId)
            }
            is SupplierProfileContract.ViewEvents.CallToSupplier -> setUpCall(event.mobile)
            is SupplierProfileContract.ViewEvents.ShareWhatsappReminder -> shareWhatsappReminder(
                event.mobile,
                event.name
            )
            is SupplierProfileContract.ViewEvents.RedirectToChatScreen -> redirectToChatScreen()
            is SupplierProfileContract.ViewEvents.GoToSupplierPaymentScreen -> gotoSupplierPaymentScreen(event.supplierId)
        }
    }

    private fun shareWhatsappReminder(mobile: String?, name: String) {
        try {
            val whatsappIntent = Intent(Intent.ACTION_SEND)
            whatsappIntent.type = "text/plain"
            whatsappIntent.setPackage("com.whatsapp")

            if (mobile.isNotNullOrBlank())
                whatsappIntent.putExtra("jid", "91$mobile@s.whatsapp.net")

            whatsappIntent.putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.supplier_hey_whatsapp, name)
            )
            requireActivity().startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            shortToast(R.string.add_destination_whats_app_not_installed)
        }
    }

    private fun redirectToChatScreen() {
        getCurrentState().supplier?.let {
            if (it.mobile.isNullOrEmpty()) {
                shortToast(R.string.add_number_of_user_for_chat)
            } else {
                activity?.let {
                    val state = getCurrentState()
                    startActivity(
                        chatNavigator.get().getChatIntent(
                            requireActivity(),
                            state.supplier!!.id,
                            "BUYER",
                            getCurrentState().unreadMessageCount,
                            getCurrentState().firstUnseenMessageId
                        )
                    )
                }
            }
        }
    }

    private fun gotoSupplierPaymentScreen(supplierId: String) {
        legacyNavigator.get().goToSupplierPaymentScreen(requireContext(), supplierId)
        dismissAllowingStateLoss()
    }

    override fun loadIntent(): UserIntent? {
        return SupplierProfileContract.Intent.LoadFirst
    }
}
