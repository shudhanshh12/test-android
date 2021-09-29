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

class DeleteBill @Inject constructor(
    private val billRemoteSource: Lazy<BillRemoteSource>,
    private val billLocalSource: Lazy<BillLocalSource>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<DeleteBill.Request, Unit> {
    override fun execute(req: Request): Observable<Result<Unit>> {

        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                billRemoteSource.get()
                    .deletedBill(
                        BillApiMessages.BillSyncRequest(
                            mutableListOf<BillApiMessages.BillOperation>().apply {
                                add(
                                    BillApiMessages.BillOperation(
                                        id = BillUtils.generateRandomId(),
                                        type = Type.DELETE.operationType,
                                        bill_id = req.billId,
                                        path = Path.BILLS.route,
                                        timestamp = BillUtils.currentTimestamp()
                                    )
                                )
                            }
                        ),
                        businessId
                    ).flatMapCompletable { billLocalSource.get().deleteBill(req.billId) }
            }
        )
    }

    data class Request(val billId: String)
}
