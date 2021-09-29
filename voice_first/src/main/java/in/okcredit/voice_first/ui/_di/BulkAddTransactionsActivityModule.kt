package `in`.okcredit.voice_first.ui._di

import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.voice_first._di.BulkAddTransactions
import `in`.okcredit.voice_first.ui.bulk_add.BulkAddTransactionsActivity
import `in`.okcredit.voice_first.ui.bulk_add.BulkAddTransactionsContract
import `in`.okcredit.voice_first.ui.bulk_add.BulkAddTransactionsViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class BulkAddTransactionsActivityModule {

    @Binds
    @BulkAddTransactions
    abstract fun activity(activity: BulkAddTransactionsActivity): AppCompatActivity

    companion object {
        @Provides
        fun initialState(): BulkAddTransactionsContract.State {
            return BulkAddTransactionsContract.State()
        }

        @Provides
        fun viewModel(
            activity: BulkAddTransactionsActivity,
            viewModelProvider: Provider<BulkAddTransactionsViewModel>,
        ): MviViewModel<BulkAddTransactionsContract.State> = activity.createViewModel(viewModelProvider)
    }
}
