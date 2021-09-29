package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class TagOnlinePaymentWithCustomer @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<TagOnlinePaymentWithCustomer.Request, Unit> {

    data class Request(val customerId: String, val paymentId: String)

    override fun execute(req: Request): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                collectionRepository.get().tagMerchantPaymentWithCustomer(req.customerId, req.paymentId, businessId)
            }
        )
    }
}
