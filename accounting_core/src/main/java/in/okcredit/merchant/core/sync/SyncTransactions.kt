package `in`.okcredit.merchant.core.sync

import `in`.okcredit.accounting_core.contract.SyncState
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdkImpl
import `in`.okcredit.merchant.core._di.Core
import `in`.okcredit.merchant.core.analytics.CoreTracker
import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.common.toTimestamp
import `in`.okcredit.merchant.core.server.CoreRemoteSource
import `in`.okcredit.merchant.core.server.internal.CoreApiMessages
import `in`.okcredit.merchant.core.server.internal.CoreApiMessages.Companion.FILE_CREATION_IN_PROGRESS
import `in`.okcredit.merchant.core.server.internal.CoreApiMessages.TransactionFile
import `in`.okcredit.merchant.core.server.toTransaction
import `in`.okcredit.merchant.core.store.CoreLocalSource
import `in`.okcredit.merchant.core.sync.SyncTransactions.Worker.Companion.TRANSACTION_PROCESSING_LIMIT_KEY
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
import com.squareup.moshi.Moshi
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import org.apache.commons.io.IOUtils
import org.apache.commons.jcs.utils.zip.CompressionUtil
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_LAST_TRANSACTION_SYNC_TIME
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.utils.getStringStackTrace
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.base.Traces
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class SyncTransactions @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
    private val localSource: Lazy<CoreLocalSource>,
    private val remoteSource: Lazy<CoreRemoteSource>,
    private val tracker: Lazy<CoreTracker>,
    private val syncTransactionsCommands: Lazy<SyncTransactionsCommands>,
    private val transferUtility: Lazy<TransferUtility>,
    private val rxPref: Lazy<DefaultPreferences>,
    private val context: Lazy<Context>,
    @Core private val moshi: Lazy<Moshi>,
    firebasePerformance: Lazy<FirebasePerformance>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : CoreTransactionSyncer {
    init {
        Timber.i("$TAG init")
    }

    companion object {
        @NonNls
        const val WORKER_TAG_BASE = "core"

        @NonNls
        const val WORKER_TAG_SYNC = "core/sync"

        @NonNls
        const val TAG = "${CoreSdkImpl.TAG}/CoreSyncer"

        @NonNls
        const val DOWNLOADED_FILE_NAME = "transactions2.gz"
    }

    private var timeStartInSec: Long = System.currentTimeMillis()
    private val traceSync = firebasePerformance.get().newTrace(Traces.Trace_CoreModule_SyncTxsPerformance)

    override fun execute(
        source: String,
        req: BehaviorSubject<SyncState>?,
        isFromSyncScreen: Boolean,
        isFromForceSync: Boolean,
        businessId: String?,
    ): Completable {
        timeStartInSec = System.currentTimeMillis()
        traceSync.start()
        val flowId = UUID.randomUUID().toString()
        return syncTransactionsCommands.get().execute(businessId)
            .doOnError {
                RecordException.recordException(it)
                tracker.get().trackSyncTransactionCommandsError(
                    "Sync commands generic error", flowId, it?.message, it?.getStringStackTrace()
                )
            }.doOnComplete {
                Timber.v("$TAG sync commands completed")
                traceSync.putMetric("SyncDirtyTxnsTime", System.currentTimeMillis().minus(timeStartInSec))
            }
            .andThen(
                getActiveBusinessId.get().thisOrActiveBusinessId(businessId)
                    .flatMapCompletable { _businessId ->
                        getLastSyncTimeDetails(_businessId)
                            .flatMapCompletable { lastSyncDetails ->
                                Timber.v("$TAG Last sync time = ${lastSyncDetails.lastSyncTime}, isCompletedOnce=${lastSyncDetails.isSyncCompletedOnce}")
                                if (!lastSyncDetails.isSyncCompletedOnce && !isFromSyncScreen) {
                                    Timber.v("$TAG returned, sync is not completed once and is not from sync screen")
                                    return@flatMapCompletable Completable.complete()
                                }
                                var clearExistingTransactions = Completable.complete()
                                if (lastSyncDetails.lastSyncTime.epoch == -1L) {
                                    // Clean command table to prevent
                                    clearExistingTransactions = localSource.get().clearCommandTableForBusiness(_businessId)
                                        .andThen(localSource.get().clearTransactionTableForBusiness(_businessId))
                                    traceSync.putAttribute("AuthSync", "true")
                                } else {
                                    traceSync.putAttribute("AuthSync", "false")
                                }
                                traceSync.putMetric(
                                    "GetLastSyncTimeDetailsTime",
                                    System.currentTimeMillis().minus(timeStartInSec)
                                )
                                val lastSyncTime = if (isFromForceSync) 0L else lastSyncDetails.lastSyncTime.epoch
                                return@flatMapCompletable clearExistingTransactions.andThen(
                                    syncTransactions(
                                        source = source,
                                        flowId = flowId,
                                        lastSyncTime = lastSyncTime,
                                        req = req,
                                        businessId = _businessId
                                    )
                                )
                            }
                    }
            )
    }

    private fun syncTransactions(
        source: String,
        flowId: String,
        lastSyncTime: Long,
        req: BehaviorSubject<SyncState>?,
        businessId: String,
    ): Completable {
        return remoteSource.get().getTransactions(lastSyncTime, source, businessId)
            .flatMapCompletable { response ->
                Timber.v("$TAG getTransactions request completed. { type = ${response.type},  file_data_status = ${response.file_data?.txn_file?.status} }")
                traceSync.putMetric(
                    "SyncApiCallTime",
                    System.currentTimeMillis().minus(timeStartInSec)
                )
                if (response.type == CoreApiMessages.TYPE_LIST) {
                    traceSync.putMetric(
                        "TxnsCount",
                        response.list_data?.transactions?.size?.toLong()!!
                    )
                    traceSync.putAttribute("TypeFile", "false")
                    saveTransactionsIntoDatabase(response.list_data.transactions, businessId)
                } else if (response.type == CoreApiMessages.TYPE_FILE &&
                    response.file_data?.txn_file?.status == FILE_CREATION_IN_PROGRESS
                ) {
                    req?.onNext(SyncState.GENERATE_FILE)
                    traceSync.putAttribute("TypeFile", "true")
                    tracker.get()
                        .trackSyncTransactions("4-1_sync_file_started", flowId, source, "File")
                    Timber.v("$TAG Getting transaction file")
                    remoteSource.get().getTransactionFile(response.file_data.txn_file.id, businessId)
                        .repeatWhen {
                            it.delay(2, TimeUnit.SECONDS)
                        }
                        .takeUntil {
                            it.txn_file.status == CoreApiMessages.FILE_CREATION_COMPLETED
                        }
                        .filter {
                            Timber.v("$TAG latest file status=${it.txn_file.status}")
                            it.txn_file.status == CoreApiMessages.FILE_CREATION_COMPLETED
                        }
                        .flatMapCompletable {
                            tracker.get().trackSyncTransactions(
                                "4-2_received_url",
                                flowId,
                                source,
                                "File"
                            )
                            Timber.v("$TAG Getting transaction file")
                            syncTransactionsFromFile(req, it.txn_file, flowId, source, businessId)
                        }
                } else if (response.type == CoreApiMessages.TYPE_FILE && response.file_data?.txn_file?.status == CoreApiMessages.FILE_CREATION_COMPLETED) {
                    syncTransactionsFromFile(req, response.file_data.txn_file, flowId, source, businessId)
                } else {
                    Completable.complete()
                }
            }
    }

    private fun saveTransactionsIntoDatabase(
        transactions: List<CoreApiMessages.Transaction>?,
        businessId: String
    ): Completable {
        return transactions?.let {
            val list = it.map { transaction -> transaction.toTransaction() }
            Timber.v("$TAG Saving ${list.size} transactions to DB")
            traceSync.putMetric("SyncTotalTime", System.currentTimeMillis().minus(timeStartInSec))
            traceSync.stop()
            localSource.get().putTransactions(list, businessId)
                .andThen(saveLastSyncTime(businessId))
        } ?: Completable.complete()
    }

    // TODO Change following 2 functions after completing core module A/B (ref: HomePresenter::observeSyncStatusOnLoad())
    private fun saveLastSyncTime(businessId: String): Completable {
        return rxCompletable {
            rxPref.get()
                .set(PREF_BUSINESS_LAST_TRANSACTION_SYNC_TIME, System.currentTimeMillis(), Scope.Business(businessId))
        }.subscribeOn(ThreadUtils.database())
    }

    private fun getLastSyncTimeFromSharedPrefs(businessId: String): Single<Long> =
        rxPref.get().getLong(PREF_BUSINESS_LAST_TRANSACTION_SYNC_TIME, Scope.Business(businessId))
            .asObservable()
            .firstOrError()
            .subscribeOn(ThreadUtils.database())

    private fun getLastSyncTimeDetails(businessId: String): Single<LastSyncTimeResponse> {
        return Single.zip(
            getLastSyncTimeFromSharedPrefs(businessId),
            localSource.get().lastUpdatedTransactionTime(businessId),
            BiFunction { sharedPrefsLastSyncedTime, databaseLastUpdatedTime ->
                return@BiFunction if (sharedPrefsLastSyncedTime == 0L)
                    LastSyncTimeResponse(0L.toTimestamp(), false)
                else LastSyncTimeResponse(databaseLastUpdatedTime, true)
            }
        )
    }

    data class LastSyncTimeResponse(val lastSyncTime: Timestamp, val isSyncCompletedOnce: Boolean)

    private fun syncTransactionsFromFile(
        req: BehaviorSubject<SyncState>?,
        txnFileResponse: TransactionFile,
        flowId: String,
        source: String,
        businessId: String
    ): Completable {
        traceSync.putMetric("PopulateTxnsFileTime", System.currentTimeMillis().minus(timeStartInSec))
        return downloadFile(txnFileResponse.file, req, flowId, source, businessId).doOnComplete {
            tracker.get().trackSyncTransactions("4-3_download", flowId, source, "File")
            traceSync.putMetric("DownloadTxnsFileTime", System.currentTimeMillis().minus(timeStartInSec))
            Timber.v("$TAG file downloaded")
        }
            .andThen(
                decrypt(req, txnFileResponse.encryption_key, flowId, source, businessId)
                    .subscribeOn(ThreadUtils.computation())
                    .flatMapCompletable {
                        Timber.v("$TAG Emitted ${it.size} transactions")
                        val list = it.map { transaction -> transaction.toTransaction() }
                        Timber.v("$TAG Converted DB object ${list.size} transactions")
                        localSource.get().putTransactions(list, businessId)
                    }
            ).doOnError {
                RecordException.recordException(it)
                Timber.d("%s Error: Saved transactions ${it.message}", TAG)
            }
            .doOnComplete {
                tracker.get().trackSyncTransactions("4-6_saved", flowId, source, "File")
                Timber.d("%s Saved transactions", TAG)
            }
            .andThen(clearFile(businessId))
            .andThen(saveLastSyncTime(businessId))
            .doOnComplete {
                tracker.get().trackSyncTransactions("5_Completed", flowId, source, "File")
                traceSync.putMetric("SyncTotalTime", System.currentTimeMillis().minus(timeStartInSec))
                traceSync.stop()
            }
            .subscribeOn(ThreadUtils.database())
    }

    private fun getTransactionsFileName(businessId: String) = "$businessId$DOWNLOADED_FILE_NAME"

    private fun clearFile(businessId: String): Completable {
        return Completable.create {
            val destinationFolder = File(context.get().filesDir, getTransactionsFileName(businessId))
            destinationFolder.delete()
            it.onComplete()
        }
    }

    private fun downloadFile(
        transactionFile: String?,
        req: BehaviorSubject<SyncState>?,
        flowId: String,
        source: String,
        businessId: String,
    ): Completable {
        Timber.v("$TAG started downloading file $transactionFile")
        return Completable.create {
            context.get().startService(Intent(context.get(), TransferService::class.java))
            val fileToBeDownloaded = URI(transactionFile)
            val s3URI = AmazonS3URI(fileToBeDownloaded)
            val destinationFolder = File(context.get().filesDir, getTransactionsFileName(businessId))

            Timber.d("$TAG File Key is ${s3URI.key}")
            val observer = transferUtility.get().download(s3URI.bucket, s3URI.key, destinationFolder)
            observer.setTransferListener(object : TransferListener, java.io.Serializable {
                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    val percentage = (((bytesCurrent * 100) / bytesTotal))
                    Timber.v("$TAG onProgressChanged: id=$id percentage=$percentage bytesCurrent=$bytesCurrent bytesTotal=$bytesTotal")
                    req?.onNext(SyncState.DOWNLOADING)
                }

                override fun onStateChanged(id: Int, state: TransferState) {
                    Timber.d("$TAG onStateChanged: id=$id name=${state.name} state=$state")
                    when (state) {
                        TransferState.WAITING_FOR_NETWORK -> {
                            req?.onNext(SyncState.NETWORK_ERROR)
                        }
                        TransferState.IN_PROGRESS -> {
                            req?.onNext(SyncState.DOWNLOADING)
                        }
                        TransferState.COMPLETED -> {
                            req?.onNext(SyncState.PROCESSING)
                            it.onComplete()
                        }
                        TransferState.FAILED -> {
                            tracker.get().trackSyncTransactionError(true, "Download_Failed", flowId, state.name, "")
                            req?.onNext(SyncState.FILE_DOWONLOAD_ERROR)
                            it.onComplete()
                        }
                        else -> {
                            tracker.get().trackDebug(
                                CoreTracker.DebugType.FILE_DOWNLOAD_STATE,
                                "state:[$state.name] id:[$flowId] source:[$source]"
                            )
                        }
                    }
                }

                override fun onError(id: Int, ex: Exception) {
                    tracker.get().trackSyncTransactionError(
                        true, "Download_Failed", flowId, ex.message, ex.getStringStackTrace()
                    )
                    Timber.d("$TAG onError: id= $id, error= ${ex.message}")
                    req?.onNext(SyncState.FILE_DOWONLOAD_ERROR)
                    it.onError(ex.cause ?: ex)
                }
            })
        }
    }

    private fun decrypt(
        req: BehaviorSubject<SyncState>?,
        encryptionKey: String?,
        flowId: String,
        source: String,
        businessId: String,
    ): Observable<List<CoreApiMessages.Transaction>> {
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
                val lines = byteArray.split(System.getProperty("line.separator") ?: "\n")

                tracker.get().trackSyncTransactions("4-4_decrypt", flowId, source, "File", lines.size)
                traceSync.putMetric(
                    "DecryptAndDeCompressFileTime",
                    System.currentTimeMillis().minus(timeStartInSec)
                )
                traceSync.putMetric("TxnsCount", lines.size.toLong())

                val txns = arrayListOf<CoreApiMessages.Transaction>()
                val adapter = moshi.get().adapter(CoreApiMessages.Transaction::class.java)
                val transactionProcessingCount =
                    firebaseRemoteConfig.get().getLong(TRANSACTION_PROCESSING_LIMIT_KEY).toInt()
                for (line in lines) {
                    if (line.isEmpty()) continue
                    val txn = adapter.fromJson(line)
                    if (txn?.id != null) {
                        txns.add(txn)
                    }
                    if (txns.size >= transactionProcessingCount) {
                        it.onNext(txns)
                        txns.clear()
                    }
                }
                tracker.get().trackSyncTransactions("4-5_emitted_txns", flowId, source, "File", lines.size)
                traceSync.putMetric("GsonAndSaveDBTime", System.currentTimeMillis().minus(timeStartInSec))
                it.onNext(txns)
                it.onComplete()
            } catch (ex: Exception) {
                tracker.get()
                    .trackSyncTransactionError(true, "Decrypt_Failed", flowId, ex.message, ex.getStringStackTrace())
                req?.onNext(SyncState.SYNC_GENERIC_ERROR)
                it.onError(ex.cause ?: ex)
            }
        }
    }

    private fun decodeHexString(hexString: String): ByteArray {
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

    override fun schedule(source: String, businessId: String): Completable {
        return Completable
            .fromAction {
                val workName = WORKER_TAG_SYNC
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val workRequest = OneTimeWorkRequest.Builder(Worker::class.java)
                    .addTag(WORKER_TAG_BASE)
                    .addTag(workName)
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        30, TimeUnit.SECONDS
                    )
                    .setInputData(
                        Data.Builder()
                            .putString(Worker.SOURCE, source)
                            .putString(Worker.BUSINESS_ID, businessId)
                            .build()
                    )
                    .build()
                    .enableWorkerLogging()

                workManager.get()
                    .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.KEEP, workRequest)
            }
            .subscribeOn(ThreadUtils.newThread())
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncer: Lazy<CoreTransactionSyncer>,
    ) : BaseRxWorker(context, params) {
        companion object {
            const val SOURCE = "source"
            const val BUSINESS_ID = "business_id"
            const val TRANSACTION_PROCESSING_LIMIT_KEY = "transaction_processing_limit"
        }

        override fun doRxWork(): Completable {
            val source = inputData.getString(SOURCE) ?: ""
            val businessId = inputData.getString(BUSINESS_ID)
            return syncer.get().execute(
                source = source,
                businessId = businessId
            )
        }

        class Factory @Inject constructor(private val syncer: Lazy<CoreTransactionSyncer>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, syncer)
            }
        }
    }
}
