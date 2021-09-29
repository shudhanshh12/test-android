package tech.okcredit.bills

import io.reactivex.Observable

interface GetAndUpdateBillSeenFirstTime {
    fun execute(): Observable<Boolean>
}
