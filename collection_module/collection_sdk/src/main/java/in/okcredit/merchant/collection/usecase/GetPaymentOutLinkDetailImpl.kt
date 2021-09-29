package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.ApiMessages
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.GetPaymentOutLinkDetail
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class GetPaymentOutLinkDetailImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetPaymentOutLinkDetail {
    override fun execute(
        accountId: String,
        accountType: String
    ): Single<ApiMessages.PaymentOutLinkDetailResponse> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            collectionRepository.get().getPaymentOutLinkDetail(accountId, accountType, businessId)
        }
    }
}
