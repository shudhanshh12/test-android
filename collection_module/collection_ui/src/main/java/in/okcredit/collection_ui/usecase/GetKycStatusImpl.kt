package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.GetKycStatus
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetKycStatusImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetKycStatus {

    private val defaultKycStatus = KycStatus.NOT_SET

    override fun execute(shouldFetchWhenCollectionNotAdopted: Boolean): Observable<KycStatus> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            isCollectionAdopted().flatMap {
                if (it || shouldFetchWhenCollectionNotAdopted) {
                    collectionRepository.get().getKycStatus(businessId).flatMap {
                        Observable.just(KycStatus.valueOf(it))
                    }
                } else {
                    Observable.just(defaultKycStatus)
                }
            }.onErrorReturn {
                defaultKycStatus
            }
        }
    }

    private fun isCollectionAdopted() = collectionRepository.get().isCollectionActivated()
}
