package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.collection.contract.CanShowAddBankDetailsPopUp
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class CanShowAddBankDetailsPopUpImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val customerRepo: Lazy<CustomerRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : CanShowAddBankDetailsPopUp {
    override fun execute(): Single<List<String>> {
        return collectionRepository.get().isCollectionActivated().firstOrError().flatMap { activated ->
            if (activated) return@flatMap Single.just(emptyList())

            return@flatMap getActiveBusinessId.get().execute().flatMap { businessId ->
                collectionRepository.get().customerCountWithPaymentIntent(businessId)
                    .flatMap { idList ->
                        customerRepo.get().listActiveCustomers(businessId).firstOrError().map { customerList ->
                            customerList.mapNotNull { customer ->
                                if (idList.contains(customer.id)) {
                                    customer.description
                                } else {
                                    null
                                }
                            }
                        }
                    }
            }
        }
    }
}
