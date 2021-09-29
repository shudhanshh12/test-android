package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.data.local.CustomerUiPreferences
import dagger.Lazy
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle
import merchant.okcredit.accounting.ui.transaction_sort_options_dialog.TransactionsSortCriteriaSelectionBottomSheet.Companion.BILL_DATE_STRING
import merchant.okcredit.accounting.ui.transaction_sort_options_dialog.TransactionsSortCriteriaSelectionBottomSheet.Companion.CREATE_DATE_STRING
import javax.inject.Inject

class GetCustomerScreenSortSelection @Inject constructor(
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val preferences: Lazy<CustomerUiPreferences>,
) {
    enum class CustomerScreenSortSelection(val value: String) {
        CREATE_DATE(CREATE_DATE_STRING), BILL_DATE(BILL_DATE_STRING);

        companion object {
            fun convertToSortBy(sortBy: String): CustomerScreenSortSelection {
                return when (sortBy) {
                    CREATE_DATE.value -> CREATE_DATE
                    BILL_DATE.value -> BILL_DATE
                    else -> BILL_DATE
                }
            }
        }
    }

    fun execute(): Single<CustomerScreenSortSelection> {
        return getActiveBusinessId.get().execute()
            .flatMap { businessId ->
                rxSingle { preferences.get().getCustomerScreenSortSelection(businessId) }
            }
    }
}
