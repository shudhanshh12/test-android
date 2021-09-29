package tech.okcredit.home.ui.home

import `in`.okcredit.merchant.suppliercredit.model.NotificationReminderForUi
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.TextDrawableUtils
import tech.okcredit.home.R
import tech.okcredit.home.databinding.DialogFragmentNotificationReminderBinding

class NotificationReminderDialog : DialogFragment() {
    private var binding: DialogFragmentNotificationReminderBinding? = null
    private var listener: NotificationReminderListener? = null

    interface NotificationReminderListener {
        fun onPayNowClicked()
        fun onDismissed()
    }

    fun initialise(listener: NotificationReminderListener) {
        this.listener = listener
    }

    companion object {
        const val TAG = "NotificationReminderDialog"
        private const val ARG_SUPPLIER_NAME = "arg_supplier_name"
        private const val ARG_PROFILE_URL = "arg_profile_url"
        private const val ARG_BALANCE_DUE = "arg_balance_due"
        private const val ARG_LAST_AMOUNT_PAID = "arg_last_amount_paid"
        private const val ARG_LAST_AMOUNT_PAID_DATE = "arg_last_amount_paid_date"

        fun newInstance(notificationReminderForUi: NotificationReminderForUi): NotificationReminderDialog {
            val bundle = Bundle().apply {
                putString(ARG_SUPPLIER_NAME, notificationReminderForUi.name)
                putString(ARG_PROFILE_URL, notificationReminderForUi.profileImage)
                putString(ARG_LAST_AMOUNT_PAID_DATE, notificationReminderForUi.lastPaymentDate)
                putString(ARG_BALANCE_DUE, notificationReminderForUi.balance)
                notificationReminderForUi.lastPayment?.let {
                    putString(ARG_LAST_AMOUNT_PAID, it)
                }
            }

            return NotificationReminderDialog().apply {
                arguments = bundle
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)

        dialog.setOnCancelListener {
            listener?.onDismissed()
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DialogFragmentNotificationReminderBinding.inflate(inflater, container, false)
        binding?.apply {
            arguments?.apply {

                val name = getString(ARG_SUPPLIER_NAME)
                textViewSupplierName.text = name

                textViewAmount.text =
                    resources.getString(R.string.t002_networked_reminder_amount, getString(ARG_BALANCE_DUE))

                val lastPayment = getString(ARG_LAST_AMOUNT_PAID)
                if (lastPayment.isNullOrEmpty()) {
                    textViewLastPayment.gone()
                } else {
                    textViewLastPayment.text = resources.getString(
                        R.string.t002_networked_reminder_last_payment,
                        lastPayment, getString(ARG_LAST_AMOUNT_PAID_DATE)
                    )
                    textViewLastPayment.visible()
                }

                val profileUrl = getString(ARG_PROFILE_URL)
                val defaultPic = TextDrawableUtils.getRoundTextDrawable(requireNotNull(name))
                if (profileUrl.isNullOrBlank()) {
                    imageViewProfileImage.setImageDrawable(defaultPic)
                } else {
                    Glide.with(this@NotificationReminderDialog)
                        .load(profileUrl)
                        .placeholder(defaultPic)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .into(imageViewProfileImage)
                }
            }

            imageViewClose.setOnClickListener {
                listener?.onDismissed()
                dismiss()
            }

            materialButtonPayNow.setOnClickListener {
                listener?.onPayNowClicked()
                dismiss()
            }
        }
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
