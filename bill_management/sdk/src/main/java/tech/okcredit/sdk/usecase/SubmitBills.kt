package tech.okcredit.sdk.usecase

import `in`.okcredit.fileupload.usecase.IUploadFile
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.utils.Utils
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import org.joda.time.DateTime
import tech.okcredit.BillUtils
import tech.okcredit.sdk.ApiEntityMapper
import tech.okcredit.sdk.DbEntityMapper
import tech.okcredit.sdk.models.Path
import tech.okcredit.sdk.models.RawBill
import tech.okcredit.sdk.models.Type
import tech.okcredit.sdk.server.BillApiMessages
import tech.okcredit.sdk.server.BillApiMessages.BillOperation
import tech.okcredit.sdk.server.BillRemoteSource
import tech.okcredit.sdk.store.BillLocalSource
import tech.okcredit.sdk.store.database.LocalBill
import tech.okcredit.sdk.store.database.LocalBillDoc
import java.util.*
import javax.inject.Inject

class SubmitBills @Inject constructor(
    private val billRemoteSource: Lazy<BillRemoteSource>,
    private val billLocalSource: Lazy<BillLocalSource>,
    private var uploadFile: IUploadFile? = null,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(req: Request): Single<Response> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            val uploadReceiptTask: Completable?
            val completableList: MutableList<Completable> = mutableListOf()
            var receiptUrl: String
            val billId = BillUtils.generateRandomId()

            val created = DateTime.now().millis.toString()
            val capturedImageList = req.bill.imageList
            val billOperationList = mutableListOf<BillOperation>()
            val localBillDocList = mutableListOf<LocalBillDoc>()
            capturedImageList?.forEach { capturedImage ->
                if (capturedImage.file.exists()) {
                    receiptUrl = IUploadFile.AWS_RECEIPT_BASE_URL + "/" + UUID.randomUUID() + ".jpg"
                    completableList.add(
                        uploadFile!!.schedule(
                            IUploadFile.RECEIPT_PHOTO,
                            receiptUrl,
                            capturedImage.file.absolutePath
                        )
                    )

                    val billDocId = BillUtils.generateRandomId()
                    val billDoc = LocalBillDoc(billDocId, receiptUrl, createdAt = created, billId = billId)
                    localBillDocList.add(billDoc)
                }
            }
            val bill = BillApiMessages.ServerBill(
                id = billId,
                serverBillDocList = Utils.mapList(localBillDocList, ApiEntityMapper.BILL_DOC),
                created_at_ms = created,
                account_id = req.accountId,
                note = req.bill.note,
                bill_date_ms = req.bill.billDate.millis.toString()
            )

            val operationId = BillUtils.generateRandomId()
            billOperationList.add(
                BillOperation(
                    operationId,
                    Type.ADD.operationType,
                    Path.BILLS.route,
                    bill,
                    null,
                    created.toLong(),
                    null,
                    billId,
                    null
                )
            )

            uploadReceiptTask = Completable.concat(completableList)
            uploadReceiptTask
                .andThen(
                    getActiveBusinessId.get().execute().flatMap {
                        billRemoteSource.get().createBill(billOperationList, it, businessId).flatMap {
                            billLocalSource.get().putBills(
                                Utils.mapList(
                                    mutableListOf<LocalBill>().apply {
                                        add(
                                            LocalBill(
                                                billId,
                                                accountId = req.accountId,
                                                localBillDocList = localBillDocList,
                                                createdAt = created,
                                                note = req.bill.note,
                                                billDate = req.bill.billDate.millis.toString(),
                                                createdByMe = true
                                            )
                                        )
                                    },
                                    DbEntityMapper.getBills(businessId)
                                ),
                                Utils.mapList(localBillDocList, DbEntityMapper.getDocs(businessId).reverse())
                            )
                                .andThen(Single.just(Response(billId)))
                        }
                    }
                )
        }
    }

    data class Request(val accountId: String, val bill: RawBill)

    data class Response(val billId: String)
}
