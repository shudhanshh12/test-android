package `in`.okcredit.individual.data.local

import `in`.okcredit.merchant.contract.IndividualPreferencesMigration
import android.content.Context
import dagger.Lazy
import kotlinx.coroutines.flow.Flow
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

class IndividualPreferences @Inject constructor(
    context: Context,
    sharedPreferencesMigrationLogger: Lazy<SharedPreferencesMigrationHandler.Logger>,
    userPreferencesMigrations: Migrations,
) : OkcSharedPreferences(
    context = context,
    prefName = SHARED_PREF_NAME,
    version = SHARED_PREF_VERSION,
    migrationList = userPreferencesMigrations.getList(),
    sharedPreferencesMigrationLogger = sharedPreferencesMigrationLogger,
) {
    companion object {
        private const val SHARED_PREF_NAME = "individual"
        private const val SHARED_PREF_VERSION = 1
    }

    class Migrations @Inject constructor(
        private val individualPreferencesMigration: IndividualPreferencesMigration,
    ) {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = individualPreferencesMigration.migration0To1()
    }

    fun getString(key: String, defaultValue: String): Flow<String> {
        return getString(key, Scope.Individual, defaultValue)
    }

    suspend fun setString(key: String, value: String) {
        return set(key, value, Scope.Individual)
    }

    suspend fun isPreferenceAvailable(key: String) = contains(key, Scope.Individual)
}
