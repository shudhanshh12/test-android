package `in`.okcredit.merchant.contract

import io.reactivex.Observable

interface IsMultipleAccountEnabled {
    fun execute(): Observable<Boolean>
}
