package merchant.okcredit.supplier.contract

import io.reactivex.Observable

interface IsSupplierCollectionEnabled {

    fun execute(): Observable<Boolean>
}
