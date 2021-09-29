package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionShareInfo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.shared.utils.CommonUtils
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class MarkCollectionShared @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<String, Unit> {

    override fun execute(req: String): Observable<Result<Unit>> {
        val customersIds = arrayListOf<String>()
        customersIds.add(req)
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                collectionRepository.get().insertCollectionShareInfo(
                    CollectionShareInfo(
                        req,
                        CommonUtils.currentDateTime()
                    ),
                    businessId
                )
            }

        )
    }
}
