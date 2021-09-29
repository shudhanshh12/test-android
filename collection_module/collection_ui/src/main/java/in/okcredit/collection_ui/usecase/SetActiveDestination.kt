package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.ApiMessages
import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class SetActiveDestination @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(
        collectionMerchantProfile: CollectionMerchantProfile,
        async: Boolean,
        referralMerchant: String,
    ): Observable<Result<ApiMessages.MerchantCollectionProfileResponse>> {
        return UseCase.wrapSingle(
            getActiveBusinessId.get().execute().flatMap { businessId ->
                collectionRepository.setActiveDestination(
                    collectionMerchantProfile,
                    async,
                    referralMerchant,
                    businessId
                )
            }
        )
    }
}
