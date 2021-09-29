package `in`.okcredit.merchant.rewards.ui.rewards_screen.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.shared.utils.CommonUtils
import dagger.Lazy
import io.reactivex.Observable
import org.joda.time.Days
import tech.okcredit.android.ab.AbRepository
import timber.log.Timber
import javax.inject.Inject

class GetHomeMerchantData @Inject constructor(
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val ab: Lazy<AbRepository>,
    private val collectionRepository: Lazy<CollectionRepository>,
) {
    companion object {
        const val COLLECTION = "collection"
    }

    fun execute(): Observable<Result<Response>> {

        val observables = listOf(
            getActiveBusiness.get().execute(),
            ab.get().isFeatureEnabled(COLLECTION).startWith(false),
            collectionRepository.get().isCollectionActivated()
        )

        return UseCase.wrapObservable(
            Observable.combineLatest(observables) {
                Timber.i("]]]> processing supplier tab visibility")
                val merchant = it[0] as Business
                val isCollectionEnabled = it[1] as Boolean
                val isCollectionActivated = it[2] as Boolean

                var isShare = false
                var isCollection = false
                var isAccount = false

                // we show share icon within 24hrs of merchant registration, collection icon for all users who enable collection feature
                when {
                    CommonUtils.currentDateTime().isBefore(merchant.createdAt.plus(Days.days(1))) -> isShare = true
                    isCollectionEnabled -> isCollection = true
                    else -> isAccount = true
                }

                // we show collection tutorial to all users who enabled collection and not adapted.
                val showCollectionTutorial = !isShare && isCollectionEnabled && !isCollectionActivated

                return@combineLatest Response(
                    business = merchant, share = isShare,
                    collection = isCollection, account = isAccount,
                    showCollectionTutorial = showCollectionTutorial,
                    currentTab = 0, isAdaptedCollection = isCollectionActivated
                )
            }
        )
    }

    data class Response(
        val business: Business,
        val share: Boolean,
        val collection: Boolean,
        val account: Boolean,
        val showCollectionTutorial: Boolean,
        val currentTab: Int,
        val isAdaptedCollection: Boolean,
    )
}
