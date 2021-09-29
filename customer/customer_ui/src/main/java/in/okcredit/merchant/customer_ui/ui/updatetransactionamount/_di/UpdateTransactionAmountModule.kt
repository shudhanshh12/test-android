package `in`.okcredit.merchant.customer_ui.ui.updatetransactionamount._di

import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerContract
import `in`.okcredit.merchant.customer_ui.ui.updatetransactionamount.UpdateTransactionAmountContract
import `in`.okcredit.merchant.customer_ui.ui.updatetransactionamount.UpdateTransactionAmountScreen
import `in`.okcredit.merchant.customer_ui.ui.updatetransactionamount.UpdateTransactionAmountViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class UpdateTransactionAmountModule {

    companion object {

        @Provides
        fun initialState(): UpdateTransactionAmountContract.State = UpdateTransactionAmountContract.State()

        @Provides
        @ViewModelParam(CustomerContract.ARG_TXN_ID)
        fun txnId(updateTransactionAmountScreen: UpdateTransactionAmountScreen): String {
            return updateTransactionAmountScreen.arguments?.getString(CustomerContract.ARG_TXN_ID) ?: ""
        }

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system
        // (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: UpdateTransactionAmountScreen,
            viewModelProvider: Provider<UpdateTransactionAmountViewModel>
        ): MviViewModel<UpdateTransactionAmountContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
