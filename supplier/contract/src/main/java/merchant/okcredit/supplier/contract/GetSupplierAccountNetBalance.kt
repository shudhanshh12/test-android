package merchant.okcredit.supplier.contract

import io.reactivex.Observable

interface GetSupplierAccountNetBalance {
    fun getNetBalance(businessId: String): Observable<Long>
}
