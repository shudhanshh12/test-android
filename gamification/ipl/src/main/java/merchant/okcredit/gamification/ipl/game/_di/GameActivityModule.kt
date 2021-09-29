package merchant.okcredit.gamification.ipl.game._di

import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import merchant.okcredit.gamification.ipl.game.ui.GameActivity
import merchant.okcredit.gamification.ipl.game.ui.GameFragment

@Module
abstract class GameActivityModule {

    @ContributesAndroidInjector(modules = [GameFragmentModule::class])
    abstract fun gameFragment(): GameFragment

    @Binds
    abstract fun activity(activity: GameActivity): AppCompatActivity
}
