package `in`.okcredit.voice_first.ui._di

import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.voice_first._di.BulkAddTransactions
import `in`.okcredit.voice_first.ui.bulk_add.search_merchant.SearchMerchantActivity
import `in`.okcredit.voice_first.ui.bulk_add.search_merchant.SearchMerchantContract
import `in`.okcredit.voice_first.ui.bulk_add.search_merchant.SearchMerchantViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class SearchMerchantActivityModule {

    @Binds
    @BulkAddTransactions
    abstract fun activity(activity: SearchMerchantActivity): AppCompatActivity

    companion object {

        @Provides
        fun initialState(): SearchMerchantContract.State = SearchMerchantContract.State()

        @Provides
        fun viewModel(
            activity: SearchMerchantActivity,
            viewModelProvider: Provider<SearchMerchantViewModel>,
        ): MviViewModel<SearchMerchantContract.State> = activity.createViewModel(viewModelProvider)
    }
}
