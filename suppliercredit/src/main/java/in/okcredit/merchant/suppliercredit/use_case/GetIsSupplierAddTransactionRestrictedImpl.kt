package `in`.okcredit.merchant.suppliercredit.use_case

import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import merchant.okcredit.suppliercredit.contract.GetIsSupplierAddTransactionRestricted
import javax.inject.Inject

class GetIsSupplierAddTransactionRestrictedImpl @Inject constructor(
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
) : GetIsSupplierAddTransactionRestricted {

    override suspend fun execute(businessId: String, supplierId: String): Boolean {
        return supplierCreditRepository.get().getIsAddTransactionRestricted(businessId, supplierId) ||
            supplierCreditRepository.get().getIsBlocked(businessId, supplierId)
    }
}
