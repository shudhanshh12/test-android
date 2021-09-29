package `in`.okcredit.backend.contract

import io.reactivex.Single

interface GetTotalTxnCount {
    fun execute(): Single<Int>
}
