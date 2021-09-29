package `in`.okcredit.merchant.suppliercredit.store

import `in`.okcredit.merchant.contract.GetBusinessIdList
import android.content.Context
import androidx.core.content.edit
import dagger.Lazy
import dagger.Reusable
import kotlinx.coroutines.flow.first
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

@Reusable
class SupplierPreferences @Inject constructor(
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
        const val SHARED_PREF_NAME = "supplier"
        const val VERSION = 1
    }

    object Keys {
        const val PREF_BUSINESS_LAST_SYNC_EVERYTHING_TIME = "KEY_LAST_SYNC_EVERYTHING_TIME"
        const val PREF_BUSINESS_SORT_TYPE = "sort_type"
        const val PREF_BUSINESS_SUPPLIER_SCREEN_SORT = "supplier_screen_sort"
    }

    class Migrations @Inject constructor(
        private val context: Lazy<Context>,
        private val getBusinessIdList: Lazy<GetBusinessIdList>,
    ) {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration(0, 1) { prefs ->
            val supplierCreditPref = context.get().getSharedPreferences("SupplierCreditPref", Context.MODE_PRIVATE)

            // copy all data from deprecated 'SupplierCreditPref' to SupplierPreferences
            prefs.edit(commit = true) {
                supplierCreditPref.all.forEach { (key, value) ->
                    SharedPreferencesMigration.put(key, value, this)
                }
            }

            // Migrate key scopes
            SharedPreferencesMigration.changeKeyListScopeFromIndividualToBusinessScope(
                prefs,
                listOf(Keys.PREF_BUSINESS_LAST_SYNC_EVERYTHING_TIME, Keys.PREF_BUSINESS_SORT_TYPE),
                getBusinessIdList.get().execute().first()
            )
        }
    }
}
