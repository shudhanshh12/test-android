package `in`.okcredit.merchant.customer_ui.ui.add_discount._di

import `in`.okcredit.merchant.customer_ui.ui.add_discount.AddDiscountContract
import `in`.okcredit.merchant.customer_ui.ui.add_discount.AddDiscountFragment
import `in`.okcredit.merchant.customer_ui.ui.add_discount.AddDiscountViewModel
import `in`.okcredit.shared.base.MviViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class AddDiscountFragmentModule {

    companion object {

        @Provides
        fun initialState(): AddDiscountContract.State = AddDiscountContract.State()

        @Provides
        @ViewModelParam("customer_id")
        fun customerId(fragment: AddDiscountFragment, activity: AppCompatActivity): String? {
            return fragment.arguments?.getString("customer_id")
                ?: activity.intent.getStringExtra("customer_id")
        }

        @Provides
        @ViewModelParam("transaction_type")
        fun transactionType(fragment: AddDiscountFragment, activity: AppCompatActivity): Int {
            return fragment.arguments?.getInt("transaction_type")
                ?.takeUnless { it == 0 }
                ?: activity.intent.getIntExtra(
                    "transaction_type",
                    merchant.okcredit.accounting.model.Transaction.CREDIT
                )
        }

        @Provides
        @ViewModelParam("transaction_amount")
        fun transactionAmount(activity: AppCompatActivity): Long {
            return activity.intent.getLongExtra(AddDiscountContract.ARG_TX_AMOUNT, 0)
        }

        @Provides
        @ViewModelParam("voice_amount")
        fun voiceTransactionAmount(fragment: AddDiscountFragment): Long {
            return fragment.arguments?.getLong("amount") ?: 0
        }

        @Provides
        @ViewModelParam("voice_id")
        fun voiceId(fragment: AddDiscountFragment): String {
            return fragment.arguments?.getString("id") ?: ""
        }

        @Provides
        @ViewModelParam("input_type")
        fun inputType(fragment: AddDiscountFragment): String {
            return fragment.arguments?.getString("method") ?: ""
        }

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system
        // (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: AddDiscountFragment,
            viewModelProvider: Provider<AddDiscountViewModel>
        ): MviViewModel<AddDiscountContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
