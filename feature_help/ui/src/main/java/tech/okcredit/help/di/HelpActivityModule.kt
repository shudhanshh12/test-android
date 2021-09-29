package tech.okcredit.help.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.base.dagger.di.scope.FragmentScope
import tech.okcredit.feedback.feedback.FeedbackFragment
import tech.okcredit.feedback.feedback._di.FeedbackModule
import tech.okcredit.help.helpHome.HelpHomeFragment
import tech.okcredit.help.helpHome.di.HelpHomeModule
import tech.okcredit.help.help_details.HelpDetailsFragment
import tech.okcredit.help.help_details._di.HelpDetailsModule
import tech.okcredit.help.help_main.HelpFragment
import tech.okcredit.help.help_main._di.HelpFragmentModule
import tech.okcredit.help.helpcontactus.HelpContactUsFragment
import tech.okcredit.help.helpcontactus._di.HelpContactUsModule

@Module
abstract class HelpActivityModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [HelpFragmentModule::class])
    abstract fun helpScreen(): HelpFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [HelpHomeModule::class])
    abstract fun helpHomeScreen(): HelpHomeFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [HelpDetailsModule::class])
    abstract fun helpDetailsScreen(): HelpDetailsFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [HelpContactUsModule::class])
    abstract fun helpContactUsScreen(): HelpContactUsFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [FeedbackModule::class])
    abstract fun feedbackScreen(): FeedbackFragment
}
