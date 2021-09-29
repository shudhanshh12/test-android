package `in`.okcredit.frontend.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.home.dialogs.MobileRechargeSmsBottomSheetDialog
import tech.okcredit.home.dialogs.MoneyTransferSmsBottomSheetDialog

@Module
abstract class WebExperimentsModule {

    @ContributesAndroidInjector
    abstract fun rechargeBottomSheetDialog(): MobileRechargeSmsBottomSheetDialog

    @ContributesAndroidInjector
    abstract fun moneySMSBottomSheetDialog(): MoneyTransferSmsBottomSheetDialog
}
