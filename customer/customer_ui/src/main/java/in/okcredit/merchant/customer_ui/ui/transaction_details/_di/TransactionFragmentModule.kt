package `in`.okcredit.merchant.customer_ui.ui.transaction_details._di

import `in`.okcredit.merchant.customer_ui.ui.transaction_details.TransactionContract
import `in`.okcredit.merchant.customer_ui.ui.transaction_details.TransactionFragment
import `in`.okcredit.merchant.customer_ui.ui.transaction_details.TransactionViewModel
import `in`.okcredit.shared.base.MviViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class TransactionFragmentModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: TransactionFragment): TransactionContract.Navigator

    companion object {

        @Provides
        @ViewModelParam("transaction_id")
        fun customerId(activity: AppCompatActivity): String? {
            return activity.intent.getStringExtra("transaction_id")
        }

        @Provides
        fun viewModel(
            fragment: TransactionFragment,
            viewModelProvider: Provider<TransactionViewModel>
        ): MviViewModel<TransactionContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
