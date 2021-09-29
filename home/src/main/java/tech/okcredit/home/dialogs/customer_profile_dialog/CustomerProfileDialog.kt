package tech.okcredit.home.dialogs.customer_profile_dialog

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.SmsHelper
import `in`.okcredit.backend.utils.setQrCode
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.collection.contract.QrCodeBuilder
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.*
import `in`.okcredit.shared.base.BaseBottomDialogScreen
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.view.KycStatusView
import `in`.okcredit.web.WebExperiment
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.convertToPx
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.exceptions.ExceptionUtils
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.home.R
import tech.okcredit.home.databinding.HomeCustomerProfileDialogBinding
import tech.okcredit.home.dialogs.customer_profile_dialog.helpers.setAmountInfo
import tech.okcredit.home.dialogs.customer_profile_dialog.helpers.setLastPaymentInfo
import tech.okcredit.home.utils.TextDrawableUtils
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CustomerProfileDialog :
    BaseBottomDialogScreen<CustomerProfileDialogContract.State>("CustomerProfileDialog"),
    CustomerProfileDialogContract.Navigator {

    private val binding: HomeCustomerProfileDialogBinding by viewLifecycleScoped(HomeCustomerProfileDialogBinding::bind)

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var tracker: Lazy<Tracker>

    @Inject
    lateinit var communicationRepository: Lazy<CommunicationRepository>

    @Inject
    lateinit var smsHelper: Lazy<SmsHelper>

    private var sendReminderPublishSubject = PublishSubject.create<Pair<String, String?>>()

    private lateinit var screen: String

    companion object {

        const val CUSTOMER_ID = "Customer_Id"
        const val SCREEN = "screen"
        const val TAG = "CustomerProfileDialog"

        fun showDialog(
            fragmentManager: FragmentManager,
            customerId: String,
            screen: String? = CustomerProfileDialogContract.Source.HOME_PAGE,
        ) {
            val fragment = CustomerProfileDialog()
            fragment.arguments = Bundle().also {
                it.putString(CUSTOMER_ID, customerId)
                it.putString(SCREEN, screen)
            }
            fragment.show(fragmentManager, TAG)
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        screen = arguments?.getString(SCREEN) ?: CustomerProfileDialogContract.Source.HOME_PAGE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return HomeCustomerProfileDialogBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog?
            val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<View>(bottomSheet!!)
            behavior.peekHeight = 0
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        dismiss()
                    }
                }
            })
        }
        setClickListeners()
        tracker.get().trackViewQr(screen)
    }

    private fun setClickListeners() {
        binding.kycStatusView.setListener(object : KycStatusView.Listener {
            override fun onBannerDisplayed(bannerType: String) {
                tracker.get()
                    .trackKycBannerShown(
                        type = getCurrentState().kycRiskCategory.value,
                        screen = screen,
                        bannerType = bannerType
                    )
            }

            override fun onStartKyc(eventName: String) {
                gotoKycScreen()
                trackKycEvents(eventName)
            }

            override fun onClose(eventName: String) {
                trackKycEvents(eventName)
            }
        })
    }

    internal fun trackKycEvents(eventName: String) {
        val state = getCurrentState()
        tracker.get()
            .trackEvents(
                eventName = eventName,
                screen = screen,
                propertiesMap = PropertiesMap.create()
                    .add("merchant_id", state.business?.id ?: "")
                    .add("account_id", state.customer?.id ?: "")
                    .add("kyc_status", state.kycStatus.value.toLowerCase())
                    .add("risk_type", state.kycRiskCategory.value.toLowerCase())
            )
    }

    internal fun gotoKycScreen() {
        tracker.get()
            .trackStartKycClicked(
                type = getCurrentState().kycRiskCategory.value,
                screen = screen
            )
        legacyNavigator.get().goWebExperimentScreen(requireContext(), WebExperiment.Experiment.KYC.type)
        dismiss()
    }

    override fun loadIntent(): UserIntent {
        return CustomerProfileDialogContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            sendReminderPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    CustomerProfileDialogContract.Intent.SendWhatsAppReminder(it.first, it.second)
                }
        )
    }

    override fun render(state: CustomerProfileDialogContract.State) {
        if (state.customer == null) {
            return
        }

        binding.name.text = state.customer.description
        binding.lastPaymentDate.setLastPaymentInfo(state.customer)
        binding.balanceInfo.setAmountInfo(state.customer)

        val defaultPic = TextDrawableUtils.getRoundTextDrawable(state.customer.description ?: "A")

        Glide.with(this)
            .load(state.customer.profileImage)
            .circleCrop()
            .placeholder(defaultPic)
            .placeholder(defaultPic)
            .into(binding.profileImage)

        when (state.customer.customerSyncStatus) {
            DIRTY.code -> {
                binding.contactPanel.gone()
                with(binding.accountSyncStatusWarning) {
                    text = getString(R.string.offline_cus_customer_profile_drawer_dirty_desc)
                    visible()
                }
                return
            }
            IMMUTABLE.code -> {
                binding.contactPanel.gone()
                with(binding.accountSyncStatusWarning) {
                    val targetDescription = state.cleanCompanionDescription?.let {
                        getString(R.string.offline_cus_customer_profile_drawer_immutable_to, it)
                    } ?: ""
                    val immutableDescription = getString(
                        R.string.offline_cus_customer_profile_drawer_immutable_desc,
                        state.customer.mobile,
                        targetDescription
                    )
                    text = immutableDescription
                    visible()
                }
                return
            }
            CLEAN.code -> {
                binding.contactPanel.visible()
                binding.accountSyncStatusWarning.gone()
                // continue
            }
        }

        if (state.customer.mobile.isNullOrEmpty()) {
            binding.callIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_icon_add_phone
                )
            )

            binding.callText.text = requireContext().getString(R.string.custpr_add_mobile)

            binding.smsIcon.gone()
            binding.smsText.gone()

            binding.whatsappIcon.gone()
            binding.whatsappText.gone()

            binding.callIcon.setAddMobileClickListener(state.customer)
            binding.callText.setAddMobileClickListener(state.customer)
        } else {
            binding.callIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_call
                )
            )
            binding.callText.text = requireContext().getString(R.string.call)

            if (state.customer.balanceV2 < 0) {
                binding.whatsappIcon.visible()
                binding.whatsappText.visible()

                binding.smsText.visible()
                binding.smsIcon.visible()
            } else {
                binding.whatsappIcon.gone()
                binding.whatsappText.gone()

                binding.smsText.gone()
                binding.smsIcon.gone()
            }

            if (state.collectionCustomerProfile == null) {
                binding.smsIcon.setSmsClickListener(state.customer, state.business)
                binding.smsText.setSmsClickListener(state.customer, state.business)
            } else {
                binding.smsIcon.setPaymentReminder(
                    state.customer,
                    GetPaymentReminderIntent.Companion.ReminderMode.SMS.value
                )
                binding.smsText.setPaymentReminder(
                    state.customer,
                    GetPaymentReminderIntent.Companion.ReminderMode.SMS.value
                )
            }

            binding.whatsappIcon.setPaymentReminder(
                state.customer
            )
            binding.whatsappText.setPaymentReminder(
                state.customer
            )

            binding.callIcon.setCallClickListener(state.customer)
            binding.callText.setCallClickListener(state.customer)
        }

        if (state.collectionCustomerProfile != null) {
            if (state.isKycLimitReached.not()) {
                binding.qrImage.visible()
                binding.tvQrDescription.visible()
                binding.ivBhimUpi.visible()
            } else {
                binding.qrImage.gone()
                binding.tvQrDescription.gone()
                binding.ivBhimUpi.gone()
            }
            if (state.kycStatus == KycStatus.FAILED || state.kycStatus == KycStatus.PENDING || state.isKycLimitReached) {
                binding.kycStatusView.setData(
                    state.kycStatus.value,
                    state.kycRiskCategory.value,
                    state.isKycLimitReached,
                    true
                )
                binding.kycStatusView.visible()
            } else {
                binding.kycStatusView.gone()
            }

            if (binding.profileImage.layoutParams?.height != 56.convertToPx()) {
                binding.profileImage.layoutParams?.height = 56.convertToPx()
                binding.profileImage.layoutParams?.width = 56.convertToPx()
                binding.profileImage.requestLayout()
            }

            val deviceWidth = requireContext().resources.displayMetrics.widthPixels
            val layoutParams = binding.qrImage.layoutParams as ConstraintLayout.LayoutParams
            val qrImgMargins = layoutParams.marginStart + layoutParams.marginEnd

            val qrIntent = QrCodeBuilder.getQrCode(
                qrIntent = state.collectionCustomerProfile.qr_intent,
                currentBalance = state.customer.balanceV2,
                lastPayment = state.customer.lastPayment,
            )
            binding.qrImage.setQrCode(qrIntent, requireContext(), (deviceWidth - qrImgMargins))

            if (state.customer.isLiveSales) {
                binding.profileImage.gone()
                binding.balanceInfo.gone()

                if (state.customer.lastPayment != null) {
                    binding.lastPaymentDate.setLastPaymentInfo(state.customer)
                } else {
                    binding.lastPaymentDate.gone()
                }

                binding.smsIcon.gone()
                binding.smsText.gone()
                binding.callIcon.gone()
                binding.callText.gone()

                binding.whatsappText.visible()
                binding.whatsappIcon.visible()

                binding.whatsappIcon.setPaymentReminder(
                    state.customer
                )
                binding.whatsappText.setPaymentReminder(
                    state.customer
                )

                binding.whatsappText.text = requireContext().getString(R.string.share_payment_link)
                binding.whatsappIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_icon_link_pay
                    )
                )

                binding.name.text = requireContext().getString(R.string.link_pay)
                binding.tvQrDescription.text = requireContext().getString(R.string.ask_customer_to_pay_online)
            } else {
                binding.tvQrDescription.text =
                    requireContext().getString(R.string.ask_customer_to_scan_qr_and_pay_online)
            }
        } else {
            binding.qrImage.gone()
            binding.tvQrDescription.gone()
            binding.ivBhimUpi.gone()

            if (binding.profileImage.layoutParams?.height != 96.convertToPx()) {
                binding.profileImage.layoutParams?.height = 96.convertToPx()
                binding.profileImage.layoutParams?.width = 96.convertToPx()
                binding.profileImage.requestLayout()
            }
        }
    }

    override fun gotoLogin() {
        requireActivity().runOnUiThread {
            legacyNavigator.get().goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    override fun goBack() {
        requireActivity().runOnUiThread {
            longToast(R.string.err_default)
            dismiss()
        }
    }

    override fun showWhatsAppNotInstalled() {
        requireActivity().runOnUiThread {
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.whatsapp_not_installed),
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    override fun shareReminder(intent: Intent) {
        dismiss()
        requireActivity().startActivity(intent)
    }

    private fun View?.setSmsClickListener(customer: Customer, business: Business?) {
        this?.setOnClickListener {
            tracker.get().trackSendReminder(
                PropertyValue.SMS,
                customer.id,
                "",
                screen,
                PropertyValue.CUSTOMER,
                customer.mobile,
                ""
            )
            val whatsappIntentBuilder = ShareIntentBuilder(
                shareText = business?.let {
                    smsHelper.get().getReminderSmsText(
                        customer,
                        it
                    )
                },
                phoneNumber = customer.mobile
            )
            communicationRepository.get().goToSms(whatsappIntentBuilder)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { intent -> startActivity(intent) },
                    { error -> ExceptionUtils.logException(Exception(error)) }
                )
            dismiss()
        }
    }

    private fun View?.setAddMobileClickListener(customer: Customer) {
        this?.setOnClickListener {
            tracker.get().trackSelectProfileV1(
                relation = PropertyValue.CUSTOMER,
                type = screen,
                field = PropertyValue.MOBILE
            )
            legacyNavigator.get().gotoCustomerProfile(activity as AppCompatActivity, customer.id, true)
            dismiss()
        }
    }

    private fun View?.setCallClickListener(
        customer: Customer,
    ) {
        this?.setOnClickListener {
            tracker.get().trackCallRelationShip(
                screen,
                PropertyValue.CUSTOMER,
                customer.id
            )
            Permission.requestCallPermission(
                activity as AppCompatActivity,
                object : IPermissionListener {
                    override fun onPermissionGrantedFirstTime() {
                        tracker.get().trackRuntimePermission(screen, Event.CALL, true)
                    }

                    override fun onPermissionGranted() {
                        val intent = Intent(Intent.ACTION_CALL)
                        intent.data = Uri.parse(getString(R.string.call_template, customer.mobile))
                        startActivity(intent)
                        dismissAllowingStateLoss()
                    }

                    override fun onPermissionDenied() {
                        tracker.get().trackRuntimePermission(screen, Event.CALL, false)
                    }
                }
            )
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (activity is CustomerProfileTransparentActivity) {
            requireActivity().finish()
        }
        super.onDismiss(dialog)
    }

    private fun View?.setPaymentReminder(
        customer: Customer,
        reminderMode: String? = "whatsapp",
    ) {
        this?.setOnClickListener {
            if (customer.isLiveSales) {
                tracker.get().trackEvents(Event.CLICKED_ON_SHARE_LINK_PAY, screen = screen)
            }

            sendReminderPublishSubject.onNext(customer.id to reminderMode)
        }
    }
}
