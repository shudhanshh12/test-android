package `in`.okcredit.installedpackges.data

import android.content.Context
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

@Reusable
class InstalledPackagesPreference @Inject constructor(
    context: Context,
    sharedPreferencesMigrationLogger: Lazy<SharedPreferencesMigrationHandler.Logger>,
    migrations: Lazy<Migrations>,
) : OkcSharedPreferences(
    context = context,
    prefName = SHARED_PREF_NAME,
    version = SHARED_PREF_VERSION,
    migrationList = migrations.get().getList(),
    sharedPreferencesMigrationLogger = sharedPreferencesMigrationLogger,
) {
    companion object {
        private const val SHARED_PREF_NAME = "installedPkgPref"
        private const val SHARED_PREF_VERSION = 1

        private val MIGRATION_0_1 = SharedPreferencesMigration.emptyMigration(0, 1)

        private val MIGRATION_LIST = SharedPreferencesMigration.MigrationListBuilder()
            .addMigration(MIGRATION_0_1)
            .build()
    }

    class Migrations @Inject constructor() {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration.emptyMigration(0, 1)
    }

    object Keys {
        const val PREF_INDIVIDUAL_INSTALLED_PKGS_LAST_SYNC_TIMESTAMP = "installed_pkgs_last_sync_time"
    }

    fun clearInstalledPkgsPref(): Completable {
        return rxCompletable { clear() }
            .subscribeOn(Schedulers.io())
    }
}
