package `in`.okcredit.onboarding.language

import `in`.okcredit.shared.base.MviViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class LanguageSelectionModule {

    @Binds
    abstract fun activity(activity: LanguageSelectionActivity): AppCompatActivity

    companion object {

        @Provides
        fun initialState(): LanguageSelectionContract.State = LanguageSelectionContract.State()

        @Provides
        fun viewModel(
            activity: LanguageSelectionActivity,
            viewModelProvider: Provider<LanguageSelectionViewModel>,
        ): MviViewModel<LanguageSelectionContract.State> = activity.createViewModel(viewModelProvider)
    }
}
