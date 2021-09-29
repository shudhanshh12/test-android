package `in`.okcredit.di

import `in`.okcredit.App
import `in`.okcredit.analytics.di.AnalyticsModule
import `in`.okcredit.backend._id.BackendWorkerModule
import `in`.okcredit.backend._offline._di.OfflineModule
import `in`.okcredit.backend._offline.usecase.SyncDeleteTransactionImage
import `in`.okcredit.backend._offline.usecase.SyncTransactionImage
import `in`.okcredit.backend._offline.usecase.UpdateTransactionNote
import `in`.okcredit.business_health_dashboard.di.BusinessHealthDashboardModule
import `in`.okcredit.cashback.di.CashbackModule
import `in`.okcredit.collection_ui.di.CollectionUiModule
import `in`.okcredit.communication_inappnotification._di.CommunicationInAppNotificationModule
import `in`.okcredit.dynamicview.di.CustomizationModule
import `in`.okcredit.dynamicview.di.DynamicViewModule
import `in`.okcredit.expense.sdk.ExpenseModule
import `in`.okcredit.fileupload._id.FileUploadModule
import `in`.okcredit.frontend.di.FrontendModule
import `in`.okcredit.frontend.di.WebExperimentsModule
import `in`.okcredit.individual.di.IndividualModule
import `in`.okcredit.installedpackges.di.InstalledPackagesModule
import `in`.okcredit.merchant.collection.sdk.CollectionModule
import `in`.okcredit.merchant.core._di.CoreModule
import `in`.okcredit.merchant.customer_ui.di.CustomersModule
import `in`.okcredit.merchant.device.sdk.DeviceModule
import `in`.okcredit.merchant.rewards.di_.RewardsModule
import `in`.okcredit.merchant.sdk.BusinessModule
import `in`.okcredit.merchant.suppliercredit.sdk.SupplierCreditModule
import `in`.okcredit.merchant.ui.di.BusinessUiModule
import `in`.okcredit.onboarding.di.OnboardingModule
import `in`.okcredit.payment.di.PaymentModule
import `in`.okcredit.sales_sdk.sdk.SalesModule
import `in`.okcredit.shared.calculator.CalculatorModule
import `in`.okcredit.shared.di.SharedModule
import `in`.okcredit.shared.mini_calculator.MiniCalculatorModule
import `in`.okcredit.shared.service.keyval._di.KeyValModule
import `in`.okcredit.storesms.di.StoreSmsModule
import `in`.okcredit.supplier.di.SupplierModule
import `in`.okcredit.supplier.payment_process.di.SupplierPaymentDialogModule
import `in`.okcredit.user_migration.presentation.di.UserMigrationModule
import `in`.okcredit.user_migration.presentation.server.di_.UserMigrationServerModule
import `in`.okcredit.voice_first._di.BulkAddTransactionsModule
import `in`.okcredit.voice_first._di.VoiceFirstModule
import android.app.Application
import com.camera._di.CameraModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule
import merchant.okcredit.accounting.di.AccountingModule
import merchant.okcredit.gamification.ipl._di.IplModule
import merchant.okcredit.ok_doc.di.OkDocModule
import merchant.okcredit.user_stories.di.UserStoriesModule
import tech.okcredit.account_chat_sdk._di.ChatModule
import tech.okcredit.account_chat_ui.message_layout.SendMessageModule
import tech.okcredit.account_chat_ui.message_list_layout.MessageListModule
import tech.okcredit.android.ab.sdk.AbModule
import tech.okcredit.android.auth.AuthModule
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.di.BaseModule
import tech.okcredit.android.communication.brodcaste_receiver.NotificationDeleteReceiver
import tech.okcredit.android.communication.sdk.CommunicationModule
import tech.okcredit.android.referral.di.ReferralModule
import tech.okcredit.android.referral.di.ShareReferralFragmentModule
import tech.okcredit.android.referral.ui.referral_rewards_v1.di_.ReferralRewardsModule
import tech.okcredit.applock.di.AppLockModuleV2
import tech.okcredit.base.network.NetworkModule
import tech.okcredit.base.network.di.RetrofitModule
import tech.okcredit.contacts.sdk.ContactsModule
import tech.okcredit.help.di.HelpModule
import tech.okcredit.home.ui._di.DashboardModule
import tech.okcredit.okstream._di.OkStreamModule
import tech.okcredit.sdk.di.BillModule
import tech.okcredit.userSupport.sdk.UserSupportModule

@AppScope
@Component(
    modules = [
        AppModule::class,
        AndroidInjectionModule::class,
        AndroidSupportInjectionModule::class,
        KeyValModule::class,
        AppActivityModule::class,
        OfflineModule::class,
        RewardsModule::class,
        SupplierCreditModule::class,
        AbModule::class,
        ReferralModule::class,
        UserSupportModule::class,
        BusinessModule::class,
        IndividualModule::class,
        FileUploadModule::class,
        AnalyticsModule::class,
        CommunicationModule::class,
        CommunicationInAppNotificationModule::class,
        DeviceModule::class,
        BaseModule::class,
        AuthModule::class,
        RetrofitModule::class,
        ReferralRewardsModule::class,
        FrontendModule::class,
        ExpenseModule::class,
        DynamicViewModule::class,
        CoreModule::class,
        ContactsModule::class,
        SalesModule::class,
        CustomizationModule::class,
        NetworkModule::class,
        WebExperimentsModule::class,
        OnboardingModule::class,
        ChatModule::class,
        CalculatorModule::class,
        MiniCalculatorModule::class,
        MessageListModule::class,
        StoreSmsModule::class,
        PaymentModule::class,
        CashbackModule::class,
        InstalledPackagesModule::class,
        AccountingModule::class,
        UserMigrationModule::class,
        UserMigrationServerModule::class,
        SendMessageModule::class,
        DashboardModule::class,
        BusinessHealthDashboardModule::class,
        CollectionUiModule::class,
        SupplierModule::class,
        BackendWorkerModule::class,
        SupplierPaymentDialogModule::class,
        UserStoriesModule::class,
        BillModule::class,
        HelpModule::class,
        CustomersModule::class,
        AppLockModuleV2::class,
        tech.okcredit.android.referral.sdk.ReferralModule::class,
        ShareReferralFragmentModule::class,
        CameraModule::class,
        SharedModule::class,
        OkDocModule::class,
        CollectionModule::class,
        OkStreamModule::class,
        VoiceFirstModule::class,
        BusinessUiModule::class,
        BulkAddTransactionsModule::class,
        IplModule::class,
    ]
)

interface AppComponent {

    fun inject(app: App)

    fun inject(receiver: NotificationDeleteReceiver)

    fun inject(worker: SyncDeleteTransactionImage)

    fun inject(worker: UpdateTransactionNote)

    fun inject(worker: SyncTransactionImage)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun app(app: Application): Builder
        fun build(): AppComponent
    }
}
