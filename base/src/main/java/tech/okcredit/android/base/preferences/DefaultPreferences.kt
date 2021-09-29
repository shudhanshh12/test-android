package tech.okcredit.android.base.preferences

import `in`.okcredit.shared.service.keyval.KeyValMigrator
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.Lazy
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * [DefaultPreferences] is the default (without an explicit name) shared preference of OkCredit app.
 */

@Reusable
class DefaultPreferences @Inject constructor(
    context: Context,
    sharedPreferencesMigrationLogger: Lazy<SharedPreferencesMigrationHandler.Logger>,
    migrations: Lazy<Migrations>,
) : OkcSharedPreferences(
    context = context,
    version = SHARED_PREF_VERSION,
    migrationList = migrations.get().getList(),
    sharedPreferencesMigrationLogger = sharedPreferencesMigrationLogger,
) {
    companion object {
        private const val SHARED_PREF_VERSION = 1
    }

    object Keys {
        const val PREF_BUSINESS_LAST_TRANSACTION_SYNC_TIME = "last_transaction_sync_time"
        const val PREF_BUSINESS_SHOULD_SHOW_AUTO_DUE_DATE = "should_show_auto_due_date"
        const val PREF_BUSINESS_IS_COLLECTION_DATE_SHOWN = "key_is_collection_date_shown"
        const val PREF_BUSINESS_CORE_SDK_ENABLED = "core_sdk_enabled"
        const val PREF_INDIVIDUAL_APP_LOCK_SYNCED = "KEY_APP_LOCK_SYNCED"

        // Legacy app lock keys
        const val PREF_INDIVIDUAL_APP_LOCK_PATTERN = "app_lock_pattern"
        const val PREF_INDIVIDUAL_APP_LOCK_STATUS = "app_lock_status"

        // KeyVal
        const val PREF_BUSINESS_SC_ENABLED_CUSTOMERS: String = "key_sc_enabled_customer_ids"
        const val PREF_BUSINESS_TXN_RESTRICTED_CUSTOMERS = "key_txn_restricted_customer_ids"
        const val PREF_BUSINESS_ADD_TXN_RESTRICTED_CUSTOMERS = "key_add_txn_restricted_customer_ids"
        const val PREF_BUSINESS_KEY_SMS_TOGGLE_OFF = "customer_profile.key_sms_toggle.off"
        const val PREF_BUSINESS_KEY_SMS_TOGGLE_ON = "customer_profile.key_sms_toggle.on"
        const val PREF_BUSINESS_IS_FIRST_TIME_SALE = "key_is_first_time_sale"
        const val PREF_INDIVIDUAL_DEVICE = "device_v2"
        const val PREF_INDIVIDUAL_DEPRECATED_DEVICE = "device"
        const val PREF_INDIVIDUAL_IP_REGION = "ip_region"
        const val PREF_INDIVIDUAL_IS_FORCE_SYNC_ONCE = "Is_Force_Transaction_Sync_Completed"
        const val PREF_INDIVIDUAL_KEY_SERVER_VERSION = "notification.server_version"
    }

    class Migrations @Inject constructor(
        private val context: Lazy<Context>,
        private val getBusinessIdList: Lazy<GetBusinessIdListForDefaultPreferencesMigration>,
        private val keyValMigrator: Lazy<KeyValMigrator>,
    ) {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration(0, 1) { prefs ->
            val businessIdList = getBusinessIdList.get().execute().first()
            migrateKeysFromUserToBusinessScope(prefs, businessIdList)
            migrateLegacyAppLockKeys(context.get(), prefs)
            migrateKeyValServiceDataToDefaultPreferences(prefs, businessIdList)
            migrateShouldShowAutoDueDateToBusinessScope(prefs, businessIdList)
        }

        private fun migrateLegacyAppLockKeys(context: Context, prefs: SharedPreferences) {
            val defaultPreferences = context.getSharedPreferences("in.okcredit.default", Context.MODE_PRIVATE)
            prefs.edit(commit = true) {
                putString(
                    Keys.PREF_INDIVIDUAL_APP_LOCK_PATTERN,
                    defaultPreferences.getString(Keys.PREF_INDIVIDUAL_APP_LOCK_PATTERN, "")
                )
                putBoolean(
                    Keys.PREF_INDIVIDUAL_APP_LOCK_STATUS,
                    defaultPreferences.getBoolean(Keys.PREF_INDIVIDUAL_APP_LOCK_STATUS, false)
                )
            }
        }

        private fun migrateKeysFromUserToBusinessScope(
            prefs: SharedPreferences,
            businessIdList: List<String>,
        ) {
            SharedPreferencesMigration.changeKeyListScopeFromIndividualToBusinessScope(
                prefs,
                listOf(
                    Keys.PREF_BUSINESS_CORE_SDK_ENABLED,
                    Keys.PREF_BUSINESS_LAST_TRANSACTION_SYNC_TIME,
                    Keys.PREF_BUSINESS_IS_COLLECTION_DATE_SHOWN
                ),
                businessIdList
            )
        }

        // PREF_BUSINESS_SHOULD_SHOW_AUTO_DUE_DATE requires special handling because it is a dynamic key
        // PREF_BUSINESS_SHOULD_SHOW_AUTO_DUE_DATE key = (should_show_auto_due_date + customer id)
        private suspend fun migrateShouldShowAutoDueDateToBusinessScope(
            prefs: SharedPreferences,
            businessIdList: List<String>,
        ) {
            prefs.edit(commit = true) {
                prefs.all.forEach { (key, value) ->
                    if (
                        key.contains(Keys.PREF_BUSINESS_SHOULD_SHOW_AUTO_DUE_DATE).not() ||
                        key.contains(Scope.DIVIDER)
                    ) {
                        return@forEach
                    }

                    businessIdList.forEach { businessId ->
                        val businessScopedKey = SharedPreferencesMigration.convertToBusinessScopedKey(key, businessId)
                        SharedPreferencesMigration.put(businessScopedKey, value, this)
                    }
                    remove(key)
                }
            }
        }

        private fun migrateKeyValServiceDataToDefaultPreferences(
            prefs: SharedPreferences,
            businessIdList: List<String>,
        ) {
            keyValMigrator.get().migrateDataToSharedPrefs(prefs, businessIdList)
        }
    }

    interface GetBusinessIdListForDefaultPreferencesMigration {
        fun execute(): Flow<List<String>>
    }
}
