package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.IsUpiVpaValid
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class IsUpiVpaValidImpl @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : IsUpiVpaValid {
    override fun execute(req: String): Single<Pair<Boolean, String>> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            collectionRepository.validatePaymentAddress("upi", req, businessId)
        }
    }
}
