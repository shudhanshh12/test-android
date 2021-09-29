package tech.okcredit.sdk.usecase

import `in`.okcredit.fileupload.usecase.IUploadFile
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import tech.okcredit.BillUtils
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.sdk.DbEntityMapper
import tech.okcredit.sdk.models.Path
import tech.okcredit.sdk.models.Type
import tech.okcredit.sdk.server.BillApiMessages
import tech.okcredit.sdk.server.BillRemoteSource
import tech.okcredit.sdk.store.BillLocalSource
import tech.okcredit.sdk.store.database.DbBillDoc
import tech.okcredit.sdk.store.database.LocalBillDoc
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class AddNewBillDocs @Inject constructor(
    private val billLocalSource: BillLocalSource,
    private var uploadFile: IUploadFile? = null,
    private var billRemoteSource: BillRemoteSource,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<AddNewBillDocs.Request, AddNewBillDocs.Response> {

    override fun execute(req: Request): Observable<Result<Response>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                val operationList = ArrayList<BillApiMessages.BillOperation>()
                val capturedImageList = req.list
                val completableList: MutableList<Completable> =
                    java.util.ArrayList()
                for (i in capturedImageList.indices) {
                    if (capturedImageList.get(i).file.exists()) {

                        val receiptUrl = IUploadFile.AWS_RECEIPT_BASE_URL + "/" +
                            UUID.randomUUID() + ".jpg"
                        completableList.add(
                            uploadFile!!.schedule(
                                IUploadFile.RECEIPT_PHOTO,
                                receiptUrl,
                                capturedImageList.get(i).file.absolutePath
                            )
                        )

                        val billDocId = BillUtils.generateRandomId()
                        val currentTime = BillUtils.currentTimestamp()
                        operationList.add(
                            BillApiMessages.BillOperation(
                                id = BillUtils.generateRandomId(),
                                type = Type.ADD.operationType,
                                bill_id = req.billId,
                                path = Path.DOCS.route,
                                bill_doc_id = billDocId,
                                serverBillDoc = BillApiMessages.ServerBillDoc(
                                    id = billDocId,
                                    created_at_ms = currentTime.toString(),
                                    url = receiptUrl
                                ),
                                timestamp = currentTime
                            )
                        )
                    }
                }
                val uploadReceiptTask = Completable.concat(completableList)
                uploadReceiptTask.andThen(
                    billRemoteSource.uploadNewBillDocs(
                        BillApiMessages.BillSyncRequest(
                            operationList
                        ),
                        businessId
                    ).flatMapCompletable {
                        val localBillDocList = ArrayList<DbBillDoc>()
                        for (i in operationList) {
                            val item = i.serverBillDoc!!
                            localBillDocList.add(
                                DbEntityMapper.getDocs(businessId).reverse()
                                    .convert(
                                        LocalBillDoc(
                                            billDocId = item.id,
                                            url = item.url,
                                            createdAt = item.created_at_ms,
                                            billId = req.billId
                                        )
                                    )!!
                            )
                        }
                        billLocalSource.putNewBillDocsInDb(localBillDocList)
                    }
                ).andThen(Observable.just(Response(req.list.size)))
            }
        )
    }

    data class Request(val billId: String, val list: List<CapturedImage>)

    data class Response(val addedImageCount: Int)
}
