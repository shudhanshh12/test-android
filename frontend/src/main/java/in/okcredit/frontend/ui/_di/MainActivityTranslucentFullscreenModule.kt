package `in`.okcredit.frontend.ui._di

import `in`.okcredit.frontend.ui.applock.AppLockFragment
import `in`.okcredit.frontend.ui.applock._di.AppLockModule
import `in`.okcredit.frontend.ui.merchant_profile.merchantinput.MerchantInputFragment
import `in`.okcredit.frontend.ui.merchant_profile.merchantinput._di.MerchantInputModule
import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.base.dagger.di.scope.FragmentScope
import tech.okcredit.home.dialogs.MerchantAddressConfirmationBottomSheetDialog

@Module
abstract class MainActivityTranslucentFullscreenModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [MerchantInputModule::class])
    abstract fun merchantInputScreen(): MerchantInputFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [AppLockModule::class])
    abstract fun appLockScreen(): AppLockFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun merchantAddressConfirmationBottomSheetDialog(): MerchantAddressConfirmationBottomSheetDialog
}
