package `in`.okcredit.shared.usecase

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyKey.REASON
import `in`.okcredit.analytics.PropertyValue.STACKTRACE
import dagger.Lazy
import tech.okcredit.android.base.extensions.itOrBlank
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

class MigrationEventLogger @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>,
) : SharedPreferencesMigrationHandler.Logger {

    companion object {
        private const val EVENT_SP_MIGRATION_SUCCESS = "SharedPreferencesMigration:Success"
        private const val EVENT_SP_MIGRATION_ERROR = "SharedPreferencesMigration:Error"
        private const val KEY_PREF_NAME = "PrefName"
        private const val KEY_FROM_VERSION = "FromVersion"
        private const val KEY_TO_VERSION = "ToVersion"
        private const val KEY_TABLE_NAME = "TableName"
        private const val KEY_DB_PATH_NAME = "DatabasePath"
        private const val EVENT_DB_MIGRATION_ERROR = "DatabaseMigration:Error"
    }

    override fun logSharedPreferencesMigrationSuccess(prefName: String?, fromVersion: Int, toVersion: Int) {
        val properties = mapOf(
            KEY_PREF_NAME to (prefName ?: "Default"),
            KEY_FROM_VERSION to fromVersion,
            KEY_TO_VERSION to toVersion,
        )
        analyticsProvider.get().trackEngineeringMetricEvents(EVENT_SP_MIGRATION_SUCCESS, properties)
    }

    override fun logSharedPreferencesMigrationError(prefName: String?, exception: Exception) {
        val properties = mapOf(
            KEY_PREF_NAME to (prefName ?: "Default"),
            REASON to exception.message.itOrBlank(),
            STACKTRACE to exception.stackTraceToString(),
        )
        analyticsProvider.get().trackEngineeringMetricEvents(EVENT_SP_MIGRATION_ERROR, properties)
    }

    fun logDatabaseTableMigrationError(tableName: String, databasePath: String, exception: Exception) {
        val properties = mapOf(
            KEY_TABLE_NAME to (tableName),
            KEY_DB_PATH_NAME to (databasePath),
            REASON to exception.message.itOrBlank(),
            STACKTRACE to exception.stackTraceToString(),
        )
        analyticsProvider.get().trackEngineeringMetricEvents(EVENT_DB_MIGRATION_ERROR, properties)
    }
}
