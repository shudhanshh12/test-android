package `in`.okcredit.supplier.home.tab

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import javax.inject.Inject

class GetSupplierSortType @Inject constructor(
    private val repository: Lazy<SupplierCreditRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute() = getActiveBusinessId.get().execute().flatMapObservable { repository.get().getSortType(it) }
}
