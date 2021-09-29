package `in`.okcredit.merchant.customer_ui.ui.customer

import `in`.okcredit.analytics.*
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import `in`.okcredit.shared.utils.CommonUtils.colorStateListOf
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import kotlinx.android.parcel.Parcelize
import tech.okcredit.android.base.extensions.getColorFromAttr

class ReminderBottomSheetDialog : ExpandedBottomSheetDialogFragment() {

    private var reminderModeListener: ReminderMode? = null

    private lateinit var reminderTotal: TextView
    private lateinit var reminderDue: TextView
    private lateinit var smsTick: ImageView
    private lateinit var whatsappTick: ImageView
    private lateinit var sendReminderButton: CardView
    private lateinit var selectDefault: CheckBox

    private lateinit var reminderSheet: ReminderSheet

    fun initialise(reminderModeListener: ReminderMode) {
        this.reminderModeListener = reminderModeListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.reminder_layout_bottom_sheet, container, false)
        reminderTotal = view.findViewById(R.id.reminder_total)
        reminderDue = view.findViewById(R.id.reminder_due)
        smsTick = view.findViewById(R.id.sms_tick)
        whatsappTick = view.findViewById(R.id.whatsapp_tick)
        sendReminderButton = view.findViewById(R.id.send_reminder_button)
        selectDefault = view.findViewById(R.id.select_default)

        val smsContainer = view.findViewById<LinearLayout>(R.id.sms_container)
        val whatsappContainer = view.findViewById<LinearLayout>(R.id.whatsapp_container)

        changeCheckboxColorStateWise(selectDefault)

        smsContainer.setOnClickListener {
            reminderSheet.reminderMode = SMS
            render(reminderSheet)

            sendReminderSettingEvent("SMS", "Customer Screen")
        }

        whatsappContainer.setOnClickListener {
            reminderSheet.reminderMode = WHATSAPP
            render(reminderSheet)
            sendReminderSettingEvent("Whatsapp", "Customer Screen")
        }

        sendReminderButton.setOnClickListener {
            if (selectDefault.isChecked) {
                reminderModeListener?.setReminderMode(reminderSheet.reminderMode)
            }

            var isReminderModeEnabled = true
            when {
                reminderSheet.mobileNumber.isEmpty() -> {
                    Toast.makeText(
                        context,
                        getString(R.string.add_customer_number_to_send),
                        Toast.LENGTH_LONG
                    ).show()
                    isReminderModeEnabled = false
                }
                reminderSheet.totalAmount >= 0 -> {
                    Toast.makeText(
                        context,
                        getString(R.string.balance_in_advance_no_reminder),
                        Toast.LENGTH_LONG
                    ).show()
                    isReminderModeEnabled = false
                }
                else -> {
                    sendReminder(reminderSheet.reminderMode)
                }
            }

            tracker.trackEvents(
                Event.SEND_REMINDER,
                screen = PropertyValue.REMINDER_RELATION_PAGE,
                relation = PropertyValue.CUSTOMER,
                propertiesMap = PropertiesMap.create()
                    .add(PropertyKey.ACCOUNT_ID, reminderSheet.accountId)
                    .add(PropertyKey.TYPE, if (reminderSheet.reminderMode.equals(SMS)) "SMS" else "Whatsapp")
                    .add(PropertyKey.FIELD, PropertyValue.REMINDER_SETTING)
                    .add(PropertyKey.VALUE, selectDefault.isChecked)
                    .add(PropertyKey.ENABLED, isReminderModeEnabled)
            )
            dismiss()
        }

        reminderSheet = arguments?.getParcelable(REMINDER_OBJECT) ?: ReminderSheet()
        render(reminderSheet)
        return view
    }

    private fun sendReminderSettingEvent(value: String, screen: String = "") {
        tracker.trackEvents(
            Event.SELECT_REMINDER_SETTING, relation = PropertyValue.CUSTOMER,
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.ACCOUNT_ID, reminderSheet.accountId)
                .add(PropertyKey.SET_VALUE, value)
                .add(PropertyKey.SCREEN, screen)
        )
    }

    private fun changeCheckboxColorStateWise(selectDefault: CheckBox) {
        val colorList = colorStateListOf(
            intArrayOf(android.R.attr.state_checked) to requireContext().getColorFromAttr(R.attr.colorPrimary),
            intArrayOf(-android.R.attr.state_checked) to ContextCompat.getColor(requireContext(), R.color.grey800)
        )
        CompoundButtonCompat.setButtonTintList(selectDefault, colorList)
    }

    private fun sendReminder(reminderMode: String) {
        when (reminderMode) {
            SMS -> {
                reminderModeListener?.sharePaymentReminderOnSms()
            }

            WHATSAPP -> {
                reminderModeListener?.sharePaymentReminderOnWhatsapp()
            }
        }

        tracker.trackEvents(
            Event.UPDATE_PPROFILE,
            screen = PropertyValue.RELATION_PAGE,
            relation = PropertyValue.CUSTOMER,
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.ACCOUNT_ID, reminderSheet.accountId)
                .add(PropertyKey.SET_VALUE, if (reminderMode.equals(SMS)) "SMS" else "Whatsapp")
                .add(PropertyKey.FIELD, PropertyValue.REMINDER_SETTING)
                .add(PropertyKey.VALUE, selectDefault.isChecked)
        )
    }

    fun render(state: ReminderSheet) {

        if (state.reminderMode.equals(SMS)) {
            smsTick.visibility = View.VISIBLE
            whatsappTick.visibility = View.GONE
            selectDefault.text = getString(R.string.always_use_sms)
        } else {
            whatsappTick.visibility = View.VISIBLE
            smsTick.visibility = View.GONE
            selectDefault.text = getString(R.string.always_use_whatsapp)
        }

        val isAmountInAdvance = state.totalAmount >= 0
        if (state.mobileNumber.isEmpty()) {
            sendReminderButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey400))
        } else if (isAmountInAdvance) {
            sendReminderButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey400))
        } else {
            sendReminderButton.setCardBackgroundColor(requireContext().getColorFromAttr(R.attr.colorPrimary))
        }

        renderBalanceV2(state.totalAmount, reminderTotal)

        reminderDue.text = context?.getString(
            if (isAmountDue(state.totalAmount)) R.string.due else R.string.advance
        )
    }

    private fun isAmountDue(totalAmount: Long): Boolean {
        return totalAmount < 0
    }

    private fun renderBalanceV2(amount: Long, textView: TextView) {
        @ColorRes var color = R.color.tx_payment
        if (amount < 0L) {
            color = R.color.red_primary
        }
        textView.text = String.format("₹%s", CurrencyUtil.formatV2(amount))
        textView.setTextColor(ContextCompat.getColor(textView.context, color))
    }

    /**
     * ReminderMode Interface to update reminder mode and it's extend
     * send payment reminder
     */
    interface ReminderMode {
        fun setReminderMode(reminderMode: String)

        fun sharePaymentReminderOnWhatsapp()

        fun sharePaymentReminderOnSms()
    }

    @Parcelize
    data class ReminderSheet(
        val totalAmount: Long = 0L,
        val mobileNumber: String = "",
        val accountId: String = "",
        var reminderMode: String = WHATSAPP,
    ) : Parcelable

    companion object {
        val TAG: String? = ReminderBottomSheetDialog::class.java.simpleName

        const val REMINDER_OBJECT = "reminder_object"
        const val SMS = "sms"
        const val WHATSAPP = "whatsapp"

        lateinit var tracker: Tracker

        fun netInstance(tracker: Tracker): ReminderBottomSheetDialog {
            this.tracker = tracker
            return ReminderBottomSheetDialog()
        }
    }
}
