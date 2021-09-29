package `in`.okcredit.supplier.statement.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.Supplier.Companion.BLOCKED
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.supplier.contract.GetSupplierAccountNetBalance
import javax.inject.Inject

class GetSupplierBalanceAndCount @Inject constructor(
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetSupplierAccountNetBalance {

    fun execute(): Observable<Response> = getActiveBusinessId.get().execute().flatMapObservable { businessId ->
        executeForBusiness(businessId)
    }

    private fun executeForBusiness(businessId: String) = supplierCreditRepository.get().getSuppliers(businessId)
        .map {
            var balance = 0L
            var count = 0
            it.forEach { supplier ->
                if (supplier.deleted.not() && supplier.state != BLOCKED) {
                    balance += supplier.balance
                    count++
                }
            }
            Response(balance, count)
        }

    data class Response(
        val supplierBalance: Long,
        val supplierCount: Int,
    )

    override fun getNetBalance(businessId: String): Observable<Long> {
        return executeForBusiness(businessId).map { response -> response.supplierBalance }
    }
}
