package `in`.okcredit.sales_ui.ui.bill_summary._di

import `in`.okcredit.sales_ui.ui.bill_summary.BillSummaryContract
import `in`.okcredit.sales_ui.ui.bill_summary.BillSummaryFragment
import `in`.okcredit.sales_ui.ui.bill_summary.BillSummaryViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class BillSummaryModule {

    companion object {

        @Provides
        fun initialState(): BillSummaryContract.State = BillSummaryContract.State()

        @Provides
        @ViewModelParam("sale_id")
        fun saleId(fragment: BillSummaryFragment): String {
            return fragment.arguments?.getString("sale_id") ?: ""
        }

        @Provides
        @ViewModelParam("editable")
        fun isEditable(fragment: BillSummaryFragment): Boolean {
            return fragment.requireArguments().getBoolean("editable", false)
        }

        @Provides
        fun viewModel(
            fragment: BillSummaryFragment,
            viewModelProvider: Provider<BillSummaryViewModel>
        ): MviViewModel<BillSummaryContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
