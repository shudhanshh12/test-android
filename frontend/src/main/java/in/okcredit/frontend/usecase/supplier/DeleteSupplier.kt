package `in`.okcredit.frontend.usecase.supplier

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class DeleteSupplier @Inject constructor(
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(supplierId: String): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            supplierCreditRepository.get().deleteSupplier(supplierId, businessId)
        }
    }
}
