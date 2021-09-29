package tech.okcredit.sdk.store

import `in`.okcredit.merchant.contract.GetBusinessIdList
import android.content.Context
import dagger.Lazy
import dagger.Reusable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

@Reusable
class BillPreferences @Inject constructor(
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
        const val SHARED_PREF_NAME = "bill_management"

        private const val PREF_BUSINESS_BILL_ADOPTION_TIME = "bill_adoption_time"
    }

    class Migrations @Inject constructor(
        private val getBusinessIdList: Lazy<GetBusinessIdList>,
    ) {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration(0, 1) { prefs ->
            SharedPreferencesMigration.changeKeyListScopeFromIndividualToBusinessScope(
                prefs,
                listOf(PREF_BUSINESS_BILL_ADOPTION_TIME),
                getBusinessIdList.get().execute().first()
            )
        }
    }

    fun setBillAdoptionTime(millis: Long, businessId: String) =
        rxCompletable { set(PREF_BUSINESS_BILL_ADOPTION_TIME, millis, Scope.Business(businessId)) }

    fun getBillAdoptionTime(defaultValue: Long = -1, businessId: String) =
        getLong(PREF_BUSINESS_BILL_ADOPTION_TIME, Scope.Business(businessId), defaultValue).asObservable()
}
