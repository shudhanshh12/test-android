package `in`.okcredit.cashback.contract.usecase

import io.reactivex.Completable

interface CashbackLocalDataOperations {
    fun executeInvalidateLocalData(): Completable

    fun executeClearLocalData(): Completable
}
