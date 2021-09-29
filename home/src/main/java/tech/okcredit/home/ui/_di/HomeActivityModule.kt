package tech.okcredit.home.ui._di

import `in`.okcredit.shared.base.MviViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.FragmentScope
import tech.okcredit.help.helpHome.HelpHomeFragment
import tech.okcredit.help.helpHome.di.HelpHomeModule
import tech.okcredit.home.ui.activity.HomeActivity
import tech.okcredit.home.ui.activity.HomeActivity.Companion.EXTRA_WEB_URL
import tech.okcredit.home.ui.activity.HomeActivityContract
import tech.okcredit.home.ui.activity.HomeActivityViewModel
import tech.okcredit.home.ui.payables_onboarding.video_backed_carousel_fragment.VideoBackedCarouselFragment
import tech.okcredit.home.ui.payables_onboarding.video_backed_carousel_fragment.di.VideoBackedCarouselModule
import javax.inject.Provider

@Module(
    includes = [
        HomeModule::class
    ]
)
abstract class HomeActivityModule {

    @Binds
    abstract fun activity(activity: HomeActivity): AppCompatActivity

    @ContributesAndroidInjector(modules = [VideoBackedCarouselModule::class])
    abstract fun videoBackedCarouselFragment(): VideoBackedCarouselFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [HelpHomeModule::class])
    abstract fun helpHomeFragment(): HelpHomeFragment

    companion object {

        @Provides
        fun initialState(): HomeActivityContract.State = HomeActivityContract.State

        @Provides
        @ViewModelParam(EXTRA_WEB_URL)
        fun webUrl(activity: HomeActivity): String? {
            return activity.intent.getStringExtra(EXTRA_WEB_URL)
        }

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            activity: HomeActivity,
            viewModelProvider: Provider<HomeActivityViewModel>
        ): MviViewModel<HomeActivityContract.State> = activity.createViewModel(viewModelProvider)
    }
}
