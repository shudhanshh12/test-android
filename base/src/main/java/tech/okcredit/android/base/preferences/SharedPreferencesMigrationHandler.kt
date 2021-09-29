package tech.okcredit.android.base.preferences

import android.content.SharedPreferences
import dagger.Lazy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.utils.debug
import tech.okcredit.android.base.utils.release
import timber.log.Timber

/**
 * Usecase for handling shared preferences migrations implemented using [OkcSharedPreferences]
 */
class SharedPreferencesMigrationHandler(
    private val version: Int,
    private val prefName: String?,
    private val sharedPreference: SharedPreferences,
    private val migrationList: List<SharedPreferencesMigration>,
    private val sharedPreferencesMigrationLogger: Lazy<Logger>,
    private val coroutineDispatcher: CoroutineDispatcher,
) {
    companion object {
        internal const val PREF_VERSION = "${Scope.DIVIDER}version"

        // lock is used with synchronized {} to prevent multiple thread running migrations simultaneously
        private val lock = Any()
    }

    internal fun checkForMigrations() {
        if (getCurrentVersion() == version) return
        synchronized(lock) {
            val currentVersion = getCurrentVersion()
            if (currentVersion == version) return // double check

            try {
                if (currentVersion > version) { // downgrade
                    throw IllegalStateException("Version downgrade ($currentVersion -> $version) is not supported")
                }

                // upgrade one version at a time (1 -> 2, 2 -> 3, and so on)
                for (fromVersion in currentVersion until version) {
                    val toVersion = fromVersion + 1
                    runBlocking { performMigration(fromVersion, toVersion) }
                }
            } catch (exception: Exception) {
                sharedPreferencesMigrationLogger.get().logSharedPreferencesMigrationError(prefName, exception)
                release { sharedPreference.edit().putInt(PREF_VERSION, version).commit() }
                debug { throw exception } // relay exception only in debug app
            }
        }
    }

    private suspend fun performMigration(fromVersion: Int, toVersion: Int) =
        withContext(coroutineDispatcher) {
            val migration = findMigrationPath(fromVersion, toVersion)
            migration.migrate.invoke(sharedPreference)
            sharedPreference.edit().putInt(PREF_VERSION, toVersion).commit()
            logMigrationSuccess(prefName, fromVersion, toVersion)
        }

    private fun getCurrentVersion() = sharedPreference.getInt(PREF_VERSION, 0)

    private fun findMigrationPath(fromVersion: Int, toVersion: Int): SharedPreferencesMigration {
        migrationList.forEach { migration ->
            if (migration.fromVersion == fromVersion && migration.toVersion == toVersion)
                return migration
        }
        throw IllegalStateException("No migration provided for upgrading from version $fromVersion to $toVersion")
    }

    private fun logMigrationSuccess(prefName: String?, fromVersion: Int, toVersion: Int) {
        Timber.i("OkcSharedPreferences > ${prefName ?: "Default"} : $fromVersion -> $toVersion migration successful")
        sharedPreferencesMigrationLogger.get().logSharedPreferencesMigrationSuccess(prefName, fromVersion, toVersion)
    }

    interface Logger {
        fun logSharedPreferencesMigrationSuccess(prefName: String?, fromVersion: Int, toVersion: Int)
        fun logSharedPreferencesMigrationError(prefName: String?, exception: Exception)
    }
}
