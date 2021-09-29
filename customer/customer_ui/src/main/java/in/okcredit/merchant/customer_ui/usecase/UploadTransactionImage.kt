package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.usecase.SyncTransactionImage
import `in`.okcredit.fileupload.usecase.IUploadFile
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import merchant.okcredit.accounting.model.TransactionImage
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.camera_contract.CapturedImage
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class UploadTransactionImage @Inject constructor(
    private val uploadFile: IUploadFile,
    private val syncTransactionImage: SyncTransactionImage,
    private val coreSdk: CoreSdk,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<UploadTransactionImage.Request, Unit> {

    override fun execute(req: Request): Observable<Result<Unit>> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            uploadTransactionImage(req, businessId)
        }
    }

    private fun uploadTransactionImage(req: Request, businessId: String): Observable<Result<Unit>> {
        return coreSdk.isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    coreUploadTransactionImage()
                } else {
                    frontendUploadTransactionImage(req, businessId)
                }
            }
    }

    private fun coreUploadTransactionImage(): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(Completable.complete())
    }

    private fun frontendUploadTransactionImage(req: Request, businessId: String): Observable<Result<Unit>> {
        val timestamp = DateTimeUtils.currentDateTime()
        val awsImageCompletablelist = ArrayList<Completable>()
        val syncImageCompletablelist = ArrayList<Completable>()
        var receiptUrl: String? = null
        for (i in req.newlyAddedImages.indices) {
            if (req.newlyAddedImages.get(i).file.exists()) {
                receiptUrl = IUploadFile.AWS_RECEIPT_BASE_URL + "/" + UUID.randomUUID().toString() + ".jpg"
                syncImageCompletablelist.add(
                    syncTransactionImage.schedule(
                        TransactionImage(
                            req.transactionId,
                            UUID.randomUUID().toString(),
                            req.transactionId,
                            receiptUrl,
                            timestamp
                        ),
                        businessId
                    )
                )
                awsImageCompletablelist.add(
                    uploadFile.schedule(
                        IUploadFile.RECEIPT_PHOTO,
                        receiptUrl,
                        req.newlyAddedImages.get(i).file.getAbsolutePath()
                    )
                )
            }
        }
        return UseCase.wrapCompletable(
            Completable.concat(awsImageCompletablelist)
                .andThen(Completable.concat(syncImageCompletablelist))
        )
    }

    data class Request(
        val newlyAddedImages: ArrayList<CapturedImage>,
        val transactionId: String,
        val merchantId: String,
    )
}
