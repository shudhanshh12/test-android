package tech.okcredit.contacts.store.preference

import android.content.Context
import dagger.Lazy
import dagger.Reusable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import tech.okcredit.contacts.store.preference.ContactPreference.Keys.PREF_INDIVIDUAL_LAST_ID
import tech.okcredit.contacts.store.preference.ContactPreference.Keys.PREF_INDIVIDUAL_START_TIME
import javax.inject.Inject

@Reusable
class ContactPreference @Inject constructor(
    context: Context,
    private val dispatcherProvider: Lazy<DispatcherProvider>,
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
        private const val SHARED_PREF_NAME = "contact"
        private const val SHARED_PREF_VERSION = 2
    }

    class Migrations @Inject constructor() {
        fun getList() = listOf(migration0To1(), migration1To2())

        private fun migration0To1() = SharedPreferencesMigration.emptyMigration(0, 1)

        private fun migration1To2() = SharedPreferencesMigration(1, 2) { prefs ->
            prefs.edit().remove(PREF_INDIVIDUAL_START_TIME).commit()
            prefs.edit().remove(PREF_INDIVIDUAL_LAST_ID).commit()
        }
    }

    object Keys {
        const val PREF_INDIVIDUAL_ADD_OKCREDIT_CONTACT_DISPLAYED = "okcredit_contact_inapp"
        const val PREF_INDIVIDUAL_START_TIME = "start_time"
        const val PREF_INDIVIDUAL_LAST_ID = "last_id"
    }

    suspend fun canShowContactInApp(): Boolean {
        return getBoolean(Keys.PREF_INDIVIDUAL_ADD_OKCREDIT_CONTACT_DISPLAYED, Scope.Individual).first()
    }

    suspend fun setContactInappDisplayed(showed: Boolean) {
        set(Keys.PREF_INDIVIDUAL_ADD_OKCREDIT_CONTACT_DISPLAYED, showed, Scope.Individual)
    }

    suspend fun setStartTime(startTime: Long) = withContext(dispatcherProvider.get().io()) {
        set(Keys.PREF_INDIVIDUAL_START_TIME, startTime, Scope.Individual)
    }

    suspend fun getStartTime(): Long = withContext(dispatcherProvider.get().io()) {
        return@withContext getLong(Keys.PREF_INDIVIDUAL_START_TIME, Scope.Individual).first()
    }

    suspend fun setLastId(lastId: String) = withContext(dispatcherProvider.get().io()) {
        set(Keys.PREF_INDIVIDUAL_LAST_ID, lastId, Scope.Individual)
    }

    suspend fun getLastId(): String = withContext(dispatcherProvider.get().io()) {
        return@withContext getString(Keys.PREF_INDIVIDUAL_LAST_ID, Scope.Individual).first()
    }
}
