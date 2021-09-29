package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.common.CoreModuleMapper
import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncDirtyTransactions
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.fileupload.usecase.IUploadFile
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.Command
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.core.common.CoreUtils
import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.model.Transaction.Type.Companion.getTransactionType
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import merchant.okcredit.accounting.model.Transaction
import merchant.okcredit.accounting.model.TransactionImage
import org.joda.time.DateTime
import tech.okcredit.android.auth.usecases.VerifyPassword
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.camera_contract.CapturedImage
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class AddTransaction @Inject constructor(
    private val transactionRepo: Lazy<TransactionRepo>,
    private val uploadFile: Lazy<IUploadFile>,
    private val verifyPassword: Lazy<VerifyPassword>,
    private val collectionAPI: Lazy<CollectionRepository>,
    private val syncDirtyTransactions: Lazy<SyncDirtyTransactions>,
    private val coreSdk: Lazy<CoreSdk>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(
        type: Int,
        customerId: String,
        amountv2: Long,
        capturedImageList: List<CapturedImage>?,
        note: String?,
        isOnboarding: Boolean,
        password: String?,
        isVerifyPasswordRequired: Boolean,
        billDate: DateTime,
        inputType: String?,
        voiceId: String?,
    ): Single<Transaction> {
        if (amountv2 == 0L) return Single.error(IllegalArgumentException("Amount cannot be 0"))

        return getActiveBusinessId.get().execute().flatMap { businessId ->
            coreSdk.get().isCoreSdkFeatureEnabled(businessId).flatMap {
                if (it) {
                    coreAddTransaction(
                        type = type,
                        customerId = customerId,
                        amountv2 = amountv2,
                        capturedImageList = capturedImageList,
                        note = note,
                        password = password,
                        isVerifyPasswordRequired = isVerifyPasswordRequired,
                        billDate = billDate,
                        inputType = inputType,
                        voiceId = voiceId,
                        businessId = businessId
                    )
                } else {
                    backendAddTransaction(
                        type = type,
                        customerId = customerId,
                        amountv2 = amountv2,
                        capturedImageList = capturedImageList,
                        note = note,
                        isOnboarding = isOnboarding,
                        password = password,
                        isVerifyPasswordRequired = isVerifyPasswordRequired,
                        billDate = billDate,
                        inputType = inputType,
                        voiceId = voiceId,
                        businessId = businessId
                    )
                }
            }
        }
    }

    private fun coreAddTransaction(
        type: Int,
        customerId: String,
        amountv2: Long,
        capturedImageList: List<CapturedImage>?,
        note: String?,
        password: String?,
        isVerifyPasswordRequired: Boolean,
        billDate: DateTime?,
        inputType: String?,
        voiceId: String?,
        businessId: String,
    ): Single<Transaction> {
        var authCheck = Completable.complete()
        if (isVerifyPasswordRequired) {
            authCheck = verifyPassword.get().execute(password)
        }
        val imagesUriList: MutableList<String> = ArrayList()
        if (capturedImageList != null) {
            for (image in capturedImageList) {
                imagesUriList.add(image.file.absolutePath)
            }
        }
        var billDateTimestamp: Timestamp? = null
        if (billDate != null) {
            billDateTimestamp = Timestamp(billDate.millis)
        }
        return authCheck.andThen(
            coreSdk.get().processTransactionCommand(
                Command.CreateTransaction(
                    customerId = customerId,
                    transactionId = CoreUtils.generateRandomId(),
                    type = getTransactionType(type),
                    amount = amountv2,
                    imagesUriList = imagesUriList,
                    note = note,
                    billDate = billDateTimestamp,
                    inputType = inputType,
                    voiceId = voiceId
                ),
                businessId
            ).map(CoreModuleMapper::toTransaction)
        )
    }

    private fun backendAddTransaction(
        type: Int,
        customerId: String,
        amountv2: Long,
        capturedImageList: List<CapturedImage>?,
        note: String?,
        isOnboarding: Boolean,
        password: String?,
        isVerifyPasswordRequired: Boolean,
        billDate: DateTime,
        inputType: String?,
        voiceId: String?,
        businessId: String,
    ): Single<Transaction> {
        Timber.d("<<<<AddTransaction 0")
        var authCheck = Completable.complete()
        if (isVerifyPasswordRequired) {
            authCheck = verifyPassword.get().execute(password)
        }
        val timestamp = DateTimeUtils.currentDateTime()
        val completableList: MutableList<Completable> = ArrayList()
        val receiptURLList: MutableList<TransactionImage> = ArrayList()
        var receiptUrl: String
        if (capturedImageList != null) {
            for (i in capturedImageList.indices) {
                if (capturedImageList[i].file.exists()) {
                    receiptUrl = IUploadFile.AWS_RECEIPT_BASE_URL + "/" +
                        UUID.randomUUID() + ".jpg"
                    receiptURLList.add(
                        TransactionImage(
                            null,
                            UUID.randomUUID().toString(),
                            null,
                            receiptUrl,
                            timestamp
                        )
                    )
                    completableList.add(
                        uploadFile.get().schedule(
                            IUploadFile.RECEIPT_PHOTO,
                            receiptUrl,
                            capturedImageList[i].file.absolutePath
                        )
                    )
                }
            }
        }
        val uploadReceiptTask = Completable.concat(completableList)
        // id and timestamp
        val requestId = UUID.randomUUID().toString()
        return authCheck
            .andThen(uploadReceiptTask)
            .andThen(
                createTransaction(
                    customerId = customerId,
                    requestId = requestId,
                    type = type,
                    amountV2 = amountv2,
                    receiptUrlList = receiptURLList,
                    note = note,
                    timestamp = timestamp,
                    isOnboarding = isOnboarding,
                    billDate = billDate,
                    inputType = inputType,
                    voiceId = voiceId
                )
            )
            .flatMap { transaction: Transaction ->
                transactionRepo.get().putTransaction(transaction, businessId)
                    .andThen(syncDirtyTransactions.get().schedule(businessId))
                    .andThen(collectionAPI.get().deleteCollectionShareInfoOfCustomer(transaction.customerId))
                    .andThen(Single.just(transaction))
            }
    }

    private fun createTransaction(
        customerId: String,
        requestId: String,
        type: Int,
        amountV2: Long,
        receiptUrlList: List<TransactionImage>,
        note: String?,
        timestamp: DateTime,
        isOnboarding: Boolean,
        billDate: DateTime,
        inputType: String?,
        voiceId: String?,
    ): Single<Transaction> {
        val localCopy = Transaction(
            id = requestId,
            type = type,
            customerId = customerId,
            collectionId = null,
            amountV2 = amountV2,
            receiptUrl = receiptUrlList,
            note = note,
            createdAt = timestamp,
            isOnboarding = isOnboarding,
            isDeleted = false,
            deleteTime = null,
            isDirty = true,
            billDate = billDate,
            updatedAt = timestamp,
            isSmsSent = false,
            isCreatedByCustomer = false,
            isDeletedByCustomer = false,
            inputType = inputType,
            voiceId = voiceId,
            transactionState = Transaction.CREATED,
            transactionCategory = Transaction.DEAFULT_CATERGORY,
            amountUpdated = false,
            amountUpdatedAt = null
        )
        return Single.just(localCopy)
    }
}
