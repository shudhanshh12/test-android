package `in`.okcredit.collection.contract

import io.reactivex.Observable

interface IsKycCompleted {
    fun execute(): Observable<Boolean>
}

enum class KycStatus(val value: String) {
    NOT_SET("NOT_SET"),
    FAILED("FAILED"),
    COMPLETE("COMPLETE"),
    PENDING("PENDING")
}
