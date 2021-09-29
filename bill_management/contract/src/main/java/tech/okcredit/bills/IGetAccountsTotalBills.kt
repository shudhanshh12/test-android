package tech.okcredit.bills

import io.reactivex.Observable

interface IGetAccountsTotalBills {
    fun execute(accountId: String): Observable<Response>

    data class Response(val totalCount: Int, val unseenCount: Int)
}
