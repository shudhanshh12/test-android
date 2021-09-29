package `in`.okcredit.supplier.payment_process.di

import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.supplier.payment_process.SupplierPaymentDialogContract
import `in`.okcredit.supplier.payment_process.SupplierPaymentDialogScreen
import `in`.okcredit.supplier.payment_process.SupplierPaymentDialogScreen.Companion.ARG_ACCOUNT_BALANCE
import `in`.okcredit.supplier.payment_process.SupplierPaymentDialogScreen.Companion.ARG_ACCOUNT_TYPE
import `in`.okcredit.supplier.payment_process.SupplierPaymentDialogScreen.Companion.ARG_SUPPLIER_ACCOUNT_ID
import `in`.okcredit.supplier.payment_process.SupplierPaymentDialogScreen.Companion.ARG_SUPPLIER_DESTINATION_TYPE
import `in`.okcredit.supplier.payment_process.SupplierPaymentDialogScreen.Companion.ARG_SUPPLIER_MESSAGE_LINK
import `in`.okcredit.supplier.payment_process.SupplierPaymentDialogScreen.Companion.ARG_SUPPLIER_MOBILE
import `in`.okcredit.supplier.payment_process.SupplierPaymentDialogScreen.Companion.ARG_SUPPLIER_NAME
import `in`.okcredit.supplier.payment_process.SupplierPaymentDialogScreen.Companion.ARG_SUPPLIER_PAYMENT_ADDRESS
import `in`.okcredit.supplier.payment_process.SupplierPaymentDialogViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class SupplierPaymentDialogModule {

    companion object {

        @Provides
        fun initialState(fragment: SupplierPaymentDialogScreen): SupplierPaymentDialogContract.State {
            val accountBalance = fragment.requireArguments().getLong(ARG_ACCOUNT_BALANCE, 0L)
            val messageLink = fragment.requireArguments().getString(ARG_SUPPLIER_MESSAGE_LINK, "")
            val mobile = fragment.requireArguments().getString(ARG_SUPPLIER_MOBILE, "")
            val paymentAddress = fragment.requireArguments().getString(ARG_SUPPLIER_PAYMENT_ADDRESS, "")
            val destinationType = fragment.requireArguments().getString(ARG_SUPPLIER_DESTINATION_TYPE, "")
            val name = fragment.requireArguments().getString(ARG_SUPPLIER_NAME, "")
            val accountId = fragment.requireArguments().getString(ARG_SUPPLIER_ACCOUNT_ID, "")
            val accountType = fragment.requireArguments().getString(ARG_ACCOUNT_TYPE, "")
            return SupplierPaymentDialogContract.State().copy(
                accountId = accountId,
                messageLink = messageLink,
                mobile = mobile,
                balance = accountBalance,
                paymentAddress = paymentAddress,
                destinationType = destinationType,
                name = name,
                accountType = accountType
            )
        }

        @Provides
        fun viewModel(
            fragment: SupplierPaymentDialogScreen,
            viewModelProvider: Provider<SupplierPaymentDialogViewModel>
        ): MviViewModel<SupplierPaymentDialogContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
