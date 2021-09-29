package `in`.okcredit.fileupload.usecase

import `in`.okcredit.fileupload.analytics.FileUploadTracker
import `in`.okcredit.fileupload.analytics.FileUploadTracker.Event.UPLOAD_AUDIO_SUCCESS
import `in`.okcredit.fileupload.analytics.FileUploadTracker.Event.UPLOAD_AUDIO_WORKER_SCHEDULED
import `in`.okcredit.fileupload.analytics.FileUploadTracker.Event.UPLOAD_AUDIO_WORKER_STARTED
import `in`.okcredit.fileupload.utils.Constants.FILE_UPLOAD
import `in`.okcredit.fileupload.utils.FileUtils
import `in`.okcredit.fileupload.utils.ISchedulerProvider
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.work.*
import com.amazonaws.util.IOUtils
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.itOrBlank
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.android.base.workmanager.WorkerConfig
import timber.log.Timber
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.HashMap

class UploadAudioSampleFileImpl @Inject constructor(
    private val awsService: Lazy<IAwsService>,
    private val fileUtils: Lazy<FileUtils>,
    private val workManager: Lazy<OkcWorkManager>,
    private val schedulers: Lazy<ISchedulerProvider>,
    private val context: Lazy<Context>,
    private val tracker: Lazy<FileUploadTracker>,
) : IUploadAudioSampleFile {

    companion object {
        private const val TAG = "UploadAudioSampleFileImpl"
        private const val REMOTE_URL = "RemoteUrl"
        private const val FILE_PATH = "FilePath"
    }

    override fun schedule(
        remoteUrl: String,
        localPath: String,
        trackerProperties: Map<String, String>,
    ): Completable {
        return Completable.fromAction {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val properties: Map<String, String> = HashMap<String, String>().apply {
                this[REMOTE_URL] = remoteUrl
                this[FILE_PATH] = localPath
                putAll(trackerProperties)
            }

            val workRequest = OneTimeWorkRequest.Builder(RxUploadWorker::class.java)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .setInputData(
                    Data.Builder()
                        .putAll(properties)
                        .build()
                )
                .build()
                .enableWorkerLogging()

            workManager.get()
                .schedule(FILE_UPLOAD.plus(remoteUrl), Scope.Individual, ExistingWorkPolicy.KEEP, workRequest)

            tracker.get().trackUploadAudioStatus(UPLOAD_AUDIO_WORKER_SCHEDULED, properties)
        }.subscribeOn(schedulers.get().newThread())
    }

    override fun createLocalFile(uri: Uri, remoteUrl: String) = Single.create<String> { emitter ->
        try {
            val stream = context.get().contentResolver.openInputStream(uri)
            stream?.let { inputStream ->
                val localFile = File(fileUtils.get().getAwsStorageDir(), fileUtils.get().getFileName(remoteUrl))
                val outputStream = FileOutputStream(localFile)
                IOUtils.copy(inputStream, outputStream)
                emitter.onSuccess(localFile.absolutePath)
            }
        } catch (e: Exception) {
            RecordException.recordException(e)
            emitter.onError(e)
        }
    }.subscribeOn(schedulers.get().newThread())

    class RxUploadWorker(
        context: Context,
        workerParams: WorkerParameters,
        private val uploadFile: Lazy<UploadAudioSampleFileImpl>,
        private val tracker: Lazy<FileUploadTracker>,
    ) :
        BaseRxWorker(context, workerParams, workerConfig = WorkerConfig(allowUnlimitedRun = true)) {

        @SuppressLint("RestrictedApi")
        override fun doRxWork(): Completable {
            val remoteUrl = inputData.getString(REMOTE_URL).itOrBlank()
            val filePath = inputData.getString(FILE_PATH).itOrBlank()
            val lang = Locale.getDefault().language
            val trackerProperties = HashMap<String, String>().apply {
                putAll(inputData.keyValueMap.mapValues { it.value.toString() })
                this[FileUploadTracker.Key.LANG] = lang
            }

            tracker.get().trackUploadAudioStatus(UPLOAD_AUDIO_WORKER_STARTED, trackerProperties)

            return uploadFile.get().execute(remoteUrl, filePath, trackerProperties)
        }

        class Factory @Inject constructor(
            private val uploadFile: Lazy<UploadAudioSampleFileImpl>,
            private val tracker: Lazy<FileUploadTracker>,
        ) :
            ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return RxUploadWorker(context, params, uploadFile, tracker)
            }
        }
    }

    fun execute(
        remoteUrl: String,
        filePath: String,
        trackerProperties: Map<String, String>,
    ): Completable {
        val file = File(filePath)
        return Single.just(file)
            .flatMap {
                if (it.exists().not()) {
                    Single.error(FileNotFoundException())
                } else {
                    Single.just(file)
                }
            }
            .subscribeOn(schedulers.get().computation())
            .flatMapCompletable {
                awsService.get().uploadAudioSampleFile(remoteUrl, it)
            }
            .subscribeOn(schedulers.get().upload())
            .doOnComplete {
                Timber.d("$TAG file upload completed, remoteUrl: $remoteUrl")
                tracker.get().trackUploadAudioStatus(UPLOAD_AUDIO_SUCCESS, trackerProperties)
            }
            .doOnError {
                RecordException.recordException(it)
                Timber.e("$TAG file upload error ${it.message}")
                tracker.get().trackUploadAudioError(it, trackerProperties)
            }
    }
}
