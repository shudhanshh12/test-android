package `in`.okcredit.merchant.usecase

import `in`.okcredit.backend.contract.GetCustomerAccountNetBalance
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.supplier.contract.GetSupplierAccountNetBalance
import javax.inject.Inject

class GetNetBalanceForBusiness @Inject constructor(
    private val getCustomerAccountNetBalance: Lazy<GetCustomerAccountNetBalance>,
    private val getSupplierAccountNetBalance: Lazy<GetSupplierAccountNetBalance>,
) {
    fun execute(businessId: String): Observable<Long> {
        return Observable.combineLatest(
            getCustomerAccountNetBalance.get().getNetBalance(businessId),
            getSupplierAccountNetBalance.get().getNetBalance(businessId),
            { customerNetBalance, supplierNetBalance ->
                customerNetBalance + supplierNetBalance
            }
        )
    }
}
