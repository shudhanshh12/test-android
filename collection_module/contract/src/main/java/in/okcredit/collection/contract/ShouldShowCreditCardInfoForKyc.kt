package `in`.okcredit.collection.contract

import io.reactivex.Observable

interface ShouldShowCreditCardInfoForKyc {
    fun execute(): Observable<Boolean>
}
