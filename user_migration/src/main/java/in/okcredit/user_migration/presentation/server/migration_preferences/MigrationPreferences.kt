package `in`.okcredit.user_migration.presentation.server.migration_preferences

import `in`.okcredit.merchant.contract.GetBusinessIdList
import android.content.Context
import dagger.Lazy
import dagger.Reusable
import kotlinx.coroutines.flow.first
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

@Reusable
class MigrationPreferences @Inject constructor(
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
        private const val SHARED_PREF_NAME = "migration"
    }

    object Keys {
        const val PREF_BUSINESS_PREDICTED_DATA = "pref_predicted_data"
    }

    class Migrations @Inject constructor(
        private val getBusinessIdList: Lazy<GetBusinessIdList>,
    ) {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration(0, 1) { prefs ->
            SharedPreferencesMigration.changeKeyListScopeFromIndividualToBusinessScope(
                prefs,
                listOf(Keys.PREF_BUSINESS_PREDICTED_DATA),
                getBusinessIdList.get().execute().first()
            )
        }
    }
}
