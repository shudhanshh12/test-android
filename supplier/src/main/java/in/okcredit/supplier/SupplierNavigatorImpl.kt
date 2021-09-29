package `in`.okcredit.supplier

import `in`.okcredit.supplier.payment_process.SupplierPaymentDialogScreen
import `in`.okcredit.supplier.statement.SupplierAccountStatementActivity
import `in`.okcredit.supplier.supplier_limit_warning_bottomsheet.PaymentLimitWarningBottomSheet
import android.content.Context
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import merchant.okcredit.supplier.contract.SupplierNavigator
import merchant.okcredit.supplier.contract.SupplierPaymentListener
import javax.inject.Inject

class SupplierNavigatorImpl @Inject constructor() : SupplierNavigator {

    override fun goToSupplierAccountStatement(context: Context) {
        SupplierAccountStatementActivity.start(context)
    }

    override fun showSupplierPaymentDialogScreen(
        fragmentManager: FragmentManager,
        accountId: String,
        mobile: String,
        balance: Long,
        destinationType: String,
        messageLink: String?,
        paymentAddress: String,
        name: String,
        accountType: String,
        listener: SupplierPaymentListener?,
    ): AppCompatDialogFragment {
        val dialog = SupplierPaymentDialogScreen.newInstance(
            accountId = accountId,
            mobile = mobile,
            balance = balance,
            destinationType = destinationType,
            messageLink = messageLink,
            paymentAddress = paymentAddress,
            name = name,
            accountType = accountType
        )
        listener?.let { dialog.setListener(listener) }
        dialog.show(fragmentManager, "SupplierPaymentDialogScreen")
        return dialog
    }

    override fun showPaymentLimitWarningBottomSheet(
        fragmentManager: FragmentManager,
    ) {
        val bottomSheet = PaymentLimitWarningBottomSheet()
        bottomSheet.show(fragmentManager, "PaymentLimitWarningBottomSheet")
    }
}
