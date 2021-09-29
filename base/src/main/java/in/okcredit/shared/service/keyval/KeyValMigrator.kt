package `in`.okcredit.shared.service.keyval

import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.Lazy
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys
import tech.okcredit.android.base.preferences.SharedPreferencesMigration.Companion.convertToBusinessScopedKey
import tech.okcredit.android.base.preferences.SharedPreferencesMigration.Companion.put
import javax.inject.Inject

/**
 * Usecase for migrating data from deprecated KeyValService to DefaultPreferences
 */
class KeyValMigrator @Inject constructor(
    private val keyValDao: Lazy<KeyValDao>,
) {

    fun migrateDataToSharedPrefs(prefs: SharedPreferences, businessIdList: List<String>) {
        prefs.edit(commit = true) {
            keyValDao.get().getAllData().forEach { entry ->
                if (shouldMigrateToBusinessScope(entry.key)) { // Business scope
                    businessIdList.forEach { businessId ->
                        val businessScopedKey = convertToBusinessScopedKey(entry.key, businessId)
                        put(businessScopedKey, entry.value, this)
                    }
                } else { // Individual scope
                    put(entry.key, entry.value, this)
                }
            }
        }
    }

    private fun shouldMigrateToBusinessScope(key: String): Boolean {
        return listOf(
            Keys.PREF_BUSINESS_SC_ENABLED_CUSTOMERS,
            Keys.PREF_BUSINESS_TXN_RESTRICTED_CUSTOMERS,
            Keys.PREF_BUSINESS_ADD_TXN_RESTRICTED_CUSTOMERS,
            Keys.PREF_BUSINESS_KEY_SMS_TOGGLE_OFF,
            Keys.PREF_BUSINESS_KEY_SMS_TOGGLE_ON,
            Keys.PREF_BUSINESS_IS_FIRST_TIME_SALE
        ).contains(key)
    }
}
