package `in`.okcredit.supplier.home.tab

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import merchant.okcredit.accounting.contract.HomeSortType
import javax.inject.Inject

class SetSupplierSortType @Inject constructor(
    private val repository: Lazy<SupplierCreditRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(type: HomeSortType) = getActiveBusinessId.get().execute().flatMap { businessId ->
        repository.get().setSortType(type, businessId).toSingleDefault(type)
    }
}
