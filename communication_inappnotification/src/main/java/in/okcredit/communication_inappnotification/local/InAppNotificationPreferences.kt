package `in`.okcredit.communication_inappnotification.local

import `in`.okcredit.merchant.contract.GetBusinessIdList
import android.content.Context
import dagger.Lazy
import dagger.Reusable
import kotlinx.coroutines.flow.first
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigration.Companion.changeKeyListScopeFromIndividualToBusinessScope
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

@Reusable
class InAppNotificationPreferences @Inject constructor(
    context: Lazy<Context>,
    sharedPreferencesMigrationLogger: Lazy<SharedPreferencesMigrationHandler.Logger>,
    migrations: Lazy<Migrations>,
) : OkcSharedPreferences(
    context = context.get(),
    prefName = SHARED_PREF_NAME,
    version = VERSION,
    migrationList = migrations.get().getList(),
    sharedPreferencesMigrationLogger = sharedPreferencesMigrationLogger,
) {
    companion object {
        const val VERSION = 1
        private const val SHARED_PREF_NAME = "in-app-notification"
    }

    object Keys {
        const val PREF_BUSINESS_DATE = "date"
        const val PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT = "notifications_shown_for_date_count"
    }

    class Migrations @Inject constructor(
        private val getBusinessIdList: Lazy<GetBusinessIdList>,
    ) {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration(0, 1) { prefs ->
            changeKeyListScopeFromIndividualToBusinessScope(
                prefs,
                listOf(Keys.PREF_BUSINESS_DATE, Keys.PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT),
                getBusinessIdList.get().execute().first()
            )
        }
    }
}
