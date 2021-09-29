package tech.okcredit.bill_management_ui.edit_notes

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.BillUtils
import tech.okcredit.sdk.models.Mask
import tech.okcredit.sdk.models.Path
import tech.okcredit.sdk.models.Type
import tech.okcredit.sdk.server.BillApiMessages
import tech.okcredit.sdk.server.BillRemoteSource
import tech.okcredit.sdk.store.BillLocalSource
import javax.inject.Inject

class UpdateNote @Inject constructor(
    private val billLocalSource: Lazy<BillLocalSource>,
    private val billRemoteSource: Lazy<BillRemoteSource>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<UpdateNote.Request, Unit> {
    override fun execute(req: Request): Observable<Result<Unit>> {

        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                billLocalSource.get().getBill(req.billId, businessId).firstOrError().flatMapCompletable {
                    val billOperation = BillApiMessages.BillOperation(
                        id = BillUtils.generateRandomId(),
                        type = Type.UPDATE.operationType,
                        bill_id = req.billId,
                        path = Path.BILLS.route,
                        mask = mutableListOf(Mask.NOTES.field),
                        timestamp = BillUtils.currentTimestamp(),
                        serverBill = BillApiMessages.ServerBill(id = it.id, note = req.note, created_at_ms = it.createdAt)
                    )
                    billRemoteSource.get().updateNote(
                        BillApiMessages.BillSyncRequest(
                            mutableListOf(billOperation)
                        ),
                        businessId
                    ).flatMapCompletable { billLocalSource.get().updateNote(req.note, req.billId) }
                }
            }
        )
    }

    data class Request(val billId: String, val note: String, val originalNote: String)
}
