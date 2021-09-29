package `in`.okcredit.payment.contract.usecase

import io.reactivex.Completable

interface ClearPaymentEditAmountLocalData {
    fun execute(): Completable
}
