package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.UpdateCustomerReferralLedgerSeen
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class UpdateCustomerReferralLedgerSeenImpl @Inject constructor(private val collectionRepository: Lazy<CollectionRepository>) :
    UpdateCustomerReferralLedgerSeen {
    override fun execute(customerId: String): Completable {
        return collectionRepository.get().updateReferralLedgerShown(customerId)
    }
}
