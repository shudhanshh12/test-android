package `in`.okcredit.business_health_dashboard.datasource.local

import android.content.Context
import dagger.Lazy
import dagger.Reusable
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

@Reusable
class BusinessHealthDashboardPreferences @Inject constructor(
    context: Context,
    sharedPreferencesMigrationLogger: Lazy<SharedPreferencesMigrationHandler.Logger>,
    migrations: Lazy<Migrations>,
) : OkcSharedPreferences(
    context = context,
    prefName = SHARED_PREF_NAME,
    version = VERSION,
    migrationList = migrations.get().getList(),
    sharedPreferencesMigrationLogger = sharedPreferencesMigrationLogger,
) {
    companion object {
        const val VERSION = 1
        const val SHARED_PREF_NAME = "business_health_dashboard"

        private const val KEY_BUSINESS_HEALTH_DASHBOARD_DATA = "dashboard_data"
        private const val KEY_USER_PREFERRED_TIME_CADENCE = "user_preferred_time_cadence"

        private const val INVALIDATED_BUSINESS_HEALTH_DASHBOARD_DATA = ""
        private const val INVALIDATED_USER_PREFERRED_TIME_CADENCE = ""
    }

    class Migrations @Inject constructor() {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration.emptyMigration(0, 1)
    }

    fun setBusinessHealthDashboardData(value: String, businessId: String) =
        rxCompletable { set(KEY_BUSINESS_HEALTH_DASHBOARD_DATA, value, Scope.Business(businessId)) }

    fun getBusinessHealthDashboardData(
        businessId: String,
        defaultValue: String = INVALIDATED_BUSINESS_HEALTH_DASHBOARD_DATA,
    ) =
        getString(KEY_BUSINESS_HEALTH_DASHBOARD_DATA, Scope.Business(businessId), defaultValue).asObservable()

    fun setUserPreferredTimeCadence(value: String, businessId: String) =
        rxCompletable { set(KEY_USER_PREFERRED_TIME_CADENCE, value, Scope.Business(businessId)) }

    fun getUserPreferredTimeCadence(
        businessId: String,
        defaultValue: String = INVALIDATED_USER_PREFERRED_TIME_CADENCE,
    ) =
        getString(KEY_USER_PREFERRED_TIME_CADENCE, Scope.Business(businessId), defaultValue).asObservable()
}
