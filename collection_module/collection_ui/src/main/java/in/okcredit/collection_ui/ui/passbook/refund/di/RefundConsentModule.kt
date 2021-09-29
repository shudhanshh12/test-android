package `in`.okcredit.collection_ui.ui.passbook.refund.di

import `in`.okcredit.collection_ui.ui.passbook.refund.RefundConsentBottomSheet
import `in`.okcredit.collection_ui.ui.passbook.refund.RefundConsentContract
import `in`.okcredit.collection_ui.ui.passbook.refund.RefundConsentViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class RefundConsentModule {

    companion object {

        @Provides
        fun initialState(fragment: RefundConsentBottomSheet): RefundConsentContract.State =
            RefundConsentContract.State(
                payoutId = fragment.arguments?.getString(RefundConsentBottomSheet.ARG_PAYOUT_ID)
                    ?: "",
                txnId = fragment.arguments?.getString(RefundConsentBottomSheet.ARG_TXN_ID)
                    ?: "",
                paymentId = fragment.arguments?.getString(RefundConsentBottomSheet.ARG_PAYMENT_ID)
                    ?: "",
                collectionType = fragment.arguments?.getString(RefundConsentBottomSheet.ARG_COLLECTION_TYPE)
                    ?: ""
            )

        @Provides
        fun viewModel(
            fragment: RefundConsentBottomSheet,
            viewModelProvider: Provider<RefundConsentViewModel>,
        ): MviViewModel<RefundConsentContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
