package merchant.okcredit.supplier.contract

import android.content.Context
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager

interface SupplierNavigator {

    fun goToSupplierAccountStatement(context: Context)

    fun showPaymentLimitWarningBottomSheet(
        fragmentManager: FragmentManager,
    )

    fun showSupplierPaymentDialogScreen(
        fragmentManager: FragmentManager,
        accountId: String = "",
        mobile: String = "",
        balance: Long = 0L,
        destinationType: String = "",
        messageLink: String? = "",
        paymentAddress: String = "",
        name: String = "",
        accountType: String = "",
        listener: SupplierPaymentListener? = null,
    ): AppCompatDialogFragment
}
