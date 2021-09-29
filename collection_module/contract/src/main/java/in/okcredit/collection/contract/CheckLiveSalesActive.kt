package `in`.okcredit.collection.contract

import io.reactivex.Observable

interface CheckLiveSalesActive {
    fun execute(): Observable<Boolean>
}
