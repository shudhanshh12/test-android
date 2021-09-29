package `in`.okcredit.merchant.customer_ui.ui.discount_details._di

import `in`.okcredit.merchant.customer_ui.ui.discount_details.DiscountDetailsContract
import `in`.okcredit.merchant.customer_ui.ui.discount_details.DiscountDetailsFragment
import `in`.okcredit.merchant.customer_ui.ui.discount_details.DiscountDetailsViewModel
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
abstract class DiscountDetailsFragmentModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: DiscountDetailsFragment): DiscountDetailsContract.Navigator

    companion object {

        @Provides
        @ViewModelParam("transaction_id")
        fun customerId(activity: AppCompatActivity): String? {
            return activity.intent.getStringExtra(DiscountDetailsContract.ARG_TRANSACTION_ID)
        }

        @Provides
        fun viewModel(
            fragment: DiscountDetailsFragment,
            viewModelProvider: Provider<DiscountDetailsViewModel>
        ): MviViewModel<DiscountDetailsContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
