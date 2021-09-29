package `in`.okcredit.merchant.suppliercredit

import io.reactivex.Observable

interface GetSupplier {
    fun executeObservable(req: String): Observable<Supplier>
}
