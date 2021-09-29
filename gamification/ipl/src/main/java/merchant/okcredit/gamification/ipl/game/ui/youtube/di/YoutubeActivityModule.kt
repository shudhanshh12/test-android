package merchant.okcredit.gamification.ipl.game.ui.youtube.di

import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import merchant.okcredit.gamification.ipl.game.ui.youtube.YoutubeActivity

@Module
abstract class YoutubeActivityModule {

    @Binds
    abstract fun activity(activity: YoutubeActivity): AppCompatActivity
}
