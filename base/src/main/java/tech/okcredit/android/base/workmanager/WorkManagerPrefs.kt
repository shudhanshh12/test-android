package tech.okcredit.android.base.workmanager

import android.content.Context
import dagger.Lazy
import dagger.Reusable
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

/**
 * [WorkManagerPrefs] stores the last scheduled timestamps of workers using [RateLimit]
 */
@Reusable
class WorkManagerPrefs @Inject constructor(
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
        private const val SHARED_PREF_NAME = "okc-work-manager"
        private const val SHARED_PREF_VERSION = 1
    }

    class Migrations @Inject constructor() {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration.emptyMigration(0, 1)
    }
}
