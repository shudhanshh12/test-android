package `in`.okcredit.merchant.suppliercredit.use_case

import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.merchant.suppliercredit.store.SupplierPreferences
import dagger.Lazy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class ClearLastSyncEverythingTimeForAllBusinesses @Inject constructor(
    private val preferences: Lazy<SupplierPreferences>,
    private val getBusinessIdList: Lazy<GetBusinessIdList>,
) {
    fun execute() = rxCompletable {
        getBusinessIdList.get().execute().first().forEach { businessId ->
            preferences.get()
                .remove(SupplierPreferences.Keys.PREF_BUSINESS_LAST_SYNC_EVERYTHING_TIME, Scope.Business(businessId))
        }
    }
}
