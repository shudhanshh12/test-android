package tech.okcredit.home.usecase.dashboard

import io.reactivex.Observable

interface DashboardValueProvider {
    fun getValue(request: Request?): Observable<Response>

    data class Request(val input: Int)
    abstract class Response(open val exclude: Boolean = false)
}
