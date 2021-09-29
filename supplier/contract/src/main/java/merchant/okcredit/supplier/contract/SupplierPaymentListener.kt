package merchant.okcredit.supplier.contract

import androidx.appcompat.app.AppCompatDialogFragment

interface SupplierPaymentListener {
    fun onChangeDetails(dialog: AppCompatDialogFragment)
    fun onConfirm(messageLink: String, dialog: AppCompatDialogFragment)
}
