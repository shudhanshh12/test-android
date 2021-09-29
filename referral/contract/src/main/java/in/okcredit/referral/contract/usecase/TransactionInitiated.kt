package `in`.okcredit.referral.contract.usecase

import io.reactivex.Completable

interface TransactionInitiated {
    fun execute(): Completable
}
