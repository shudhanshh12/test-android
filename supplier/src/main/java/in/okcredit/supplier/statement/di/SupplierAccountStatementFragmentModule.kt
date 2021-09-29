package `in`.okcredit.supplier.statement.di

import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.supplier.statement.SupplierAccountStatementContract
import `in`.okcredit.supplier.statement.SupplierAccountStatementFragment
import `in`.okcredit.supplier.statement.SupplierAccountStatementViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class SupplierAccountStatementFragmentModule {

    companion object {

        @Provides
        fun initialState(): SupplierAccountStatementContract.State = SupplierAccountStatementContract.State()

        @Provides
        @ViewModelParam(SupplierAccountStatementViewModel.ARG_SOURCE)
        fun source(activity: AppCompatActivity): String {
            return activity.intent.getStringExtra(SupplierAccountStatementViewModel.ARG_SOURCE) ?: ""
        }

        @Provides
        @ViewModelParam(SupplierAccountStatementViewModel.ARG_DURATION)
        fun duration(activity: AppCompatActivity): String {
            return activity.intent.getStringExtra(SupplierAccountStatementViewModel.ARG_DURATION) ?: ""
        }

        @Provides
        fun viewModel(
            fragment: SupplierAccountStatementFragment,
            viewModelProvider: Provider<SupplierAccountStatementViewModel>
        ): MviViewModel<SupplierAccountStatementContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
