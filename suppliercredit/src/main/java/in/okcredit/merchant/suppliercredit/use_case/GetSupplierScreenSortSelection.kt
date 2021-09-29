package `in`.okcredit.merchant.suppliercredit.use_case

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.store.SupplierPreferences
import `in`.okcredit.merchant.suppliercredit.store.SupplierPreferences.Keys.PREF_BUSINESS_SUPPLIER_SCREEN_SORT
import `in`.okcredit.merchant.suppliercredit.use_case.GetSupplierScreenSortSelection.SupplierScreenSortSelection.BILL_DATE
import dagger.Lazy
import io.reactivex.Single
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.rxSingle
import merchant.okcredit.accounting.ui.transaction_sort_options_dialog.TransactionsSortCriteriaSelectionBottomSheet.Companion.BILL_DATE_STRING
import merchant.okcredit.accounting.ui.transaction_sort_options_dialog.TransactionsSortCriteriaSelectionBottomSheet.Companion.CREATE_DATE_STRING
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class GetSupplierScreenSortSelection @Inject constructor(
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val preferences: Lazy<SupplierPreferences>,
) {
    enum class SupplierScreenSortSelection(val value: String) {
        CREATE_DATE(CREATE_DATE_STRING), BILL_DATE(BILL_DATE_STRING);

        companion object {
            fun convertToSortBy(sortBy: String): SupplierScreenSortSelection {
                return when (sortBy) {
                    CREATE_DATE.value -> CREATE_DATE
                    BILL_DATE.value -> BILL_DATE
                    else -> BILL_DATE
                }
            }
        }
    }

    fun execute(): Single<SupplierScreenSortSelection> {
        return getActiveBusinessId.get().execute()
            .flatMap { businessId ->
                rxSingle {
                    val sortByString = preferences.get().getString(
                        key = PREF_BUSINESS_SUPPLIER_SCREEN_SORT,
                        scope = Scope.Business(businessId),
                        defaultValue = BILL_DATE.value
                    ).first()
                    SupplierScreenSortSelection.convertToSortBy(sortByString)
                }
            }
    }
}
