package `in`.okcredit.voice_first._di

import `in`.okcredit.voice_first.BuildConfig
import `in`.okcredit.voice_first.contract.ResetDraftTransactions
import `in`.okcredit.voice_first.data.bulk_add.BulkAddApiService
import `in`.okcredit.voice_first.ui._di.BulkAddTransactionsActivityModule
import `in`.okcredit.voice_first.ui._di.EditDraftTransactionActivityModule
import `in`.okcredit.voice_first.ui._di.SearchMerchantActivityModule
import `in`.okcredit.voice_first.ui.bulk_add.BulkAddTransactionsActivity
import `in`.okcredit.voice_first.ui.bulk_add.edit_draft.EditDraftTransactionActivity
import `in`.okcredit.voice_first.ui.bulk_add.search_merchant.SearchMerchantActivity
import `in`.okcredit.voice_first.usecase.bulk_add.ClearDraftTransactions
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.extensions.delegatingCallFactory

@Module
abstract class BulkAddTransactionsModule {

    @ContributesAndroidInjector(modules = [BulkAddTransactionsActivityModule::class])
    abstract fun bulkAddTransactionsActivity(): BulkAddTransactionsActivity

    @ContributesAndroidInjector(modules = [EditDraftTransactionActivityModule::class])
    abstract fun editDraftTransactionActivity(): EditDraftTransactionActivity

    @ContributesAndroidInjector(modules = [SearchMerchantActivityModule::class])
    abstract fun searchMerchantActivity(): SearchMerchantActivity

    @Binds
    abstract fun clearDraftTransactions(clearDraftTransactions: ClearDraftTransactions): ResetDraftTransactions

    companion object {

        @Provides
        @BulkAddTransactions
        fun retrofitBulkAdd(
            @AuthOkHttpClient client: Lazy<OkHttpClient>,
            converterFactory: GsonConverterFactory,
        ) = Retrofit.Builder()
            .baseUrl(BuildConfig.DRAFT_SERVICE_URL)
            .delegatingCallFactory(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(converterFactory)
            .build()

        @Provides
        fun bulkAddApiService(@BulkAddTransactions retrofit: Retrofit) = retrofit.create(
            BulkAddApiService::class.java
        )
    }
}
