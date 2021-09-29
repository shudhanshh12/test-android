package `in`.okcredit.merchant.customer_ui.ui.due_customer._di

import `in`.okcredit.merchant.customer_ui.ui.due_customer.DueCustomerContract
import `in`.okcredit.merchant.customer_ui.ui.due_customer.DueCustomerFragment
import `in`.okcredit.merchant.customer_ui.ui.due_customer.DueCustomerViewModel
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
abstract class DueCustomerModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: DueCustomerFragment): DueCustomerContract.Navigator

    companion object {

        @Provides
        fun initialState(): DueCustomerContract.State = DueCustomerContract.State()

        @Provides
        @ViewModelParam(DueCustomerContract.ARG_SOURCE)
        fun sourceScreen(activity: AppCompatActivity): String? {
            return activity.intent.getStringExtra(DueCustomerContract.ARG_SOURCE)
        }

        @Provides
        @ViewModelParam(DueCustomerContract.ARG_REWARDS_AMOUNT)
        fun rewardsAmount(activity: AppCompatActivity): Long {
            return activity.intent.getLongExtra(DueCustomerContract.ARG_REWARDS_AMOUNT, 0L)
        }

        @Provides
        @ViewModelParam(DueCustomerContract.ARG_REDIRECT_TO_REWARDS_PAGE)
        fun redirectToRewardsPage(activity: AppCompatActivity): Boolean {
            return activity.intent.getBooleanExtra(DueCustomerContract.ARG_REDIRECT_TO_REWARDS_PAGE, false)
        }

        @Provides
        fun viewModel(
            fragment: DueCustomerFragment,
            viewModelProvider: Provider<DueCustomerViewModel>
        ): MviViewModel<DueCustomerContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
