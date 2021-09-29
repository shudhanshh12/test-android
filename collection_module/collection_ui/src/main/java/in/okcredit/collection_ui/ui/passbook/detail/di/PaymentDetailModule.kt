package `in`.okcredit.collection_ui.ui.passbook.detail.di

import `in`.okcredit.collection_ui.ui.passbook.detail.PaymentDetailContract
import `in`.okcredit.collection_ui.ui.passbook.detail.PaymentDetailFragment
import `in`.okcredit.collection_ui.ui.passbook.detail.PaymentDetailViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class PaymentDetailModule {
    companion object {

        @Provides
        fun initialState(fragment: PaymentDetailFragment): PaymentDetailContract.State =
            PaymentDetailContract.State(
                source = fragment.arguments?.getString(PaymentDetailFragment.SOURCE) ?: "",
                txnId = fragment.arguments?.getString(PaymentDetailFragment.PAYMENT_ID) ?: ""
            )

        @Provides
        fun viewModel(
            fragment: PaymentDetailFragment,
            viewModelProvider: Provider<PaymentDetailViewModel>,
        ): MviViewModel<PaymentDetailContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
