package `in`.okcredit.supplier.di

import `in`.okcredit.backend.server.riskInternal.RiskApiClient
import `in`.okcredit.backend.server.riskInternal.RiskQualifier
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.supplier.BuildConfig.RISK_URL
import `in`.okcredit.supplier.SupplierNavigatorImpl
import `in`.okcredit.supplier.data.SupplierCreditRepositoryImpl
import `in`.okcredit.supplier.payment_process.SupplierPaymentDialogScreen
import `in`.okcredit.supplier.payment_process.di.SupplierPaymentDialogModule
import `in`.okcredit.supplier.statement.SupplierAccountStatementActivity
import `in`.okcredit.supplier.statement.di.SupplierAccountStatementActivityModule
import `in`.okcredit.supplier.statement.usecase.GetSupplierBalanceAndCount
import `in`.okcredit.supplier.supplier_limit_warning_bottomsheet.PaymentLimitWarningBottomSheet
import `in`.okcredit.supplier.supplier_profile_bottom_sheet.SupplierProfileBottomSheet
import `in`.okcredit.supplier.supplier_profile_bottom_sheet.di.SupplierProfileModule
import `in`.okcredit.supplier.usecase.GetSupplier
import `in`.okcredit.supplier.usecase.IsAccountChatEnabledForSupplierImpl
import `in`.okcredit.supplier.usecase.IsNetworkReminderEnabledImpl
import `in`.okcredit.supplier.usecase.IsSupplierCollectionEnabledImpl
import `in`.okcredit.supplier.usecase.PutNotificationReminderActionImpl
import `in`.okcredit.supplier.usecase.PutNotificationReminderImpl
import `in`.okcredit.supplier.usecase.SyncSupplierEnabledCustomerIdsImpl
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import merchant.okcredit.accounting.ui.customer_support_exit_dialog.CustomerSupportExitDialog
import merchant.okcredit.accounting.ui.customer_support_exit_dialog.di.CustomerSupportExitModule
import merchant.okcredit.accounting.ui.customer_support_option_dialog.CustomerSupportOptionDialog
import merchant.okcredit.accounting.ui.customer_support_option_dialog.di.CustomerSupportOptionModule
import merchant.okcredit.supplier.contract.GetSupplierAccountNetBalance
import merchant.okcredit.supplier.contract.IsAccountChatEnabledForSupplier
import merchant.okcredit.supplier.contract.IsNetworkReminderEnabled
import merchant.okcredit.supplier.contract.IsSupplierCollectionEnabled
import merchant.okcredit.supplier.contract.PutNotificationReminder
import merchant.okcredit.supplier.contract.PutNotificationReminderAction
import merchant.okcredit.supplier.contract.SupplierNavigator
import merchant.okcredit.supplier.contract.SyncSupplierEnabledCustomerIds
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.extensions.delegatingCallFactory

@Module
abstract class SupplierModule {

    @ContributesAndroidInjector(modules = [SupplierAccountStatementActivityModule::class])
    abstract fun supplierAccountStatementActivity(): SupplierAccountStatementActivity

    @ContributesAndroidInjector(modules = [SupplierPaymentDialogModule::class])
    abstract fun supplierPaymentDialogScreen(): SupplierPaymentDialogScreen

    @ContributesAndroidInjector(modules = [SupplierProfileModule::class])
    abstract fun supplierProfileBottomSheet(): SupplierProfileBottomSheet

    @ContributesAndroidInjector
    abstract fun paymentLimitWarningBottomSheet(): PaymentLimitWarningBottomSheet

    @ContributesAndroidInjector(modules = [CustomerSupportOptionModule::class])
    abstract fun customerSupportOptionDialog(): CustomerSupportOptionDialog

    @ContributesAndroidInjector(modules = [CustomerSupportExitModule::class])
    abstract fun customerSupportExitDialog(): CustomerSupportExitDialog

    @Binds
    @Reusable
    abstract fun supplierNavigator(supplierNavigator: SupplierNavigatorImpl): SupplierNavigator

    @Binds
    @Reusable
    abstract fun isSupplierCollectionEnabled(isSupplierCollectionEnabledImpl: IsSupplierCollectionEnabledImpl): IsSupplierCollectionEnabled

    @Binds
    @Reusable
    abstract fun isAccountChatEnabledForSupplier(isAccountChatEnabledForSupplierImpl: IsAccountChatEnabledForSupplierImpl): IsAccountChatEnabledForSupplier

    @Binds
    @Reusable
    abstract fun isNetworkReminderEnabled(isNetworkReminderEnabled: IsNetworkReminderEnabledImpl): IsNetworkReminderEnabled

    @Binds
    @Reusable
    abstract fun putNotificationReminder(putNetworkReminderEnabled: PutNotificationReminderImpl): PutNotificationReminder

    @Binds
    @Reusable
    abstract fun putNotificationReminderAction(putNotificationReminderActionImpl: PutNotificationReminderActionImpl): PutNotificationReminderAction

    @Binds
    @Reusable
    abstract fun syncSupplierEnabledCustomerIds(syncSupplierEnabledCustomerIdsImpl: SyncSupplierEnabledCustomerIdsImpl): SyncSupplierEnabledCustomerIds

    @Binds
    @Reusable
    abstract fun getSupplier(getSupplier: GetSupplier): `in`.okcredit.merchant.suppliercredit.GetSupplier

    @Binds
    @Reusable
    abstract fun getSupplierAccountNetBalance(getSupplierAccountNetBalance: GetSupplierBalanceAndCount): GetSupplierAccountNetBalance

    @Binds
    @AppScope
    abstract fun supplierRepository(supplierCreditApiImpl: SupplierCreditRepositoryImpl): SupplierCreditRepository

    companion object {

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
            @RiskQualifier factory: MoshiConverterFactory,
            callAdapterFactory: RxJava2CallAdapterFactory,
        ): RiskApiClient {
            return Retrofit.Builder()
                .baseUrl(RISK_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(factory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create()
        }

        @Provides
        @RiskQualifier
        internal fun moshiConverterFactory(moshi: Moshi) =
            MoshiConverterFactory.create(moshi)

        @Provides
        @RiskQualifier
        internal fun moshi() = Moshi.Builder().build()
    }
}
