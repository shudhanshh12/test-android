package `in`.okcredit.cashback.contract.usecase

import io.reactivex.Observable

interface IsSupplierCashbackFeatureEnabled {
    fun execute(): Observable<Boolean>
}
