package `in`.okcredit.merchant.suppliercredit.use_case

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.store.SupplierPreferences
import `in`.okcredit.merchant.suppliercredit.store.SupplierPreferences.Keys.PREF_BUSINESS_SUPPLIER_SCREEN_SORT
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class SetSupplierScreenSortSelection @Inject constructor(
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val preferences: Lazy<SupplierPreferences>,
) {
    suspend fun execute(sortSelection: GetSupplierScreenSortSelection.SupplierScreenSortSelection) {
        val businessId = getActiveBusinessId.get().execute().await()
        preferences.get().set(PREF_BUSINESS_SUPPLIER_SCREEN_SORT, sortSelection.value, Scope.Business(businessId))
    }
}
