package `in`.okcredit.merchant.customer_ui.ui.dialogs

import `in`.okcredit.backend._offline.model.DueInfo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.DialogPaymentPendingBinding
import `in`.okcredit.merchant.customer_ui.usecase.GetCollectionNudgeOnDueDateCrossed
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.setBooleanVisibility
import tech.okcredit.android.base.utils.DateTimeUtils

class PaymentPendingDialog : ExpandedBottomSheetDialogFragment() {

    lateinit var binding: DialogPaymentPendingBinding

    companion object {
        const val TAG = "PaymentPendingDialog"

        private const val SCREEN = "PaymentPendingDialog"
        private const val TYPE = "post_due_date"
    }

    interface Listener {
        fun onUpdate()
        fun onRemind()
        fun onSetUpNow()
    }

    interface EventListener {
        fun displayed(variant: String, type: String = TYPE, screen: String = SCREEN)
        fun cleared(variant: String, type: String = TYPE, screen: String = SCREEN)
        fun clicked(variant: String, focal: Boolean, type: String = TYPE, screen: String = SCREEN)
        fun trackUpdate(variant: String, type: String = TYPE, screen: String = SCREEN)
        fun trackRemind(variant: String, type: String = TYPE, screen: String = SCREEN)
        fun trackSetup(variant: String, type: String = TYPE, screen: String = SCREEN)
    }

    private var event: EventListener? = null
    private var listener: Listener? = null
    private var customer: Customer? = null
    private var dueInfo: DueInfo? = null
    private var showVariant: GetCollectionNudgeOnDueDateCrossed.Show = GetCollectionNudgeOnDueDateCrossed.Show.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedCornerBottomSheet)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogPaymentPendingBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun initListener(listener: Listener) {
        this.listener = listener
    }

    fun setEventListener(eventListener: EventListener) {
        event = eventListener
    }

    fun setData(customer: Customer?, dueInfo: DueInfo?, showVariant: GetCollectionNudgeOnDueDateCrossed.Show) {
        this.customer = customer
        this.dueInfo = dueInfo
        this.showVariant = showVariant
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleOutsideClick()
        setListeners()
        render()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            val dialog = dialog as BottomSheetDialog?
            val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<View>(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    private fun setListeners() {
        binding.action1.setOnClickListener {
            if (showVariant == GetCollectionNudgeOnDueDateCrossed.Show.SETUP_COLLECTION) {
                listener?.onRemind()
                event?.trackRemind(showVariant.name)
            } else if (showVariant == GetCollectionNudgeOnDueDateCrossed.Show.UPDATE) {
                listener?.onUpdate()
                event?.trackUpdate(showVariant.name)
            }
            dismiss()
        }
        binding.action2.setOnClickListener {
            if (showVariant == GetCollectionNudgeOnDueDateCrossed.Show.SETUP_COLLECTION) {
                listener?.onSetUpNow()
                event?.trackSetup(showVariant.name)
            } else if (showVariant == GetCollectionNudgeOnDueDateCrossed.Show.UPDATE) {
                listener?.onRemind()
                event?.trackRemind(showVariant.name)
            }
            dismiss()
        }
        binding.root.setOnClickListener {
            event?.clicked(showVariant.name, true)
        }
    }

    private fun setProfile(name: String?, profilepic: String?) {
        binding.name.text = name
        name?.let {
            var text = if (it.length > 1) it.substring(0, 1) else it
            if (text.isEmpty() || text.toCharArray()[0].isDigit()) {
                text = "+"
            } else {
                text = text.toUpperCase()
            }
            val defaultPic = TextDrawable
                .builder()
                .buildRound(
                    text,
                    ColorGenerator.MATERIAL.getColor(it)
                )
            if (profilepic != null) {
                GlideApp
                    .with(requireContext())
                    .load(profilepic)
                    .placeholder(defaultPic)
                    .error(defaultPic)
                    .fallback(defaultPic)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(binding.profileImage)
            } else {
                binding.profileImage.setImageDrawable(defaultPic)
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        event?.cleared(showVariant.name)
        super.onCancel(dialog)
    }

    private fun handleOutsideClick() {
        val outsideView =
            dialog?.window?.decorView?.findViewById<View>(com.google.android.material.R.id.touch_outside)
        outsideView?.setOnClickListener {
            event?.clicked(showVariant.name, false)
        }
    }

    private fun setDescription(amount: Double = 0.0, date: DateTime?) {
        val dateStr = if (date != null) DateTimeUtils.formatDateOnly(date) else "NA"

        val amountStr = CurrencyUtil.currencyDisplayFormat(amount * -1)

        val stringResource = if (showVariant == GetCollectionNudgeOnDueDateCrossed.Show.SETUP_COLLECTION) {
            R.string.payment_pending_description_collection
        } else {
            R.string.payment_pending_description_collection
        }
        val content = context?.getString(
            stringResource,
            amountStr,
            dateStr
        )
        val spn = SpannableString(content)
        val amountStart = 11
        val amountEnd = amountStart + 1 + amountStr.length

        val dateStart = amountEnd + 15
        val dateEnd = dateStart + dateStr.length
        spn.setSpan(
            ForegroundColorSpan(requireContext().resources.getColor(R.color.red_primary)),
            amountStart,
            amountEnd,
            SpannableString.SPAN_INCLUSIVE_INCLUSIVE
        )
        spn.setSpan(StyleSpan(BOLD), amountStart, amountEnd, SpannableString.SPAN_INCLUSIVE_INCLUSIVE)
        spn.setSpan(
            ForegroundColorSpan(requireContext().resources.getColor(R.color.red_primary)),
            dateStart,
            dateEnd,
            SpannableString.SPAN_INCLUSIVE_INCLUSIVE
        )
        spn.setSpan(StyleSpan(BOLD), dateStart, dateEnd, SpannableString.SPAN_INCLUSIVE_INCLUSIVE)
        binding.description.text = spn
    }

    private fun setMobileNumber(mobile: String?) {
        binding.number.text = mobile
        binding.number.setBooleanVisibility(mobile.isNullOrEmpty().not())
    }

    private fun displaySetupCollectionVariant() {
        binding.action1.setText(requireContext().getString(R.string.skip_and_remind))
        binding.action2.setText(requireContext().getString(R.string.setup_now))
    }

    private fun displayUpdateVariant() {
        binding.action1.setText(requireContext().getString(R.string.update))
        binding.action2.setText(requireContext().getString(R.string.remind))
    }

    private fun displayButtons() {
        when (showVariant) {
            GetCollectionNudgeOnDueDateCrossed.Show.SETUP_COLLECTION -> displaySetupCollectionVariant()
            GetCollectionNudgeOnDueDateCrossed.Show.UPDATE -> displayUpdateVariant()
            else -> dismiss()
        }
    }

    private fun render() {
        setProfile(customer?.description, customer?.profileImage)
        setMobileNumber(customer?.mobile)
        val amount = customer?.balanceV2 ?: 0L
        setDescription(amount / 100.0, dueInfo?.activeDate)
        displayButtons()
        event?.displayed(showVariant.name)
    }
}
