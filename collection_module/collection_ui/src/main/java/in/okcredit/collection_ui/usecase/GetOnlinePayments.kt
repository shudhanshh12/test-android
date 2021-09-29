package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import org.joda.time.DateTime
import javax.inject.Inject

class GetOnlinePayments @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val customerRepo: CustomerRepo,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<GetOnlinePayments.Request, List<GetOnlinePayments.OnlineCollectionData>> {

    data class OnlineCollectionData(
        val collectionOnlinePayment: CollectionOnlinePayment,
        val customer: Customer? = null,
    )

    data class Request(val startTime: DateTime, val endTime: DateTime)

    override fun execute(req: Request): Observable<Result<List<OnlineCollectionData>>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                return@flatMapObservable setDataRead(businessId).andThen(
                    Observable.combineLatest(
                        collectionRepository.listOnlinePayments(businessId),
                        customerRepo.listCustomers(businessId),
                        { collectionOnlinePayments, customers ->
                            collectionOnlinePayments
                                .filter {
                                    it.createdTime.isAfter(
                                        req.startTime.millis
                                    ) && it.createdTime.isBefore(req.endTime.millis)
                                }
                                .map { collectionOnlinePayment ->
                                    val customer = customers.find { it.id == collectionOnlinePayment.accountId }
                                    OnlineCollectionData(collectionOnlinePayment, customer)
                                }
                        }
                    )
                )
            }
        )
    }

    private fun setDataRead(businessId: String): Completable {
        return collectionRepository.setOnlinePaymentsDataRead(businessId)
    }
}
