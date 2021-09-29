package `in`.okcredit.cashback.contract.usecase

import io.reactivex.Observable

interface IsCustomerCashbackFeatureEnabled {
    fun execute(): Observable<Boolean>
}
