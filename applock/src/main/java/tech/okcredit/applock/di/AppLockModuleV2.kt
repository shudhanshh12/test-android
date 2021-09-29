package tech.okcredit.applock.di

import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import tech.okcredit.applock.AppLockActivityV2
import tech.okcredit.applock.AppLockImpl
import tech.okcredit.applock.AppLockTrackerImpl
import tech.okcredit.applock.MerchantPrefSyncImpl
import tech.okcredit.base.dagger.di.scope.ActivityScope
import tech.okcredit.contract.AppLock
import tech.okcredit.contract.AppLockTracker
import tech.okcredit.contract.MerchantPrefSyncStatus

@Module
abstract class AppLockModuleV2 {

    @ActivityScope
    @ContributesAndroidInjector(modules = [AppLockActivityModuleV2::class])
    abstract fun appLockActivityv2(): AppLockActivityV2

    @Binds
    @Reusable
    abstract fun appLock(appLock: AppLockImpl): AppLock

    @Binds
    @Reusable
    abstract fun appLockTracker(appLock: AppLockTrackerImpl): AppLockTracker

    @Binds
    @Reusable
    abstract fun merchantPrefSyncStatus(merchantPrefSyncImpl: MerchantPrefSyncImpl): MerchantPrefSyncStatus
}
