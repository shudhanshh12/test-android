package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.model.DueInfo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.backend.contract.GetTotalTxnCount
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.customer.contract.CustomerRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import org.joda.time.DateTime
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.crashlytics.RecordException
import javax.inject.Inject

class GetCollectionNudgeOnDueDateCrossed @Inject constructor(
    private val getCustomer: Lazy<GetCustomer>,
    private val ab: Lazy<AbRepository>,
    private val getTotalTxnCount: Lazy<GetTotalTxnCount>,
    private val customerRepository: Lazy<CustomerRepository>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<DueInfo, GetCollectionNudgeOnDueDateCrossed.Response> {

    companion object {
        private const val EXPT = "postlogin_android-all-due_date_crossed"
        private const val CONTROL = "control"
        private const val FEATURE_COLLECTION = "collection"
        private const val COLLECTIONS = "collections"
        private const val UPDATE_DUE_DATE = "update_due_date"
    }

    enum class Show {
        UPDATE, SETUP_COLLECTION, NONE
    }

    data class Response(val customer: Customer, val dueInfo: DueInfo, val show: Show)

    override fun execute(req: DueInfo): Observable<Result<Response>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                canShow(req, businessId)
                    .flatMap {
                        isExpEnabled()
                            .flatMap {
                                getResponse(req)
                            }
                    }
            }
        )
    }

    private fun getResponse(dueInfo: DueInfo) = getVariant()
        .flatMap { show ->
            getCustomer.get().execute(dueInfo.customerId).flatMap { customer ->
                Observable.just(Response(customer, dueInfo, show))
            }
        }

    private fun isCollectionActivated() = collectionRepository.get().isCollectionActivated()

    private fun isExpEnabled() = ab.get().isExperimentEnabled(EXPT).filter { it }

    private fun isFeatureEnabled(businessId: String) =
        ab.get().isFeatureEnabled(FEATURE_COLLECTION, businessId = businessId)

    private fun getVariant() = ab.get().getExperimentVariant(EXPT).flatMap {
        when (it) {
            COLLECTIONS -> Observable.just(Show.SETUP_COLLECTION)
            UPDATE_DUE_DATE -> Observable.just(Show.UPDATE)
            else -> Observable.just(Show.NONE)
        }
    }.filter { it != Show.NONE }

    private fun canShow(dueInfo: DueInfo, businessId: String) = Observable.combineLatest(
        isFeatureEnabled(businessId),
        isCollectionActivated(),
        txnCountCheck(businessId),
        { isFeatureEnabled, isCollectionActivated, isConditionPassed ->
            isFeatureEnabled && isCollectionActivated.not() && isConditionPassed && dueInfo.isDueActive && dueInfo.activeDate != null && dueInfo.activeDate!!.isBefore(
                DateTime.now().withTimeAtStartOfDay()
            )
        }
    ).filter { it }

    private fun txnCountCheck(businessId: String) = getTotalTxnCount.get().execute().toObservable().map {
        (it - customerRepository.get().getTxnCntForCollectionNudgeOnDueDateCrossed(businessId)) <= 2
    }.onErrorReturn {
        RecordException.recordException(it)
        false
    }
}
