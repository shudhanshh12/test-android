package tech.okcredit.home.usecase.pre_network_onboarding

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class HideBigButtonAndNudge @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val supplierRepo: Lazy<SupplierCreditRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(): Observable<Boolean> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            Observable.combineLatest(
                customerCountGreaterThanZero(businessId),
                supplierCountGreaterThanZero(businessId)
            ) { customerCount, supplierCount ->
                customerCount || supplierCount
            }
        }
    }

    private fun customerCountGreaterThanZero(businessId: String) = customerRepo.get().getCustomersCount(businessId)
        .map { it > 0 }

    private fun supplierCountGreaterThanZero(businessId: String) = supplierRepo.get().getSuppliersCount(businessId)
        .map { it > 0 }
}
