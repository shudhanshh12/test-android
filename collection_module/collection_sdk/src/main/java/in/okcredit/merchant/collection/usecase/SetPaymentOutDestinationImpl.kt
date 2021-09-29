package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.SetPaymentOutDestination
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class SetPaymentOutDestinationImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : SetPaymentOutDestination {
    override fun execute(
        accountId: String,
        accountType: String,
        paymentType: String,
        paymentAddress: String
    ): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            collectionRepository.get().setPaymentOutDestination(accountId, accountType, paymentType, paymentAddress, businessId)
        }
    }
}
