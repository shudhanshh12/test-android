package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend.contract.GetTotalTxnCount
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.customer.contract.CustomerRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.crashlytics.RecordException
import javax.inject.Inject

class GetCollectionNudgeOnSetDueDate @Inject constructor(
    private val ab: Lazy<AbRepository>,
    private val getTotalTxnCount: Lazy<GetTotalTxnCount>,
    private val customerRepository: Lazy<CustomerRepository>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<Unit, Boolean> {

    companion object {
        private const val EXPT = "postlogin_android-all-collection_adoption_post_due_date"
        private const val SHOW = "show"
        private const val DONT_SHOW = "dont_show"
        private const val COLLECTION = "collection"
    }

    override fun execute(req: Unit): Observable<Result<Boolean>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                canShow(businessId)
                    .flatMap { canShow ->
                        if (canShow) {
                            isExpEnabled()
                                .flatMap {
                                    if (it) {
                                        getVariant()
                                    } else {
                                        Observable.just(false)
                                    }
                                }
                        } else {
                            Observable.just(false)
                        }
                    }
            }
        )
    }

    private fun isCollectionActivated() = collectionRepository.get().isCollectionActivated()

    private fun isExpEnabled() = ab.get().isExperimentEnabled(EXPT)

    private fun isFeatureEnabled(businessId: String) = ab.get().isFeatureEnabled(COLLECTION, businessId = businessId)

    private fun getVariant() = ab.get().getExperimentVariant(EXPT).flatMap {
        when (it) {
            SHOW -> Observable.just(true)
            DONT_SHOW -> Observable.just(false)
            else -> Observable.just(false)
        }
    }

    private fun canShow(businessId: String) = Observable.combineLatest(
        isFeatureEnabled(businessId),
        isCollectionActivated(),
        txnCountCheck(businessId),
        { isFeatureEnabled, isCollectionActivated, isConditionPassed ->
            isFeatureEnabled && isCollectionActivated.not() && isConditionPassed
        }
    )

    private fun txnCountCheck(businessId: String) = getTotalTxnCount.get().execute().toObservable().map {
        (it - customerRepository.get().getTxnCntForCollectionNudgeOnSetDueDate(businessId)) <= 2
    }.onErrorReturn {
        RecordException.recordException(it)
        false
    }
}
