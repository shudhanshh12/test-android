package `in`.okcredit.backend.contract

import io.reactivex.Observable

interface CheckAuth {

    fun execute(): Observable<Boolean>
}
