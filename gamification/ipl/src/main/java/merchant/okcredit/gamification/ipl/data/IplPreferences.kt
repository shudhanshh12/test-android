package merchant.okcredit.gamification.ipl.data

import android.content.Context
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Single
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

@Reusable
class IplPreferences @Inject constructor(
    context: Context,
    sharedPreferencesMigrationLogger: Lazy<SharedPreferencesMigrationHandler.Logger>,
    migrations: Lazy<Migrations>,
) : OkcSharedPreferences(
    context = context,
    prefName = SHARED_PREF_NAME,
    version = VERSION,
    migrationList = migrations.get().getList(),
    sharedPreferencesMigrationLogger = sharedPreferencesMigrationLogger,
) {

    companion object {
        const val VERSION = 1
        private const val SHARED_PREF_NAME = "ipl_gamification"

        private const val PREF_INDIVIDUAL_GAMES_EDUCATION = "games_education"
        private const val PREF_INDIVIDUAL_HOME_SCREEN_DC_TOOLTIP = "home_screen_dc_tooltip"
    }

    class Migrations @Inject constructor() {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration.emptyMigration(0, 1)
    }

    fun setGamesEducationView() = rxCompletable {
        set(PREF_INDIVIDUAL_GAMES_EDUCATION, true, Scope.Individual)
    }

    fun setHomeScreenDcToolTipViewed() = rxCompletable {
        set(PREF_INDIVIDUAL_HOME_SCREEN_DC_TOOLTIP, true, Scope.Individual)
    }

    fun hasGamesEducationView(): Single<Boolean> {
        return getBoolean(PREF_INDIVIDUAL_GAMES_EDUCATION, Scope.Individual).asObservable().firstOrError()
    }

    suspend fun hasHomeScreenDcToolTipViewed(): Boolean {
        return getBoolean(PREF_INDIVIDUAL_HOME_SCREEN_DC_TOOLTIP, Scope.Individual).first()
    }
}
