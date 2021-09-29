package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class DoDueCustomersExists @Inject constructor(
    private val customerRepo: CustomerRepo,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(): Observable<Boolean> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            customerRepo.listCustomers(businessId).flatMap { it ->
                val dueCustomers = it.filter {
                    it.canSendCollectionLink()
                }
                return@flatMap Observable.just(dueCustomers.isNotEmpty())
            }
        }
    }
}
