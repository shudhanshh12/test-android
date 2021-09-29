package tech.okcredit.android.referral.store

import android.content.Context
import androidx.core.content.edit
import dagger.Lazy
import dagger.Reusable
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

@Reusable
class ReferralPreferences @Inject constructor(
    context: Lazy<Context>,
    sharedPreferencesMigrationLogger: Lazy<SharedPreferencesMigrationHandler.Logger>,
    migrations: Lazy<Migrations>,
) : OkcSharedPreferences(
    context = context.get(),
    prefName = SHARED_PREF_NAME,
    version = SHARED_PREF_VERSION,
    migrationList = migrations.get().getList(),
    sharedPreferencesMigrationLogger = sharedPreferencesMigrationLogger,
) {
    companion object {
        private const val SHARED_PREF_NAME = "referral"
        private const val SHARED_PREF_VERSION = 1
    }

    class Migrations @Inject constructor(
        private val context: Lazy<Context>,
    ) {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration(0, 1) { prefs ->
            val referralPref = context.get().getSharedPreferences("referralPref", Context.MODE_PRIVATE)

            // copy all data from deprecated 'referralPref' to ReferralPreferences
            prefs.edit(commit = true) {
                referralPref.all.forEach { (key, value) ->
                    SharedPreferencesMigration.put(key, value, this)
                }
            }
        }
    }
}
