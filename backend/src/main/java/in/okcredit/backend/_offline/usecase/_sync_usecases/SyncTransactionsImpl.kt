package `in`.okcredit.backend._offline.usecase._sync_usecases

import `in`.okcredit.accounting_core.contract.SyncState
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend._offline.serverV2.ServerV2
import `in`.okcredit.backend._offline.serverV2.internal.ApiMessagesV2
import `in`.okcredit.backend._offline.serverV2.internal.toDomainModel
import `in`.okcredit.backend.contract.SyncTransaction
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.core.analytics.CoreTracker
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3URI
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import org.apache.commons.io.IOUtils
import org.apache.commons.jcs.utils.zip.CompressionUtil
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.LogUtils
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.getStringStackTrace
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.base.Traces
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.Serializable
import java.net.URI
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

// Trace1
class SyncTransactionsImpl @Inject constructor(
    private val serverV2: Lazy<ServerV2>,
    private val transactionRepo: Lazy<TransactionRepo>,
    private val transferUtility: Lazy<TransferUtility>,
    private val tracker: Lazy<Tracker>,
    private val transactionsSyncService: Lazy<TransactionsSyncService>,
    private val syncDirtyTransactions: Lazy<SyncDirtyTransactions>,
    private val workManager: Lazy<OkcWorkManager>,
    private val context: Lazy<Context>,
    private val coreSdk: Lazy<CoreSdk>,
    private val coreTracker: Lazy<CoreTracker>,
    firebasePerformance: Lazy<FirebasePerformance>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : SyncTransaction {

    companion object {
        const val TRANSACTION_PROCESSING_LIMIT_KEY = "transaction_processing_limit"
        const val TAG = "<<<<SyncAuthScope"
        const val DOWNLOADED_FILE_NAME = "transactions.gz"
    }

    private var timeStartInSec: Long = System.currentTimeMillis()
    private val traceSync = firebasePerformance.get().newTrace(Traces.Trace_syncTxsPerformance)

    fun execute(
        source: String,
        req: BehaviorSubject<SyncState>? = null,
        isFromSyncScreen: Boolean = false,
        businessId: String? = null,
    ): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId)
            .flatMapCompletable { _businessId ->
                coreSdk.get().isCoreSdkFeatureEnabled(_businessId)
                    .flatMapCompletable {
                        if (it) {
                            coreSyncTransactions(source, req, isFromSyncScreen, false, _businessId)
                        } else {
                            backendSyncTransactions(source, req, isFromSyncScreen, false, _businessId)
                        }
                    }
            }
    }

    override fun executeForceSync(businessId: String?): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId)
            .flatMapCompletable { _businessId ->
                coreSdk.get().isCoreSdkFeatureEnabled(_businessId)
                    .flatMapCompletable { isCoreSync ->
                        coreTracker.get().trackForceTransactionSyncStarted(isCoreSync)
                        return@flatMapCompletable getNumberOfTransactionsBeforeSync(
                            isCoreSync,
                            _businessId
                        ).flatMapCompletable { res ->
                            if (isCoreSync) {
                                coreSyncTransactions(
                                    source = "force_sync",
                                    isFromForceSync = true,
                                    fromSyncScreen = false,
                                    req = null,
                                    businessId = _businessId
                                )
                            } else {
                                backendSyncTransactions(
                                    source = "force_sync",
                                    isFromForceSync = true,
                                    isFromSyncScreen = false,
                                    req = null,
                                    businessId = _businessId
                                )
                            }.andThen(
                                trackForceSyncCompleted(isCoreSync, res, _businessId)
                            )
                        }.doOnError {
                            RecordException.recordException(it)
                            coreTracker.get().trackForceSyncTransactionsError(isCoreSync, it)
                        }
                    }
            }
    }

    private fun getNumberOfTransactionsBeforeSync(
        isCoreSync: Boolean,
        businessId: String,
    ): Single<TrackForceSyncStartedResponse> {
        return if (isCoreSync) {
            coreSdk.get().lastUpdatedTransactionTime(businessId).flatMap { timestamp ->
                coreSdk.get().getNumberOfSyncTransactionsTillGivenUpdatedTime(timestamp.epoch, businessId).map {
                    TrackForceSyncStartedResponse(it, timestamp.epoch)
                }
            }
        } else {
            transactionRepo.get().lastUpdatedTransactionTime(businessId).flatMap { timestamp ->
                transactionRepo.get().getNumberOfSyncedTransactionsTillGivenUpdatedTime(timestamp, businessId).map {
                    TrackForceSyncStartedResponse(it, timestamp)
                }
            }
        }
    }

    private fun trackForceSyncCompleted(
        isCoreSync: Boolean,
        res: TrackForceSyncStartedResponse,
        businessId: String,
    ): Completable {
        if (isCoreSync) {
            return coreSdk.get().getNumberOfSyncTransactionsTillGivenUpdatedTime(res.maximumUpdateTime, businessId)
                .doAfterSuccess {
                    coreTracker.get()
                        .trackForceSyncTransactionsSuccess(
                            isCoreSync = true,
                            totalDifferenceInTransaction = it - res.noOfTransactions
                        )
                }.ignoreElement()
        } else {
            return transactionRepo.get()
                .getNumberOfSyncedTransactionsTillGivenUpdatedTime(res.maximumUpdateTime, businessId)
                .flatMapCompletable {
                    coreTracker.get().trackForceSyncTransactionsSuccess(
                        isCoreSync = false,
                        totalDifferenceInTransaction = it - res.noOfTransactions
                    )

                    return@flatMapCompletable Completable.complete()
                }
        }
    }

    data class TrackForceSyncStartedResponse(
        val noOfTransactions: Int,
        val maximumUpdateTime: Long,
    )

    private fun coreSyncTransactions(
        source: String,
        req: BehaviorSubject<SyncState>?,
        fromSyncScreen: Boolean,
        isFromForceSync: Boolean,
        businessId: String,
    ): Completable {
        return coreSdk.get()
            .syncTransactions(
                source = source,
                req = req,
                isFromSyncScreen = fromSyncScreen,
                isFromForceSync = isFromForceSync,
                businessId = businessId
            )
    }

    internal fun backendSyncTransactions(
        source: String,
        req: BehaviorSubject<SyncState>? = null,
        isFromSyncScreen: Boolean = false,
        isFromForceSync: Boolean = false,
        businessId: String,
    ): Completable {
        timeStartInSec = System.currentTimeMillis()
        traceSync.start()
        return syncDirtyTransactions.get().execute()
            .doOnError {
                RecordException.recordException(it)
                tracker.get().trackError("SyncScreen", "SyncDirtyTransactionsApiError", it.message ?: "", "")
            }
            .doOnComplete {
                traceSync.putMetric("SyncDirtyTxnsTime", System.currentTimeMillis().minus(timeStartInSec))
            }.andThen(
                getLastSyncTimeDetails(businessId).flatMapCompletable { lastSyncDetails ->
                    Timber.v("$TAG Last sync time = ${lastSyncDetails.lastSyncTime} isCompletedOnce=${lastSyncDetails.isSyncCompletedOnce}")
                    if (!lastSyncDetails.isSyncCompletedOnce && !isFromSyncScreen) {
                        Timber.v("$TAG returned")
                        return@flatMapCompletable Completable.complete()
                    }

                    var clearExistingTransactions = Completable.complete()
                    if (lastSyncDetails.lastSyncTime == -1L) {
                        clearExistingTransactions = transactionRepo.get().clear(businessId)
                        traceSync.putAttribute("AuthSync", "true")
                    } else {
                        traceSync.putAttribute("AuthSync", "false")
                    }
                    traceSync.putMetric("SyncDirtyTxnsTime", System.currentTimeMillis().minus(timeStartInSec))
                    val lastSyncTime = if (isFromForceSync) 0L else lastSyncDetails.lastSyncTime
                    clearExistingTransactions.andThen(
                        serverV2.get().getCustomerTransactions(lastSyncTime, source, businessId)
                            .flatMapCompletable { response ->
                                Timber.v("$TAG getCustomerTransactions Request Completed. { type = ${response.type}  file_data_status = ${response.file_data?.txn_file?.status}   }")
                                traceSync.putMetric("SyncApiCallTime", System.currentTimeMillis().minus(timeStartInSec))
                                if (response.type == ApiMessagesV2.TYPE_LIST) {
                                    traceSync.putMetric("TxnsCount", response.list_data?.transactions?.size?.toLong()!!)
                                    traceSync.putAttribute("TypeFile", "false")
                                    val list = response.list_data?.transactions
                                        ?.map { it.toDomainModel(businessId) }
                                        ?.toMutableList()
                                    Timber.v("$TAG Saving ${list?.size} transactions")
                                    traceSync.putMetric(
                                        "SyncTotalTime",
                                        System.currentTimeMillis().minus(timeStartInSec)
                                    )
                                    traceSync.stop()
                                    if (isFromForceSync) {
                                        transactionRepo.get().putTransactionsWithIgnoreStrategy(list)
                                            .andThen(saveLastSyncTime(businessId))
                                    } else {
                                        transactionRepo.get().putTransactionsV2(list)
                                            .andThen(saveLastSyncTime(businessId))
                                    }
                                } else if (response.type == ApiMessagesV2.TYPE_FILE && response.file_data?.txn_file?.status == ApiMessagesV2.FILE_DOWNLOAD_IN_PROGRESS) {
                                    req?.onNext(SyncState.GENERATE_FILE)
                                    traceSync.putAttribute("TypeFile", "true")
                                    tracker.get().trackSyncTransactions("1_started", 0)
                                    Timber.v("$TAG Getting transaction file")
                                    serverV2.get().getTransactionFile(response.file_data.txn_file.id, businessId)
                                        .repeatWhen {
                                            it.delay(2, TimeUnit.SECONDS)
                                        }
                                        .takeUntil {
                                            it.txn_file.status == ApiMessagesV2.FILE_DOWNLOAD_COMPLETED
                                        }
                                        .filter {
                                            Timber.v("$TAG latest file status=${it.txn_file.status}")
                                            it.txn_file.status == ApiMessagesV2.FILE_DOWNLOAD_COMPLETED
                                        }
                                        .flatMapCompletable {
                                            tracker.get().trackSyncTransactions("2_received_url", 0)
                                            Timber.v("$TAG Getting transaction file")
                                            syncTransactionsFromFile(req, it.txn_file, isFromForceSync, businessId)
                                        }
                                } else if (response.type == ApiMessagesV2.TYPE_FILE && response.file_data?.txn_file?.status == ApiMessagesV2.FILE_DOWNLOAD_COMPLETED) {
                                    syncTransactionsFromFile(
                                        req,
                                        response.file_data.txn_file,
                                        isFromForceSync,
                                        businessId
                                    )
                                } else {
                                    Completable.complete()
                                }
                            }
                    )
                }
            )
    }

    fun schedule(source: String, businessId: String): Completable {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMapCompletable {
                if (it) {
                    coreSchedule(source, businessId)
                } else {
                    backendSchedule(source, businessId)
                }
            }
    }

    private fun coreSchedule(source: String, businessId: String): Completable {
        return coreSdk.get().scheduleSyncTransactions(source = source, businessId)
    }

    fun backendSchedule(source: String, businessId: String): Completable {
        return Completable
            .fromAction {

                val workName = "sync_transactions"

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val workRequest = OneTimeWorkRequest.Builder(Worker::class.java)
                    .addTag(workName)
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
                    .setInputData(
                        workDataOf(
                            Worker.SOURCE to source,
                            Worker.SOURCE to businessId
                        )
                    )
                    .build()

                LogUtils.enableWorkerLogging(workRequest)

                workManager
                    .get()
                    .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.KEEP, workRequest)
            }
            .subscribeOn(ThreadUtils.newThread())
    }

    private fun getLastSyncTimeDetails(businessId: String): Single<LastSyncTimeResponse> {
        return Single.zip(
            transactionRepo.get().lastUpdatedTransactionTime(businessId),
            transactionsSyncService.get().getLastSyncTime(businessId),
            BiFunction<Long, Long, LastSyncTimeResponse>
            { lastUpdatedTransactionTime, lastSyncTime -> findLastSyncTime(lastUpdatedTransactionTime, lastSyncTime) }
        ).flatMap {
            return@flatMap Single.just(it)
        }
    }

    data class LastSyncTimeResponse(
        val lastSyncTime: Long,
        val isSyncCompletedOnce: Boolean,
    )

    private fun findLastSyncTime(lastUpdatedTransactionTime: Long, lastSyncTime: Long): LastSyncTimeResponse {
        // lastSyncTime will be `0` if it never sync at least once
        Timber.d("$TAG lastUpdatedTransactionTime= $lastUpdatedTransactionTime lastSyncTime=$lastSyncTime")
        return if (lastSyncTime == 0L) {
            LastSyncTimeResponse(0L, false)
        } else {
            LastSyncTimeResponse(lastUpdatedTransactionTime, true)
        }
    }

    private fun saveLastSyncTime(businessId: String): Completable {
        Timber.v("$TAG updating lastSyncTime = ${DateTimeUtils.currentDateTime()}")
        return transactionsSyncService.get().setLastSyncTime(DateTimeUtils.currentDateTime(), businessId)
    }

    private fun syncTransactionsFromFile(
        req: BehaviorSubject<SyncState>?,
        txnFileResponse: ApiMessagesV2.TransactionFile,
        isFromForceSync: Boolean,
        businessId: String,
    ): Completable {
        traceSync.putMetric("PopulateTxnsFileTime", System.currentTimeMillis().minus(timeStartInSec))

        return downloadFile(txnFileResponse.file, req, businessId).doOnComplete {
            tracker.get().trackSyncTransactions("3_download", 0)
            traceSync.putMetric("DownloadTxnsFileTime", System.currentTimeMillis().minus(timeStartInSec))
            Timber.v("$TAG file downloaded")
        }
            .andThen(
                decrypt(req, txnFileResponse.encryption_key, businessId)
                    .subscribeOn(ThreadUtils.computation())
                    .flatMapCompletable {
                        Timber.d(TAG + " Emitted ${it.size} transactions")
                        val list = it.map { it.toDomainModel(businessId) }.toMutableList()
                        Timber.d(TAG + " Converted DB object ${list.size} transactions")
                        if (isFromForceSync) {
                            transactionRepo.get().putTransactionsWithIgnoreStrategy(list)
                                .subscribeOn(ThreadUtils.database())
                        } else {
                            transactionRepo.get().putTransactionsV2(list).subscribeOn(ThreadUtils.database())
                        }
                    }
            ).doOnError {
                RecordException.recordException(it)
                Timber.d("%s Error: Saved transactions ${it.message}", TAG)
            }
            .doOnComplete { Timber.d("%s Saved transactions", TAG) }
            .andThen(clearFile(businessId))
            .doOnComplete {
                tracker.get().trackSyncTransactions("6_completed", 0)
                traceSync.putMetric("SyncTotalTime", System.currentTimeMillis().minus(timeStartInSec))
                traceSync.stop()
            }
            .andThen(saveLastSyncTime(businessId)).subscribeOn(ThreadUtils.database())
    }

    private fun getTransactionsFileName(businessId: String) = "$businessId$DOWNLOADED_FILE_NAME"

    private fun clearFile(businessId: String): Completable {
        return Completable.create {
            val destinationFolder = File(context.get().filesDir, getTransactionsFileName(businessId))
            destinationFolder.delete()
            it.onComplete()
        }
    }

    private fun downloadFile(transaction_file: String?, req: BehaviorSubject<SyncState>?, businessId: String): Completable {
        Timber.v("$TAG started downloading file $transaction_file")
        return Completable.create {
            context.get().startService(Intent(context.get(), TransferService::class.java))
            val fileToBeDownloaded = URI(transaction_file)
            val s3URI = AmazonS3URI(fileToBeDownloaded)
            val destinationFolder = File(context.get().filesDir, getTransactionsFileName(businessId))

            Timber.d(TAG + " File Key is ${s3URI.key}")
            val observer = transferUtility.get().download(s3URI.bucket, s3URI.key, destinationFolder)
            observer.setTransferListener(object : TransferListener, Serializable {
                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    val percentage = (((bytesCurrent * 100) / bytesTotal))
                    Timber.d(TAG + "onProgressChanged: id=$id percentage=$percentage bytesCurrent=$bytesCurrent bytesTotal=$bytesTotal")
                    req?.onNext(SyncState.DOWNLOADING)
                }

                override fun onStateChanged(id: Int, state: TransferState) {
                    Timber.d(TAG + "onStateChanged: id=$id name=${state?.name} state=$state")
                    if (state == TransferState.WAITING_FOR_NETWORK) {
                        req?.onNext(SyncState.NETWORK_ERROR)
                    } else if (state == TransferState.IN_PROGRESS) {
                        req?.onNext(SyncState.DOWNLOADING)
                    } else if (state == TransferState.COMPLETED) {
                        req?.onNext(SyncState.PROCESSING)
                        it.onComplete()
                    } else if (state == TransferState.FAILED) {
                        tracker.get().trackSyncError(true, "Download_Failed", state.name, "")
                        req?.onNext(SyncState.FILE_DOWONLOAD_ERROR)
                        it.onComplete()
                    }
                }

                override fun onError(id: Int, ex: Exception) {
                    tracker.get().trackSyncError(true, "Download_Failed", ex.message, ex.getStringStackTrace())
                    Timber.d(TAG + " onError: id=$id error=${ex?.message}")
                    req?.onNext(SyncState.FILE_DOWONLOAD_ERROR)
                    it.onError(ex.cause ?: ex)
                }
            })
        }
    }

    private fun decrypt(
        req: BehaviorSubject<SyncState>?,
        encryptionKey: String?,
        businessId: String,
    ): Observable<List<ApiMessagesV2.Transaction>> {
        return Observable.create {
            try {
                Timber.v("$TAG started decryption $encryptionKey")
                val fis = FileInputStream(File(context.get().filesDir, getTransactionsFileName(businessId)).path)

                val data = IOUtils.toByteArray(fis)
                val ivSpec = IvParameterSpec(data, 0, 16)
                val key = decodeHexString(encryptionKey!!)
                val secretKey = SecretKeySpec(key, "AES")
                val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

                val decryptedText = cipher.doFinal(data, 16, data.size - 16)
                Timber.v("$TAG ended decryption")
                val decompressGzipByteArray = CompressionUtil.decompressGzipByteArray(decryptedText)
                Timber.v("$TAG ended decompression")
                val byteArray = String(decompressGzipByteArray, Charset.forName("UTF-8"))
                Timber.v("$TAG ended conversion byteArray")
                val lines = byteArray.split(System.getProperty("line.separator") ?: "")

                tracker.get().trackSyncTransactions("4_decrypt", lines.size)

                traceSync.putMetric("DecryptAndDeCompressFileTime", System.currentTimeMillis().minus(timeStartInSec))
                traceSync.putMetric("TxnsCount", lines.size.toLong())

                val txns = arrayListOf<ApiMessagesV2.Transaction>()
                val transactionCount = firebaseRemoteConfig.get().getLong(TRANSACTION_PROCESSING_LIMIT_KEY).toInt()
                for (line in lines) {
                    val txn = Gson().fromJson(line, ApiMessagesV2.Transaction::class.java)
                    if (txn?.id != null) {
                        tracker.get().trackSyncTransactions("5_emitted_txns", lines.size)
                        txns.add(txn)
                    }

                    if (txns.size % transactionCount == 0) {
                        it.onNext(txns)
                        txns.clear()
                    }
                }

                traceSync.putMetric("GsonAndSaveDBTime", System.currentTimeMillis().minus(timeStartInSec))
                it.onNext(txns)
                it.onComplete()
            } catch (ex: Exception) {
                tracker.get().trackSyncError(true, "Decrypt_Failed", ex.message, ex.getStringStackTrace())
                Timber.d(TAG + "Error " + ex.message)
                req?.onNext(SyncState.SYNC_GENERIC_ERROR)
                it.onError(ex.cause ?: ex)
            }
        }
    }

    fun decodeHexString(hexString: String): ByteArray {
        require(hexString.length % 2 != 1) { "Invalid hexadecimal String supplied." }

        val bytes = ByteArray(hexString.length / 2)
        var i = 0
        while (i < hexString.length) {
            bytes[i / 2] = hexToByte(hexString.substring(i, i + 2))
            i += 2
        }
        return bytes
    }

    private fun hexToByte(hexString: String): Byte {
        val firstDigit = toDigit(hexString[0])
        val secondDigit = toDigit(hexString[1])
        return ((firstDigit shl 4) + secondDigit).toByte()
    }

    private fun toDigit(hexChar: Char): Int {
        val digit = Character.digit(hexChar, 16)
        require(digit != -1) { "Invalid Hexadecimal Character: $hexChar" }
        return digit
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncTransactionsImpl: Lazy<SyncTransactionsImpl>,
    ) : BaseRxWorker(context, params) {

        companion object {
            const val SOURCE = "source"
            const val BUSINESS_ID = "business-id"
        }

        override fun doRxWork(): Completable {
            val source = inputData.getString(SOURCE) ?: ""
            val businessId = inputData.getString(BUSINESS_ID)!!
            return syncTransactionsImpl.get().backendSyncTransactions(source, businessId = businessId)
        }

        class Factory @Inject constructor(private val syncTransactionsImpl: Lazy<SyncTransactionsImpl>) :
            ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, syncTransactionsImpl)
            }
        }
    }
}
