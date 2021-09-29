package `in`.okcredit.collection_ui.ui.passbook.payments._di

import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsContract
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsFragment
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class OnlinePaymentsModule {

    companion object {

        @Provides
        fun initialState(fragment: OnlinePaymentsFragment): OnlinePaymentsContract.State {
            val source = fragment.arguments?.getString(OnlinePaymentsFragment.ARG_SOURCE)
                ?: OnlinePaymentsFragment.SOURCE_MERCHANT_QR
            return OnlinePaymentsContract.State(source = source)
        }

        @Provides
        fun viewModel(
            fragment: OnlinePaymentsFragment,
            viewModelProvider: Provider<OnlinePaymentsViewModel>,
        ): MviViewModel<OnlinePaymentsContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
