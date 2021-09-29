package tech.okcredit.home.ui.reminder.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class SendBulkReminder @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(customerList: List<String>): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable {
            collectionRepository.get().createBatchCollection(customerList, it)
        }
    }
}
