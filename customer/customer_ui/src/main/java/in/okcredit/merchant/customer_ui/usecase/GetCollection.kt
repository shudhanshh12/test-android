package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncCustomer
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.util.Pair
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class GetCollection @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val customerRepo: CustomerRepo,
    private val syncCustomer: SyncCustomer,
    private val collectionSyncer: CollectionSyncer,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<String, GetCollection.CollectionWrapper> {
    override fun execute(req: String): Observable<Result<CollectionWrapper>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                collectionSyncer.scheduleSyncCollections(
                    syncType = CollectionSyncer.SYNC_CUSTOMER_COLLECTIONS,
                    source = CollectionSyncer.Source.GET_COLLECTION,
                    businessId = businessId
                )
                Observable.combineLatest(
                    collectionRepository.getCollection(req, businessId),
                    customerRepo.listCustomers(businessId),
                    { collection, customers -> process(customers, collection) }
                )
                    .flatMap { res ->
                        var syncMissingCustomers = Completable.complete()
                        val missingCustomer = res.second

                        if (res.second == null && !missingCustomer.isNullOrEmpty()) {
                            syncMissingCustomers =
                                syncMissingCustomers.andThen(
                                    syncCustomer.schedule(missingCustomer, businessId)
                                )
                        }

                        syncMissingCustomers.andThen(Observable.just(res.first))
                    }
            }
        )
    }

    private fun process(
        customers: List<Customer?>,
        collection: Collection,
    ): Pair<CollectionWrapper, String?> {
        val tx: CollectionWrapper
        var isCustomerMissing = null
        var customer: Customer? = null

        for (item in customers) {
            if (collection.customer_id == item?.id) {
                customer = item
            }
        }

        if (customer != null) {
            tx = CollectionWrapper(
                collection,
                getCustomerName(customer),
                customer.profileImage
            )
        } else {
            isCustomerMissing = customer
            tx = CollectionWrapper(
                collection, null,
                null
            )
        }
        return Pair(tx, isCustomerMissing)
    }

    private fun getCustomerName(customer: Customer): String {
        return if (customer.status == 1) {
            customer.description
        } else {
            try {
                customer.description.substring(0, customer.description.indexOf("" + " ["))
            } catch (e: Exception) {
                customer.description
            }
        }
    }

    data class CollectionWrapper(
        val collection: Collection,
        val customerName: String?,
        val customerProfile: String?,
    )
}
