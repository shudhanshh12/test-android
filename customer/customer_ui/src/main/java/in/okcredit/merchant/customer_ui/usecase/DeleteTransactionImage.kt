package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.backend._offline.usecase.SyncDeleteTransactionImage
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class DeleteTransactionImage @Inject constructor(
    val remoteSource: BackendRemoteSource,
    private val syncDeleteTransactionImage: SyncDeleteTransactionImage,
    private val coreSdk: CoreSdk,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<DeleteTransactionImage.RequestBody, Unit> {

    override fun execute(req: RequestBody): Observable<Result<Unit>> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            deleteTransactionImage(req, businessId)
        }
    }

    private fun deleteTransactionImage(req: RequestBody, businessId: String): Observable<Result<Unit>> {
        return coreSdk.isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    coreDeleteTransactionImage()
                } else {
                    frontendDeleteTransactionImage(req, businessId)
                }
            }
    }

    private fun coreDeleteTransactionImage(): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(Completable.complete())
    }

    private fun frontendDeleteTransactionImage(requestBody: RequestBody, businessId: String): Observable<Result<Unit>> {
        val completableList = mutableListOf<Completable>()
        for (i in 0 until requestBody.deletedPhotoList.size) {
            completableList.add(syncDeleteTransactionImage.schedule(requestBody.deletedPhotoList[i], businessId))
        }
        return UseCase.wrapCompletable(Completable.concat(completableList))
    }

    data class RequestBody(val deletedPhotoList: ArrayList<merchant.okcredit.accounting.model.TransactionImage>)
}
