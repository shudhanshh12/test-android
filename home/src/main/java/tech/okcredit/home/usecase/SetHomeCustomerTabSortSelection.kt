package tech.okcredit.home.usecase

import `in`.okcredit.home.HomePreferences
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class SetHomeCustomerTabSortSelection @Inject constructor(
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val homePreferences: Lazy<HomePreferences>,
) {
    suspend fun execute(sortBy: String) {
        val businessId = getActiveBusinessId.get().execute().await()
        homePreferences.get()
            .set(HomePreferences.Keys.PREF_BUSINESS_CUSTOMER_TAB_SORT, sortBy, Scope.Business(businessId))
    }
}
