package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.SetCollectionDestination
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.rewards.contract.RewardsSyncer
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.crashlytics.RecordException
import javax.inject.Inject

class SetCollectionDestinationImpl @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val rewardsSyncer: RewardsSyncer,
    private val tracker: Tracker,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : SetCollectionDestination {
    override fun execute(
        collectionMerchantProfile: CollectionMerchantProfile,
        async: Boolean,
    ): Observable<CollectionMerchantProfile> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            collectionRepository.setActiveDestination(collectionMerchantProfile, businessId = businessId)
                .ignoreElement()
                .doOnComplete {
                    rewardsSyncer.scheduleEverything(businessId)
                }
                .andThen(Observable.just(collectionMerchantProfile))
                .doOnError {
                    RecordException.recordException(it)
                    tracker.trackDebug("setActiveDestination ${it.message}")
                }
        }
    }
}
