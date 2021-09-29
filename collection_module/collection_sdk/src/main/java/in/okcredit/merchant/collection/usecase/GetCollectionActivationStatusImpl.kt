package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.GetCollectionActivationStatus
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetCollectionActivationStatusImpl @Inject constructor(private val collectionRepository: Lazy<CollectionRepository>) :
    GetCollectionActivationStatus {

    override fun execute(): Observable<Boolean> {
        return collectionRepository.get().isCollectionActivated()
    }
}
