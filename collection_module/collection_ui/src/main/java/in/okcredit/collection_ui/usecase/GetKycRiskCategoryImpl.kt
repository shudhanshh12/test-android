package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.GetKycRiskCategory
import `in`.okcredit.collection.contract.KycRisk
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetKycRiskCategoryImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetKycRiskCategory {

    private val defaultRisk = KycRisk(KycRiskCategory.NO_RISK, false, CollectionMerchantProfile.DAILY)

    override fun execute(shouldFetchWhenCollectionNotAdopted: Boolean): Observable<KycRisk> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            collectionRepository.get().getCollectionMerchantProfile(businessId).flatMap { merchantProfile ->
                val adopted = merchantProfile.payment_address.isNotEmpty()
                return@flatMap if (adopted || shouldFetchWhenCollectionNotAdopted) {
                    collectionRepository.get().getKycExternalInfo(businessId).map {
                        when (it.category) {
                            KycRiskCategory.HIGH.value -> KycRisk(
                                KycRiskCategory.HIGH,
                                merchantProfile.remainingLimit <= 0L,
                                merchantProfile.limitType ?: CollectionMerchantProfile.DAILY,
                            )
                            KycRiskCategory.LOW.value -> KycRisk(
                                KycRiskCategory.LOW,
                                merchantProfile.remainingLimit <= 0L,
                                merchantProfile.limitType ?: CollectionMerchantProfile.DAILY,
                            )
                            else -> KycRisk(
                                kycRiskCategory = KycRiskCategory.LOW,
                                isLimitReached = merchantProfile.remainingLimit <= 0L,
                                limitType = merchantProfile.limitType ?: CollectionMerchantProfile.DAILY,
                            )
                        }
                    }
                } else {
                    Observable.just(defaultRisk)
                }
            }
        }
    }
}
