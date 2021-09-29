package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetNewOnlinePayments @Inject constructor(
    val collectionRepository: CollectionRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<Unit, List<CollectionOnlinePayment>> {
    override fun execute(req: Unit): Observable<Result<List<CollectionOnlinePayment>>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                collectionRepository.listOfNewOnlinePayments(businessId)
            }
        )
    }
}
