package tech.okcredit.home.usecase

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.*
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.Companion.from
import `in`.okcredit.merchant.core.usecase.ImmutableConflictHelper
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

class GetCustomerAndCollectionProfile @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
    private val getCustomer: Lazy<GetCustomer>,
    private val immutableConflictHelper: Lazy<ImmutableConflictHelper>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(customerId: String): Observable<Response> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            getCustomer.get().execute(customerId)
                .switchMap { customer ->
                    when (from(customer.customerSyncStatus)) {
                        DIRTY -> {
                            Observable.just(Response(customer))
                        }
                        IMMUTABLE -> {
                            customer.mobile?.takeUnless { it.isBlank() }
                                ?.let { mobile ->
                                    rxSingle {
                                        Response(
                                            customer,
                                            cleanCompanionDescription = immutableConflictHelper.get()
                                                .getCleanCustomerByMobile(mobile, businessId)?.description
                                        )
                                    }
                                }
                                ?.toObservable()
                                ?: Observable.just(Response(customer))
                        }
                        CLEAN -> {
                            collectionRepository.get().isCollectionActivated()
                                .switchMap { collectionActivated ->
                                    if (collectionActivated) {
                                        collectionSyncer.get()
                                            .scheduleCollectionProfileForCustomer(customerId, businessId)
                                        collectionRepository.get().getCollectionCustomerProfile(customerId, businessId)
                                            .map { Response(customer, collectionCustomerProfile = it) }
                                    } else {
                                        Observable.just(Response(customer))
                                    }
                                }
                        }
                    }
                }
        }
    }

    data class Response(
        val customer: Customer,
        val collectionCustomerProfile: CollectionCustomerProfile? = null,
        val cleanCompanionDescription: String? = null, // If immutable, description of the clean profile that conflicts
    )
}
