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
import javax.inject.Inject

class GetCollectionNudgeForCustomerScreen @Inject constructor(
    private val ab: Lazy<AbRepository>,
    private val getTotalTxnCount: Lazy<GetTotalTxnCount>,
    private val customerRepository: Lazy<CustomerRepository>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<Unit, GetCollectionNudgeForCustomerScreen.Show> {
    companion object {
        private const val EXPT = "postlogin_android-all-collection_adoption_customer_page_widget"
        private const val PAYMENT_SIDE_VARIANT = "payment_side"
        private const val CREDIT_SIDE_VARIANT = "credit_side"
        private const val COLLECTION = "collection"
    }

    enum class Show {
        PAYMENT_SIDE, CREDIT_SIDE, NONE
    }

    override fun execute(req: Unit): Observable<Result<Show>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                canShow(businessId).flatMap { canShow ->
                    if (canShow) {
                        isExpEnabled().flatMap {
                            if (it) {
                                getVariant()
                            } else {
                                Observable.just(Show.NONE)
                            }
                        }
                    } else {
                        Observable.just(Show.NONE)
                    }
                }
            }
        )
    }

    private fun isCollectionActivated() = collectionRepository.get().isCollectionActivated()

    private fun isExpEnabled() = ab.get().isExperimentEnabled(EXPT)

    private fun isFeatureEnabled() = ab.get().isFeatureEnabled(COLLECTION)

    private fun getVariant() = ab.get().getExperimentVariant(EXPT).flatMap {
        when (it) {
            PAYMENT_SIDE_VARIANT -> Observable.just(Show.PAYMENT_SIDE)
            CREDIT_SIDE_VARIANT -> Observable.just(Show.CREDIT_SIDE)
            else -> Observable.just(Show.NONE)
        }
    }

    private fun canShow(businessId: String) = Observable.combineLatest(
        isFeatureEnabled(),
        isCollectionActivated(),
        txnCountCheck(businessId),
        { isFeatureEnabled, isCollectionActivated, isConditionPassed ->
            isFeatureEnabled && isCollectionActivated.not() && isConditionPassed
        }
    )

    private fun txnCountCheck(businessId: String) = getTotalTxnCount.get().execute().toObservable().map {
        (it - customerRepository.get().getTxnCntForCollectionNudgeOnCustomerScr(businessId)) <= 5
    }.onErrorReturn {
        false
    }
}
