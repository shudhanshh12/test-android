package tech.okcredit.applock.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.applock.changePin.ChangeSecurityPinFragment
import tech.okcredit.applock.changePin.di.ChangeSecurityPinModule
import tech.okcredit.applock.enterPin.EnterPinFragment
import tech.okcredit.applock.enterPin.di.EnterPinModule
import tech.okcredit.applock.pinLock.PinLockFragment
import tech.okcredit.applock.pinLock.di.PinLockModule
import tech.okcredit.base.dagger.di.scope.FragmentScope

@Module
abstract class AppLockActivityModuleV2 {
    @FragmentScope
    @ContributesAndroidInjector(modules = [ChangeSecurityPinModule::class])
    abstract fun changePinScreen(): ChangeSecurityPinFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [PinLockModule::class])
    abstract fun pinLockScreen(): PinLockFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [EnterPinModule::class])
    abstract fun enterPinScreen(): EnterPinFragment
}
