package `in`.okcredit.backend.contract

import io.reactivex.Completable

interface SyncCustomers {
    fun execute(businessId: String? = null): Completable
}
