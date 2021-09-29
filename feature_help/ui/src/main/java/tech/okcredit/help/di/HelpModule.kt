package tech.okcredit.help.di

import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import tech.okcredit.feature_help.contract.GetContextualHelpIds
import tech.okcredit.feature_help.contract.GetSupportNumber
import tech.okcredit.help.HelpActivity
import tech.okcredit.help.contextual_help.GetContextualHelpIdsImpl
import tech.okcredit.userSupport.usecses.GetSupportNumberImpl

@Module
abstract class HelpModule {

    @ContributesAndroidInjector(modules = [HelpActivityModule::class])
    abstract fun helpActivity(): HelpActivity

    @Binds
    @Reusable
    abstract fun getHelpNumber(getSupportNumber: GetSupportNumberImpl): GetSupportNumber

    @Binds
    @Reusable
    abstract fun getContextualHelpIds(getContextualHelpIds: GetContextualHelpIdsImpl): GetContextualHelpIds
}
