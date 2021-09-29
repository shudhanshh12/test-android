package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetAllDueCustomers @Inject constructor(
    private val customerRepo: CustomerRepo,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    UseCase<GetAllDueCustomers.Request, List<Customer>> {

    override fun execute(req: Request): Observable<Result<List<Customer>>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                customerRepo.listCustomers(businessId).flatMap { it ->
                    var dueCustomers: List<Customer> = it.filter { it.canSendCollectionLink() }

                    if (req.searchQuery.isNullOrEmpty()) {
                        Observable.just(dueCustomers)
                    } else {
                        dueCustomers =
                            dueCustomers.filter {
                                it.description.toLowerCase().replace(" ", "").contains(req.searchQuery)
                            }
                        Observable.just(dueCustomers)
                    }
                }
            }
        )
    }

    data class Request(
        val searchQuery: String?,
    )
}
