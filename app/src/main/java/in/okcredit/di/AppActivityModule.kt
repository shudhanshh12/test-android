package `in`.okcredit.di

import `in`.okcredit.di.binding.communications.NavigationActivityModule
import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.ui.MainActivityTranslucentFullScreen
import `in`.okcredit.frontend.ui.SupplierActivity
import `in`.okcredit.frontend.ui._di.MainActivityTranslucentFullscreenModule
import `in`.okcredit.navigation.MainActivityModule
import `in`.okcredit.navigation.NavigationActivity
import `in`.okcredit.navigation.SupplierActivityModule
import `in`.okcredit.notification.DeepLinkActivity
import `in`.okcredit.onboarding.language.LanguageSelectionActivity
import `in`.okcredit.onboarding.language.LanguageSelectionModule
import `in`.okcredit.onboarding.launcher.LauncherFragment
import `in`.okcredit.onboarding.social_validation.SocialValidationActivity
import `in`.okcredit.onboarding.social_validation.SocialValidationModule
import `in`.okcredit.sales_ui.SalesActivity
import `in`.okcredit.sales_ui.di.SalesActivityModule
import `in`.okcredit.ui.app_lock.forgot.ForgotAppLockActivity
import `in`.okcredit.ui.app_lock.forgot._di.ForgotAppLockActivity_Module
import `in`.okcredit.ui.app_lock.preference.AppLockPrefActivity
import `in`.okcredit.ui.app_lock.preference._di.AppLockPrefActivityModule
import `in`.okcredit.ui.app_lock.prompt.AppLockPromptActivity
import `in`.okcredit.ui.app_lock.prompt._di.AppLockPromptActivity_Module
import `in`.okcredit.ui.app_lock.set.AppLockActivity
import `in`.okcredit.ui.app_lock.set._di.AppLockActivityModule
import `in`.okcredit.ui.customer_profile.CustomerProfileActivity
import `in`.okcredit.ui.customer_profile._di.CustomerProfileActivityModule
import `in`.okcredit.ui.delete_customer.DeleteCustomerActivity
import `in`.okcredit.ui.delete_customer._di.DeleteCustomerActivityModule
import `in`.okcredit.ui.delete_txn.DeleteTransactionActivity
import `in`.okcredit.ui.delete_txn._di.DeleteTxActivityModule
import `in`.okcredit.ui.delete_txn.supplier.supplier.DeleteSupplierActivity
import `in`.okcredit.ui.delete_txn.supplier.supplier._di.DeleteSupplierActivityModule
import `in`.okcredit.ui.delete_txn.supplier.transaction.DeleteSupplierTransactionActivity
import `in`.okcredit.ui.delete_txn.supplier.transaction._di.DeleteSupplierTxActivityModule
import `in`.okcredit.ui.language.InAppLanguageActivity
import `in`.okcredit.ui.language._di.InAppLanguageActivity_Module
import `in`.okcredit.ui.launcher._di.LauncherFragmentModule
import `in`.okcredit.ui.reset_pwd.ResetPwdActivity
import `in`.okcredit.ui.reset_pwd._di.ResetPasswordActivityFragmentModule
import `in`.okcredit.ui.supplier_profile.SupplierProfileActivity
import `in`.okcredit.ui.supplier_profile._di.SupplierProfileActivityModule
import `in`.okcredit.ui.trasparent.TransparentDeeplinkActivity
import `in`.okcredit.ui.whatsapp.WhatsAppActivity
import `in`.okcredit.ui.whatsapp._di.WhatsAppActivityModule
import `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet.UploadOptionBottomSheet
import `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet.di_.UploadOptionBottomSheetModule
import `in`.okcredit.web_features.cash_counter.CashCounterActivity
import `in`.okcredit.web_features.cash_counter._di.CashCounterActivityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.account_chat_ui.chat_activity.ChatActivity
import tech.okcredit.account_chat_ui.chat_activity.ChatActivityModule
import tech.okcredit.android.referral.ui.referral_in_app_bottomsheet.ReferralInAppBottomSheet
import tech.okcredit.android.referral.ui.referral_in_app_bottomsheet.di.ReferralInAppModule
import tech.okcredit.base.dagger.di.scope.ActivityScope
import tech.okcredit.bill_management_ui.BillActivity
import tech.okcredit.bill_management_ui.BillActivity_Module
import tech.okcredit.bill_management_ui.bill_camera.BillCameraActivity
import tech.okcredit.bill_management_ui.editBill.EditBillActivity
import tech.okcredit.bill_management_ui.editBill.EditBillModule
import tech.okcredit.bill_management_ui.enhance_image.EnhanceImageActivity
import tech.okcredit.bill_management_ui.enhance_image.EnhanceImageModule
import tech.okcredit.bill_management_ui.selected_bills.SelectedBillActivity
import tech.okcredit.bill_management_ui.selected_bills.SelectedBillModule
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileTransparentActivity
import tech.okcredit.home.dialogs.customer_profile_dialog._di.CustomerProfileTransparentActivityModule
import tech.okcredit.home.ui._di.AccountActivityModule
import tech.okcredit.home.ui._di.BusinessHealthDashboardActivityModule
import tech.okcredit.home.ui._di.HomeActivityModule
import tech.okcredit.home.ui._di.HomeSearchActivityModule
import tech.okcredit.home.ui._di.SettingsActivityModule
import tech.okcredit.home.ui.acccountV2.ui.AccountActivity
import tech.okcredit.home.ui.activity.HomeActivity
import tech.okcredit.home.ui.activity.HomeSearchActivity
import tech.okcredit.home.ui.add_transaction_home_search.AddTxnShortcutSearchActivity
import tech.okcredit.home.ui.add_transaction_home_search.di.AddTxnShortcutSearchActivityModule
import tech.okcredit.home.ui.business_health_dashboard.BusinessHealthDashboardActivity
import tech.okcredit.home.ui.settings.SettingsActivity
import tech.okcredit.web.ui.WebViewActivity

@Module
abstract class AppActivityModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [LauncherFragmentModule::class])
    abstract fun launcherFragment(): LauncherFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [BusinessHealthDashboardActivityModule::class])
    abstract fun businessHealthDashboardActivity(): BusinessHealthDashboardActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ResetPasswordActivityFragmentModule::class])
    abstract fun forgotPwdActivity(): ResetPwdActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [CustomerProfileTransparentActivityModule::class])
    abstract fun customerProfileTransparentActivity(): CustomerProfileTransparentActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [DeleteCustomerActivityModule::class])
    abstract fun deleteCustomerActivity(): DeleteCustomerActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [DeleteSupplierActivityModule::class])
    abstract fun deleteSupplierActivity(): DeleteSupplierActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [InAppLanguageActivity_Module::class])
    abstract fun inAppLanguageActivity(): InAppLanguageActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [AppLockPromptActivity_Module::class])
    abstract fun appLockPromptActivity(): AppLockPromptActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ForgotAppLockActivity_Module::class])
    abstract fun forgotAppLockActivity(): ForgotAppLockActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [AppLockPrefActivityModule::class])
    abstract fun appLockPrefActivity(): AppLockPrefActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [AppLockActivityModule::class])
    abstract fun appLockActivity(): AppLockActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [DeleteTxActivityModule::class])
    abstract fun deleteTransactionActivity(): DeleteTransactionActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [DeleteSupplierTxActivityModule::class])
    abstract fun deleteSupplierTransactionActivity(): DeleteSupplierTransactionActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [WhatsAppActivityModule::class])
    abstract fun whatsAppActivity(): WhatsAppActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun mainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityTranslucentFullscreenModule::class])
    abstract fun mainActivityTranslucentFullScreen(): MainActivityTranslucentFullScreen

    @ActivityScope
    @ContributesAndroidInjector(modules = [SupplierProfileActivityModule::class])
    abstract fun supplierProfileActivity(): SupplierProfileActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun deepLinkActivity(): DeepLinkActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun webExperimentActivity(): WebViewActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [NavigationActivityModule::class])
    abstract fun navigationActivity(): NavigationActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [LanguageSelectionModule::class])
    abstract fun languageSelectionActivity(): LanguageSelectionActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [SocialValidationModule::class])
    abstract fun socialValidationActivity(): SocialValidationActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [HomeActivityModule::class])
    abstract fun homeActivity(): HomeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [CustomerProfileActivityModule::class])
    abstract fun customerProfileActivity(): CustomerProfileActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [AddTxnShortcutSearchActivityModule::class])
    abstract fun addTransactionHomeSearchActivity(): AddTxnShortcutSearchActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun transparentActivity(): TransparentDeeplinkActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ChatActivityModule::class])
    abstract fun chatActivity(): ChatActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BillActivity_Module::class])
    abstract fun billActivity(): BillActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [CashCounterActivityModule::class])
    abstract fun cashCounterActivity(): CashCounterActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun billCameraActivity(): BillCameraActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [SelectedBillModule::class])
    abstract fun selectedBillActivity(): SelectedBillActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [EnhanceImageModule::class])
    abstract fun enhanceImageActivity(): EnhanceImageActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [SalesActivityModule::class])
    abstract fun salesActivity(): SalesActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [EditBillModule::class])
    abstract fun editBillActivity(): EditBillActivity

    @ContributesAndroidInjector(modules = [SettingsActivityModule::class])
    abstract fun settingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [AccountActivityModule::class])
    abstract fun accountActivity(): AccountActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [SupplierActivityModule::class])
    abstract fun supplierActivity(): SupplierActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [HomeSearchActivityModule::class])
    abstract fun homeSearchActivity(): HomeSearchActivity

    // TODO(Move Inside user Migration module)
    @ContributesAndroidInjector(modules = [UploadOptionBottomSheetModule::class])
    abstract fun uploadOptionBottomSheet(): UploadOptionBottomSheet

    // TODO(Moved to Referral Module) currently ReferralInApp Bottomsheet is opening from Home Fragment
    @ContributesAndroidInjector(modules = [ReferralInAppModule::class])
    abstract fun referralInAppBottomSheet(): ReferralInAppBottomSheet
}
