package merchant.okcredit.user_stories.homestory.di

import `in`.okcredit.shared.base.IBaseLayoutViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import merchant.okcredit.user_stories.HomeStoryNavigationImpl
import merchant.okcredit.user_stories.contract.HomeStoryNavigation
import merchant.okcredit.user_stories.homestory.HomeUserStoryContract
import merchant.okcredit.user_stories.homestory.HomeUserStoryViewModel

@Module
abstract class HomeUserStoryModule {

    @Binds
    abstract fun homeNav(homeStoriesNavigationImpl: HomeStoryNavigationImpl): HomeStoryNavigation

    companion object {
        @Provides
        fun initialState() = HomeUserStoryContract.State()

        @Provides
        fun homeUserStoryViewModel(homeUserStoryViewModel: HomeUserStoryViewModel): IBaseLayoutViewModel<HomeUserStoryContract.State> {
            return homeUserStoryViewModel
        }
    }
}
