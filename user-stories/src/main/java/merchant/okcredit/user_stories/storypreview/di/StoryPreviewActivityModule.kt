package merchant.okcredit.user_stories.storypreview.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import merchant.okcredit.user_stories.storypreview.StoryPreviewFragment
import tech.okcredit.base.dagger.di.scope.FragmentScope

@Module
abstract class StoryPreviewActivityModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [StoryPreviewFragmentModule::class])
    abstract fun subscriptionListScreen(): StoryPreviewFragment
}
