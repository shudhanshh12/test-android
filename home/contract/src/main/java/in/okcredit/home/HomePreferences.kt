package `in`.okcredit.home

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
class HomePreferences @Inject constructor(
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
        const val SHARED_PREF_NAME = "home"
    }

    object Keys {
        const val PREF_BUSINESS_FILTER_ENABLED = "filter enabled pref"
        const val PREF_BUSINESS_SHOW_QUICK_ADD_CARD = "show_quick_add_card"
        const val PREF_BUSINESS_CUSTOMER_TAB_SORT = "customer_tab_sort"
    }

    class Migrations @Inject constructor(
        private val getBusinessIdList: Lazy<GetBusinessIdList>,
    ) {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration(0, 1) { prefs ->
            SharedPreferencesMigration.changeKeyListScopeFromIndividualToBusinessScope(
                prefs,
                listOf(Keys.PREF_BUSINESS_FILTER_ENABLED, Keys.PREF_BUSINESS_SHOW_QUICK_ADD_CARD),
                getBusinessIdList.get().execute().first()
            )
        }
    }
}
