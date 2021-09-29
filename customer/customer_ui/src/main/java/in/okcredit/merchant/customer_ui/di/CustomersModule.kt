package `in`.okcredit.merchant.customer_ui.di

import `in`.okcredit.customer.contract.BulkReminderAnalytics
import `in`.okcredit.customer.contract.CustomerNavigator
import `in`.okcredit.customer.contract.CustomerRepository
import `in`.okcredit.customer.contract.GetBannerForBulkReminder
import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import `in`.okcredit.merchant.customer_ui.BuildConfig
import `in`.okcredit.merchant.customer_ui.CustomerNavigatorImpl
import `in`.okcredit.merchant.customer_ui.addrelationship.AddRelationshipActivity
import `in`.okcredit.merchant.customer_ui.addrelationship.AddRelationshipActivityModule
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import `in`.okcredit.merchant.customer_ui.data.local.db.CustomerDatabase
import `in`.okcredit.merchant.customer_ui.data.local.db.CustomerDatabase.Companion.getInstance
import `in`.okcredit.merchant.customer_ui.data.server.CustomerApiService
import `in`.okcredit.merchant.customer_ui.data.server.GooglePayApiService
import `in`.okcredit.merchant.customer_ui.data.server.StaffLinkApiService
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.di_.AddTxnActivityModule
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Activity
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2._di.BulkReminderV2Module
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.analytics.BulkReminderAnalyticsImpl
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.default_reminder_dialog.SelectReminderModeDialog
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.default_reminder_dialog.di.SelectReminderModeModule
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.send_reminder_dialog.SendReminderDialog
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.send_reminder_dialog.di.SendReminderModule
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase.GetBannerForBulkReminderImpl
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.BulkReminderTab
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.bulk_reminder_tab.BulkReminderTabModule
import `in`.okcredit.merchant.customer_ui.ui.payment.AddCustomerPaymentActivity
import `in`.okcredit.merchant.customer_ui.ui.payment.AddCustomerPaymentModule
import `in`.okcredit.merchant.customer_ui.ui.payment.success.PaymentSuccessActivity
import `in`.okcredit.merchant.customer_ui.ui.staff_link.StaffLinkActivity
import `in`.okcredit.merchant.customer_ui.ui.staff_link.di.StaffLinkActivityModule
import `in`.okcredit.merchant.customer_ui.ui.subscription.SubscriptionActivity
import `in`.okcredit.merchant.customer_ui.ui.subscription.di.SubscriptionActivityModule
import android.content.Context
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.extensions.delegatingCallFactory
import javax.inject.Named

@Module
abstract class CustomersModule {

    @Binds
    @Reusable
    abstract fun getCustomerApi(customerRepositoryImpl: CustomerRepositoryImpl): CustomerRepository

    @Binds
    @Reusable
    abstract fun customerNavigator(customerNavigatorImpl: CustomerNavigatorImpl): CustomerNavigator

    @Binds
    @Reusable
    abstract fun bulkReminderAnalytics(bulkReminderAnalyticsImpl: BulkReminderAnalyticsImpl): BulkReminderAnalytics

    @Binds
    @Reusable
    abstract fun getBannerForBulkReminder(getBannerForBulkReminder: GetBannerForBulkReminderImpl): GetBannerForBulkReminder

    @ContributesAndroidInjector(modules = [AddTxnActivityModule::class])
    abstract fun addTxnTransparentActivity(): AddTxnContainerActivity

    @ContributesAndroidInjector(modules = [SubscriptionActivityModule::class])
    abstract fun subscriptionActivity(): SubscriptionActivity

    @ContributesAndroidInjector(modules = [StaffLinkActivityModule::class])
    abstract fun staffLinkActivity(): StaffLinkActivity

    @ContributesAndroidInjector(modules = [AddCustomerPaymentModule::class])
    abstract fun addCustomerPaymentActivity(): AddCustomerPaymentActivity

    @ContributesAndroidInjector
    abstract fun paymentSuccessActivity(): PaymentSuccessActivity

    @ContributesAndroidInjector(modules = [BulkReminderV2Module::class])
    abstract fun bulkReminderV2Activity(): BulkReminderV2Activity

    @ContributesAndroidInjector(modules = [BulkReminderTabModule::class])
    abstract fun bulkReminderTabItem(): BulkReminderTab

    @ContributesAndroidInjector(modules = [SelectReminderModeModule::class])
    abstract fun selectReminderModeDialog(): SelectReminderModeDialog

    @ContributesAndroidInjector(modules = [SendReminderModule::class])
    abstract fun sendReminderDialog(): SendReminderDialog

    @ContributesAndroidInjector(modules = [AddRelationshipActivityModule::class])
    abstract fun addRelationshipActivity(): AddRelationshipActivity

    companion object {

        @Provides
        @AppScope
        fun customerDatabase(
            context: Context,
            migrationHandler: MultipleAccountsDatabaseMigrationHandler,
        ): CustomerDatabase = getInstance(context, migrationHandler)

        @Provides
        fun customerDatabaseDao(customerDatabase: CustomerDatabase) = customerDatabase.customerDatabaseDao()

        @Provides
        @Named("auto_credit")
        fun retrofit(
            @AuthOkHttpClient okHttpClient: Lazy<OkHttpClient>,
            converterFactory: GsonConverterFactory,
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.AUTO_CREDIT_BASE_URL)
                .delegatingCallFactory(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(converterFactory)
                .build()
        }

        @Provides
        @Named("google_pay")
        fun googlePayRetrofit(
            @AuthOkHttpClient okHttpClient: Lazy<OkHttpClient>,
            converterFactory: GsonConverterFactory,
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.GOOGLE_PAY_BASE_URL)
                .delegatingCallFactory(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(converterFactory)
                .build()
        }

        @Provides
        @Named("staff_link")
        fun staffLinkRetrofit(
            @AuthOkHttpClient okHttpClient: Lazy<OkHttpClient>,
            converterFactory: GsonConverterFactory,
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.STAFF_LINK_BASE_URL)
                .delegatingCallFactory(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(converterFactory)
                .build()
        }

        @Provides
        fun customerApiService(@Named("auto_credit") retrofit: Lazy<Retrofit>): CustomerApiService =
            retrofit.get().create(CustomerApiService::class.java)

        @Provides
        fun googlePayApiService(@Named("google_pay") retrofit: Lazy<Retrofit>): GooglePayApiService =
            retrofit.get().create(GooglePayApiService::class.java)

        @Provides
        fun staffLinkApiService(@Named("staff_link") retrofit: Lazy<Retrofit>): StaffLinkApiService =
            retrofit.get().create(StaffLinkApiService::class.java)
    }
}
