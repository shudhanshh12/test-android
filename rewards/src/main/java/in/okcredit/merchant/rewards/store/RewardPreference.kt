package `in`.okcredit.merchant.rewards.store

import android.content.Context
import androidx.core.content.edit
import dagger.Lazy
import dagger.Reusable
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

@Reusable
class RewardPreference @Inject constructor(
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
        const val SHARED_PREF_NAME = "reward"
        const val VERSION = 1
    }

    class Migrations @Inject constructor(
        private val context: Lazy<Context>,
    ) {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration(0, 1) { prefs ->
            val rewardsPref = context.get().getSharedPreferences("RewardPref", Context.MODE_PRIVATE)

            // copy all data from deprecated 'rewards' to RewardPreference
            prefs.edit(commit = true) {
                rewardsPref.all.forEach { (key, value) ->
                    SharedPreferencesMigration.put(key, value, this)
                }
            }
        }
    }
}
