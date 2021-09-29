package `in`.okcredit.collection.contract

import io.reactivex.Completable

interface UpdateCustomerReferralLedgerSeen {
    fun execute(customerId: String): Completable
}
