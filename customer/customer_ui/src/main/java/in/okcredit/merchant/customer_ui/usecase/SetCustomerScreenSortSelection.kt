package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.data.local.CustomerUiPreferences
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class SetCustomerScreenSortSelection @Inject constructor(
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val preferences: Lazy<CustomerUiPreferences>,
) {
    suspend fun execute(customerScreenSortSelection: GetCustomerScreenSortSelection.CustomerScreenSortSelection) {
        val businessId = getActiveBusinessId.get().execute().await()
        preferences.get().setCustomerScreenSortSelection(customerScreenSortSelection, businessId)
    }
}
