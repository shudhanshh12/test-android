package `in`.okcredit.merchant.contract

import io.reactivex.Completable

interface SyncBusinessData {
    fun execute(): Completable
}
