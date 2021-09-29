package `in`.okcredit.merchant.customer_ui.ui.dialogs

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.server.internal.common.SupplierCreditServerErrors
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView

class CyclicAccountDialog {
    interface Listener {
        fun onViewClicked()
    }

    companion object {
        fun showCustomerConflict(activity: Activity?, errData: SupplierCreditServerErrors.Error?, listener: Listener?) {
            activity?.let {
                showDialog(activity, errData?.mobile, errData?.name, true, listener)
            }
        }

        fun showSupplierConflict(activity: Activity?, supplier: Supplier?, listener: Listener?) {
            activity?.let {
                showDialog(activity, supplier?.mobile, supplier?.name, false, listener)
            }
        }

        private fun showDialog(
            activity: Activity,
            mobile: String?,
            name: String?,
            isCustomer: Boolean,
            listener: Listener?
        ) {
            val builder = AlertDialog.Builder(activity)
            builder.setCancelable(false)

            val layoutInflater = activity.layoutInflater
            val dialogView = layoutInflater.inflate(R.layout.dialog_mobile_conflict, null)
            builder.setView(dialogView)

            val alertDialog = builder.create()
            if (!activity.isFinishing) {
                alertDialog.show()
            }

            val tvMessage = dialogView.findViewById<TextView>(R.id.title)
            val tvView = dialogView.findViewById<TextView>(R.id.btn_title)
            val viewSupplierButton = dialogView.findViewById<CardView>(R.id.bottom_button)

            if (isCustomer) {
                tvMessage.text = activity.getString(R.string.err_cyclick_conflict_customer, mobile, name)
                tvView.text = activity.getString(R.string.view_customer)
            } else {
                tvMessage.text = activity.getString(R.string.err_cyclick_conflict_supplier, mobile, name)
                tvView.text = activity.getString(R.string.view_supplier)
            }

            viewSupplierButton.setOnClickListener {
                listener.let {
                    alertDialog.dismiss()
                    it?.onViewClicked()
                }
            }

            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            alertDialog.setCancelable(true)

            alertDialog.show()
        }
    }
}
