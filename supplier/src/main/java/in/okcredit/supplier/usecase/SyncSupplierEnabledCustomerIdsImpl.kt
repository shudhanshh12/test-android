package `in`.okcredit.supplier.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import io.reactivex.Completable
import merchant.okcredit.supplier.contract.SyncSupplierEnabledCustomerIds
import javax.inject.Inject

class SyncSupplierEnabledCustomerIdsImpl @Inject constructor(
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : SyncSupplierEnabledCustomerIds {
    override fun execute(): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            supplierCreditRepository.get().syncSupplierEnabledCustomerIds(businessId)
        }
    }
}
