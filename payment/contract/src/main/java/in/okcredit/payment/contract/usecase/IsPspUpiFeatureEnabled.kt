package `in`.okcredit.payment.contract.usecase

import io.reactivex.Observable

interface IsPspUpiFeatureEnabled {
    fun execute(): Observable<Boolean>
}
