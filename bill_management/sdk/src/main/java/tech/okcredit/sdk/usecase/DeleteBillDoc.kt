package tech.okcredit.sdk.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.BillUtils
import tech.okcredit.sdk.models.Path
import tech.okcredit.sdk.models.Type
import tech.okcredit.sdk.server.BillApiMessages
import tech.okcredit.sdk.server.BillRemoteSource
import tech.okcredit.sdk.store.BillLocalSource
import javax.inject.Inject

class DeleteBillDoc @Inject constructor(
    private val billLocalSource: Lazy<BillLocalSource>,
    private val billRemoteSource: Lazy<BillRemoteSource>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<DeleteBillDoc.Request, Unit> {
    override fun execute(req: Request): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                billLocalSource.get().getBill(req.billId, businessId).firstOrError().flatMapCompletable {
                    val billOperation = BillApiMessages.BillOperation(
                        id = BillUtils.generateRandomId(),
                        type = Type.DELETE.operationType,
                        bill_id = req.billId,
                        path = Path.DOCS.route,
                        timestamp = BillUtils.currentTimestamp(),
                        bill_doc_id = req.billDocId
                    )
                    billRemoteSource.get().deleteBillDoc(
                        BillApiMessages.BillSyncRequest(
                            mutableListOf(billOperation)
                        ),
                        businessId
                    ).flatMapCompletable { billLocalSource.get().deleteBillDoc(req.billDocId) }
                }
            }
        )
    }

    data class Request(val billDocId: String, val billId: String)
}
