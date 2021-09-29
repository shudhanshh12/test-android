package merchant.okcredit.gamification.ipl.data

import dagger.Lazy
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject

class IplLocalDataSource @Inject constructor(
    private val iplPreferences: Lazy<IplPreferences>
) {

    fun setGamesEducationView() = iplPreferences.get().setGamesEducationView()

    fun hasGamesEducationView(): Single<Boolean> {
        return iplPreferences.get().hasGamesEducationView()
    }

    fun setHomeScreenDcToolTipViewed() = rxCompletable {
        iplPreferences.get().setHomeScreenDcToolTipViewed()
    }

    suspend fun hasHomeScreenDcToolTipViewed(): Boolean {
        return iplPreferences.get().hasHomeScreenDcToolTipViewed()
    }

    suspend fun clear() {
        iplPreferences.get().clear()
    }
}
