package tech.okcredit.android.base.preferences

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.room.migration.Migration
import tech.okcredit.android.base.preferences.Scope.Companion.DIVIDER
import tech.okcredit.android.base.preferences.Scope.Companion.getScopedKey

/**
 * Migration class to be used with [OkcSharedPreferences]
 *
 * Note: The working of SharedPreferencesMigration is similar to that of Room database's [Migration] class.
 */
class SharedPreferencesMigration(
    val fromVersion: Int,
    val toVersion: Int,
    val migrate: suspend (sharedPreference: SharedPreferences) -> Unit,
) {
    companion object {
        internal fun convertToIndividualScopedKey(businessScopedKey: String): String {
            if (isBusinessScopedKey(businessScopedKey).not()) {
                throw IllegalArgumentException("Cannot convert $businessScopedKey to individual scoped key")
            }

            return businessScopedKey.split(DIVIDER)[1]
        }

        internal fun convertToBusinessScopedKey(individualScopedKey: String, businessId: String): String {
            if (isIndividualScopedKey(individualScopedKey).not()) {
                throw IllegalArgumentException("Cannot convert $individualScopedKey to business scoped key")
            }
            return getScopedKey(individualScopedKey, Scope.Business(businessId))
        }

        private fun isIndividualScopedKey(key: String) = key.contains(DIVIDER).not()

        private fun isBusinessScopedKey(key: String) = key.contains(DIVIDER)

        fun changeKeyListScopeFromIndividualToBusinessScope(
            prefs: SharedPreferences,
            individualScopedKeys: List<String>,
            businessIdList: List<String>,
        ) {
            prefs.apply {
                edit(commit = true) {
                    prefs.all.forEach { (key, value) ->
                        if (individualScopedKeys.contains(key).not()) {
                            return@forEach
                        }

                        if (isIndividualScopedKey(key).not()) {
                            throw IllegalArgumentException("Incorrect individual scoped key: $key")
                        }

                        businessIdList.forEach { businessId ->
                            val businessScopedKey = convertToBusinessScopedKey(key, businessId)
                            put(businessScopedKey, value, this)
                        }
                        remove(key) // remove individual scoped key
                    }
                }
            }
        }

        fun put(key: String, value: Any?, prefsEditor: SharedPreferences.Editor) {
            when (value) { // need to call put method according to data type of value
                is Int -> prefsEditor.putInt(key, value)
                is Long -> prefsEditor.putLong(key, value)
                is String -> prefsEditor.putString(key, value)
                is Float -> prefsEditor.putFloat(key, value)
                is Boolean -> prefsEditor.putBoolean(key, value)
                else -> {
                    val klass = value?.javaClass?.simpleName
                    throw IllegalArgumentException("Unsupported class datatype: $klass with key: $key")
                }
            }
        }

        // Can be used for initial 0 -> 1 version migration
        fun emptyMigration(fromVersion: Int, toVersion: Int): SharedPreferencesMigration {
            return SharedPreferencesMigration(fromVersion, toVersion) { }
        }
    }

    class MigrationListBuilder {
        private val migrationList: ArrayList<SharedPreferencesMigration> = arrayListOf()

        fun addMigration(migration: SharedPreferencesMigration): MigrationListBuilder {
            migrationList.add(migration)
            return this
        }

        fun build() = migrationList
    }
}
