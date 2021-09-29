package tech.okcredit.home.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.shared.utils.CommonUtils
import io.reactivex.Observable
import org.joda.time.Days
import timber.log.Timber
import javax.inject.Inject

class GetHomeMerchantData @Inject constructor(
    private val getActiveBusiness: GetActiveBusiness,
    private val collectionRepository: CollectionRepository
) : UseCase<Unit, GetHomeMerchantData.Response> {
    override fun execute(req: Unit): Observable<Result<Response>> {

        val observables = listOf(
            getActiveBusiness.execute(),
            collectionRepository.isCollectionActivated()
        )

        return UseCase.wrapObservable(
            Observable.combineLatest(observables) {
                Timber.i("]]]> processing supplier tab visibility")
                val business = it[0] as Business
                val isCollectionActivated = it[1] as Boolean

                var isShare = false
                var isAccount = false

                // we show share icon within 24hrs of business registration, collection icon for all users
                when {
                    CommonUtils.currentDateTime().isBefore(business.createdAt.plus(Days.days(1))) -> isShare = true
                    else -> isAccount = true
                }

                // we show collection tutorial to all users who enabled collection and not adapted.
                val showCollectionTutorial = !isShare && !isCollectionActivated

                return@combineLatest Response(
                    business = business, share = isShare,
                    collection = true,
                    account = isAccount,
                    showCollectionTutorial = showCollectionTutorial,
                    isAdaptedCollection = isCollectionActivated
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
        val isAdaptedCollection: Boolean
    )
}
