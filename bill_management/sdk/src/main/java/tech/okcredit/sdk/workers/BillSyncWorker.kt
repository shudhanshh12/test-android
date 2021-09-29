package tech.okcredit.sdk.workers

import `in`.okcredit.shared.utils.AbFeatures
import android.content.Context
import android.content.Intent
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
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
import kotlinx.coroutines.rx2.rxCompletable
import org.apache.commons.io.IOUtils
import org.apache.commons.jcs.utils.zip.CompressionUtil
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_LAST_TRANSACTION_SYNC_TIME
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.getStringStackTrace
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.base.Traces
import tech.okcredit.sdk.ApiEntityMapper
import tech.okcredit.sdk.DbEntityMapper
import tech.okcredit.sdk.analytics.BillTracker
import tech.okcredit.sdk.models.Ordering
import tech.okcredit.sdk.server.BillApiMessages
import tech.okcredit.sdk.server.BillRemoteSource
import tech.okcredit.sdk.store.BillLocalSource
import tech.okcredit.sdk.store.database.DbBillDoc
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

class BillSyncWorker(
    private val context: Lazy<Context>,
    params: WorkerParameters,
    private val billRemoteSourceImpl: Lazy<BillRemoteSource>,
    private val tracker: Lazy<BillTracker>,
    private val transferUtility: Lazy<TransferUtility>,
    private val billLocalSource: Lazy<BillLocalSource>,
    firebasePerformance: Lazy<FirebasePerformance>,
    private val rxPref: Lazy<DefaultPreferences>,
    private val moshi: Lazy<Moshi>,
    private val ab: Lazy<AbRepository>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
) : BaseRxWorker(context.get(), params) {

    private val traceSync = firebasePerformance.get().newTrace(Traces.Trace_BillModule_SyncTxsPerformance)

    companion object {
        const val TYPE_LIST = 0
        const val TYPE_FILE = 1

        const val FILE_CREATION_IN_PROGRESS = 0
        const val FILE_CREATION_COMPLETED = 1

        const val TAG = "BillSyncer"
        const val DOWNLOADED_FILE_NAME = "bill.gz"
        const val TRANSACTION_PROCESSING_LIMIT_KEY = "transaction_processing_limit"
        const val BUSINESS_ID = "business_id"
    }

    private var timeStartInSec: Long = System.currentTimeMillis()
    override fun doRxWork(): Completable {
        val source = ""
        val flowId = UUID.randomUUID().toString()
        val businessId = inputData.getString(BUSINESS_ID)!!
        return ab.get().isFeatureEnabled(AbFeatures.BILL_MANAGER, businessId = businessId).firstOrError()
            .flatMapCompletable {
                if (it) {
                    billLocalSource.get().getStartTime(businessId).flatMapCompletable {
                        billRemoteSourceImpl.get()
                            .getBills(
                                BillApiMessages.ListBillsRequest(it, false, Ordering.UPDATE_TIME.label),
                                businessId
                            )
                            .flatMapCompletable { response ->
                                tracker.get().trackSyncBills("3_Server_Call_Completed", flowId, source)
                                Timber.v("$TAG getTransactions request completed. { type = ${response.type},  file_data_status = ${response.file_data?.bill_file?.status} }")
                                traceSync.putMetric(
                                    "SyncApiCallTime",
                                    System.currentTimeMillis().minus(timeStartInSec)
                                )
                                if (response.type == TYPE_LIST) {
                                    tracker.get().trackSyncBills(
                                        "4-1_saving_into_database",
                                        flowId,
                                        source,
                                        "List"
                                    )
                                    traceSync.putMetric(
                                        "BillsCount",
                                        response.list_data?.billsList?.size?.toLong()!!
                                    )
                                    traceSync.putAttribute("TypeFile", "false")
                                    saveTransactionsIntoDatabase(
                                        response.list_data.billsList,
                                        flowId,
                                        source,
                                        businessId
                                    )
                                } else if (response.type == TYPE_FILE && response.file_data?.bill_file?.status == FILE_CREATION_IN_PROGRESS) {
                                    traceSync.putAttribute("TypeFile", "true")
                                    tracker.get().trackSyncBills("4-1_sync_file_started", flowId, source, "File")
                                    Timber.v("$TAG Getting transaction file")
                                    billRemoteSourceImpl.get()
                                        .getTransactionFile(response.file_data.bill_file.id, businessId)
                                        .repeatWhen {
                                            it.delay(2, TimeUnit.SECONDS)
                                        }
                                        .takeUntil {
                                            it.bill_file.status == FILE_CREATION_COMPLETED
                                        }
                                        .filter {
                                            Timber.v("$TAG latest file status=${it.bill_file.status}")
                                            it.bill_file.status == FILE_CREATION_COMPLETED
                                        }
                                        .flatMapCompletable {
                                            tracker.get().trackSyncBills(
                                                "4-2_received_url",
                                                flowId,
                                                source,
                                                "File"
                                            )
                                            Timber.v("$TAG Getting transaction file")
                                            syncTransactionsFromFile(it.bill_file, flowId, source, businessId)
                                        }
                                } else if (response.type == TYPE_FILE && response.file_data?.bill_file?.status == FILE_CREATION_COMPLETED) {
                                    syncTransactionsFromFile(response.file_data.bill_file, flowId, source, businessId)
                                } else {
                                    Completable.complete()
                                }
                            }
                    }
                } else {
                    Completable.complete()
                }
            }
    }

    private fun syncTransactionsFromFile(
        txnFileResponse: BillApiMessages.BillFile,
        flowId: String,
        source: String,
        businessId: String,
    ): Completable {
        traceSync.putMetric("PopulateTxnsFileTime", System.currentTimeMillis().minus(timeStartInSec))

        return downloadFile(txnFileResponse.file, flowId, source).doOnComplete {
            tracker.get().trackSyncBills("4-3_download", flowId, source, "File")
            traceSync.putMetric("DownloadTxnsFileTime", System.currentTimeMillis().minus(timeStartInSec))
            Timber.v("$TAG file downloaded")
        }
            .andThen(
                decrypt(txnFileResponse.encryption_key, flowId, source)
                    .subscribeOn(ThreadUtils.computation())
                    .flatMapCompletable {
//                    Timber.v("$TAG Emitted ${it.size} transactions")
//                    val list = it.map { transaction -> transaction.toTransaction() }
//                    Timber.v("$TAG Converted DB object ${list.size} transactions")
                        saveTransactionsIntoDatabase(it, flowId, source, businessId)
                    }
            ).doOnError {
                RecordException.recordException(it)
                Timber.d("%s Error: Saved transactions ${it.message}", TAG)
            }
            .doOnComplete {
                tracker.get().trackSyncBills("4-6_saved", flowId, source, "File")
                Timber.d("%s Saved transactions", TAG)
            }
            .andThen(clearFile())
            .andThen(saveLastSyncTime(businessId))
            .doOnComplete {
                tracker.get().trackSyncBills("5_Completed", flowId, source, "File")
                traceSync.putMetric("SyncTotalTime", System.currentTimeMillis().minus(timeStartInSec))
                traceSync.stop()
            }
            .subscribeOn(ThreadUtils.database())
    }

    private fun clearFile(): Completable {
        return Completable.create {
            val destinationFolder = File(context.get().filesDir, DOWNLOADED_FILE_NAME)
            destinationFolder.delete()
            it.onComplete()
        }
    }

    private fun downloadFile(
        transaction_file: String?,
        flowId: String,
        source: String,
    ): Completable {
        Timber.v("$TAG started downloading file $transaction_file")
        return Completable.create {
            context.get().startService(Intent(context.get(), TransferService::class.java))
            val fileToBeDownloaded = URI(transaction_file)
            val s3URI = AmazonS3URI(fileToBeDownloaded)
            val destinationFolder = File(context.get().filesDir, DOWNLOADED_FILE_NAME)

            Timber.d("$TAG File Key is ${s3URI.key}")
            val observer = transferUtility.get().download(s3URI.bucket, s3URI.key, destinationFolder)
            observer.setTransferListener(object : TransferListener, java.io.Serializable {
                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    val percentage = (((bytesCurrent * 100) / bytesTotal))
                    Timber.v("$TAG onProgressChanged: id=$id percentage=$percentage bytesCurrent=$bytesCurrent bytesTotal=$bytesTotal")
                }

                override fun onStateChanged(id: Int, state: TransferState) {
                    Timber.d("$TAG onStateChanged: id=$id name=${state.name} state=$state")
                    when (state) {
                        TransferState.WAITING_FOR_NETWORK -> {
                        }
                        TransferState.IN_PROGRESS -> {
                        }
                        TransferState.COMPLETED -> {
                            it.onComplete()
                        }
                        TransferState.FAILED -> {
                            tracker.get().trackSyncTransactionError(true, "Download_Failed", flowId, state.name, "")
                            it.onComplete()
                        }
                        else -> {
                            tracker.get().trackDebug(
                                BillTracker.DebugType.FILE_DOWNLOAD_STATE,
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
                    it.onError(ex.cause ?: ex)
                }
            })
        }
    }

    private fun saveTransactionsIntoDatabase(
        transactions: List<BillApiMessages.ServerBill>?,
        flowId: String,
        source: String,
        businessId: String,
    ): Completable {
        return if (transactions.isNullOrEmpty()) {
            Completable.complete()
        } else {
            Completable.fromAction {
                val list = transactions.map { serverBill -> ApiEntityMapper.BILLS.convert(serverBill) }
                list.forEach { localBill ->
                    DbEntityMapper.getBills(businessId).convert(localBill)?.let { bill ->
                        val docList = localBill?.localBillDocList?.map { doc ->
                            DbBillDoc(
                                doc.billDocId,
                                doc.url,
                                doc.createdAt,
                                doc.updatedAt,
                                doc.deletedAt,
                                localBill.id,
                                businessId
                            )
                        }
                        billLocalSource.get().insertOrUpdateBills(bill, docList)
                    }
                }
                tracker.get().trackSyncBills("5_Completed", flowId, source, "List", list.size)
            }
        }
    }

    private fun saveLastSyncTime(businessId: String): Completable {
        return rxCompletable {
            rxPref.get()
                .set(PREF_BUSINESS_LAST_TRANSACTION_SYNC_TIME, System.currentTimeMillis(), Scope.Business(businessId))
        }.subscribeOn(ThreadUtils.database())
    }

    private fun decrypt(
        encryptionKey: String?,
        flowId: String,
        source: String,
    ): Observable<List<BillApiMessages.ServerBill>> {
        return Observable.create {
            try {
                Timber.v("$TAG started decryption $encryptionKey")
                val fis = FileInputStream(File(context.get().filesDir, DOWNLOADED_FILE_NAME).path)

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

                tracker.get().trackSyncBills("4-4_decrypt", flowId, source, "File", lines.size)
                traceSync.putMetric(
                    "DecryptAndDeCompressFileTime",
                    System.currentTimeMillis().minus(timeStartInSec)
                )
                traceSync.putMetric("BillsCount", lines.size.toLong())

                val txns = arrayListOf<BillApiMessages.ServerBill>()
                val adapter = moshi.get().adapter(BillApiMessages.ServerBill::class.java)
                val count = firebaseRemoteConfig.get().getLong(TRANSACTION_PROCESSING_LIMIT_KEY).toInt()
                for (line in lines) {
                    if (line.isEmpty()) continue
                    val txn = adapter.fromJson(line)
                    if (txn?.id != null) {
                        txns.add(txn)
                    }
                    if (txns.size >= count) {
                        it.onNext(txns)
                        txns.clear()
                    }
                }
                tracker.get().trackSyncBills("4-5_emitted_txns", flowId, source, "File", lines.size)
                traceSync.putMetric("GsonAndSaveDBTime", System.currentTimeMillis().minus(timeStartInSec))
                it.onNext(txns)
                it.onComplete()
            } catch (ex: Exception) {
                tracker.get()
                    .trackSyncTransactionError(true, "Decrypt_Failed", flowId, ex.message, ex.getStringStackTrace())
//                req?.onNext(SyncState.SYNC_GENERIC_ERROR)
                it.onError(ex.cause ?: ex)
            }
        }
    }

    class Factory @Inject constructor(
        private val ab: Lazy<AbRepository>,
        private val billRemoteSourceImpl: Lazy<BillRemoteSource>,
        private val billLocalSource: Lazy<BillLocalSource>,
        private val tracker: Lazy<BillTracker>,
        private val moshi: Lazy<Moshi>,
        private val transferUtility: Lazy<TransferUtility>,
        private val rxPref: Lazy<DefaultPreferences>,
        private val firebasePerformance: Lazy<FirebasePerformance>,
        private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    ) : ChildWorkerFactory {

        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return BillSyncWorker(
                { context },
                params,
                billRemoteSourceImpl = billRemoteSourceImpl,
                tracker = tracker,
                transferUtility = transferUtility,
                billLocalSource = billLocalSource,
                rxPref = rxPref,
                moshi = moshi,
                firebasePerformance = firebasePerformance,
                ab = ab,
                firebaseRemoteConfig = firebaseRemoteConfig
            )
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
}
