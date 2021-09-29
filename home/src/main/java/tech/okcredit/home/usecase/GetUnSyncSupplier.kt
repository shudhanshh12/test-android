package tech.okcredit.home.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetUnSyncSupplier @Inject constructor(
    private val supplierCreditRepository: SupplierCreditRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(): Observable<List<String>> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            supplierCreditRepository.listDirtyTransactions(businessId)
                .map {
                    return@map it.map {
                        it.supplierId
                    }
                }
        }
    }
}
