package `in`.okcredit.cashback.contract.usecase

import io.reactivex.Observable

interface GetCashbackMessage {
    fun execute(): Observable<String>
}
