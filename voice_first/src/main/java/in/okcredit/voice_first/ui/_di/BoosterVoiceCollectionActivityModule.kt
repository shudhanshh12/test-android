package `in`.okcredit.voice_first.ui._di

import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.voice_first.ui.voice_collection.BoosterVoiceCollectionActivity
import `in`.okcredit.voice_first.ui.voice_collection.BoosterVoiceCollectionActivity.Companion.KEY_VOICE_MESSAGE_INPUT
import `in`.okcredit.voice_first.ui.voice_collection.BoosterVoiceCollectionContract
import `in`.okcredit.voice_first.ui.voice_collection.BoosterVoiceCollectionViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.android.base.extensions.itOrBlank
import javax.inject.Provider

@Module
abstract class BoosterVoiceCollectionActivityModule {

    companion object {
        @Provides
        fun initialState(activity: BoosterVoiceCollectionActivity): BoosterVoiceCollectionContract.State {
            val voiceMessageInput = activity.intent.getStringExtra(KEY_VOICE_MESSAGE_INPUT).itOrBlank()
            return BoosterVoiceCollectionContract.State(voiceMessageInput)
        }

        @Provides
        fun viewModel(
            activity: BoosterVoiceCollectionActivity,
            viewModelProvider: Provider<BoosterVoiceCollectionViewModel>,
        ): MviViewModel<BoosterVoiceCollectionContract.State> = activity.createViewModel(viewModelProvider)
    }

    @Binds
    abstract fun activity(activity: BoosterVoiceCollectionActivity): AppCompatActivity
}
