package tech.okcredit.android.referral.ui.share.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.android.referral.share.ShareAppFragment
import tech.okcredit.android.referral.share.di.ShareAppModule
import tech.okcredit.base.dagger.di.scope.FragmentScope

@Module
abstract class ShareActivityModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [ShareAppModule::class])
    abstract fun shareAppFragment(): ShareAppFragment
}
