package `in`.okcredit.di.binding.communications

import `in`.okcredit.navigation.NavigationActivity
import `in`.okcredit.onboarding.di.OnboardingFragmentModule
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import tech.okcredit.home.ui._di.HomeModule

@Module(
    includes = [
        OnboardingFragmentModule::class,
        HomeModule::class
    ]
)
abstract class NavigationActivityModule {

    @Binds
    abstract fun activity(activity: NavigationActivity): AppCompatActivity
}
