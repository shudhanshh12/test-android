package `in`.okcredit.frontend.usecase.supplier

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import javax.inject.Inject

/**
 *  This class is used to fetch transaction details of supplier
 */

class GetSupplierTransaction @Inject constructor(
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(req: String) = getActiveBusinessId.get().execute().flatMapObservable { businessId ->
        supplierCreditRepository.get().getTransaction(req, businessId)
    }
}
