package `in`.okcredit.accounting_core.contract

import io.reactivex.Completable
import io.reactivex.Single

interface SuggestedCustomerIdsForAddTransaction {
    fun getSuggestionsFromStore(businessId: String): Single<List<String>>
    fun getSuggestionsFromServer(businessId: String): Single<List<String>>
    fun replaceSuggestedCustomerIdsForAddTransaction(ids: List<String>, businessId: String): Completable
}
