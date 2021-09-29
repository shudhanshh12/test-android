package `in`.okcredit.fileupload.usecase

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.fileupload.utils.AnalyticsEvents
import `in`.okcredit.fileupload.utils.Constants.AWS_IDENTITY_POOL_ID
import `in`.okcredit.fileupload.utils.FileUtils
import `in`.okcredit.fileupload.utils.ISchedulerProvider
import android.content.Context
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.utils.onCompleteIfObserving
import tech.okcredit.android.base.utils.onErrorIfObserving
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import javax.inject.Inject

class IAwsServiceImp @Inject constructor(
    private val context: Context,
    private val fileUtils: FileUtils,
    private val tracker: Tracker,
    private val schedulers: ISchedulerProvider,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>
) : IAwsService {

    private var transferUtility: TransferUtility? = null

    /**
     *  initializes AWS with id, region and client
     */
    private fun initAwsSettings(context: Context) {
        val credentialsProvider = CognitoCachingCredentialsProvider(
            context,
            AWS_IDENTITY_POOL_ID,
            Regions.US_EAST_1
        )
        TransferNetworkLossHandler.getInstance(context)
        // TODO: Sync remove transferUtility
        val timeOut = firebaseRemoteConfig.get().getLong(TIMEOUT_DURATION_FOR_AWS).toInt()
        val clientConfiguration = ClientConfiguration()
        clientConfiguration.connectionTimeout = timeOut * 1000
        clientConfiguration.socketTimeout = timeOut * 1000
        val s3 = AmazonS3Client(credentialsProvider, clientConfiguration)
        transferUtility = TransferUtility.builder().s3Client(s3).context(context).build()
    }

    /**
     *  This uploads the copied local file (compressed) to s3 bucket
     *  on Complete state, the local file is deleted
     *  Analytics is sent to track the error state
     */

    override fun uploadFile(
        photoType: String?,
        remoteUrl: String,
        inputStream: InputStream,
        flowId: String
    ): Completable {
        val fileName = fileUtils.getFileName(remoteUrl)

        Timber.d("${UploadFileImpl.TAG} Upload File Started fileName=$fileName")

        return createLocalCopy(inputStream)
            .doOnSuccess {
                tracker.trackFileUpload("7.1", remoteUrl, flowId)
                Timber.d("${UploadFileImpl.TAG} Created LocalCopy fileName=${it.absolutePath}")
            }.doOnError {
                RecordException.recordException(it)
                tracker.trackFileUploadError("7.1", it)
            }
            .flatMapCompletable {
                val subject = PublishSubject.create<String>()
                if (transferUtility == null) {
                    initAwsSettings(context)
                }
                transferUtility!!.upload(IUploadFile.AWS_BUCKET_NAME, fileName, it)
                    .setTransferListener(object : TransferListener {
                        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                            Timber.v("${UploadFileImpl.TAG} onProgressChanged Percentage = ${(bytesCurrent * 100) / bytesTotal}")
                        }

                        override fun onStateChanged(id: Int, state: TransferState?) {
                            tracker.trackFileUploadAwsUploaderChanges(state?.name, remoteUrl, flowId)
                            Timber.d("${UploadFileImpl.TAG} State Changed to $state")
                            when (state) {
                                TransferState.COMPLETED -> {
                                    if (it.exists()) it.delete()
                                    subject.onCompleteIfObserving()
                                }

                                TransferState.FAILED -> {
                                    tracker.track(AnalyticsEvents.TX_UPLOAD_RECEIPT_FAILED)
                                    subject.onErrorIfObserving(java.lang.RuntimeException("upload $photoType failed"))
                                }

                                TransferState.WAITING_FOR_NETWORK -> {
                                    subject.onErrorIfObserving(java.lang.RuntimeException("upload $photoType failed"))
                                }
                            }
                        }

                        override fun onError(id: Int, ex: Exception?) {
                            Timber.e("${UploadFileImpl.TAG} Upload Error ${ex?.message}")
                            tracker.track(AnalyticsEvents.TX_UPLOAD_RECEIPT_FAILED)
                            subject.onErrorIfObserving(ex as Throwable)
                        }
                    })
                subject.ignoreElements()
            }
            .subscribeOn(schedulers.upload())
    }

    /**
     *  Contains info about Upload
     */
    data class FileInfo(
        var url: String?,
        var localFile: File?,
        var isLocal: Boolean = (localFile != null)
    )

    /**
     *  This creates a local copy of file with .jpg extension
     */

    override fun createLocalCopy(stream: InputStream): Single<File> {
        return Single.fromCallable {
            val tempFile = File(fileUtils.getAwsStorageDir(), UUID.randomUUID().toString() + ".jpg")

            val outputStream = FileOutputStream(tempFile)
            val bytes = ByteArray(1024)

            var bytesRead: Int
            while (stream.read(bytes).also { bytesRead = it } >= 0) {
                outputStream.write(bytes, 0, bytesRead)
            }

            stream.close()
            outputStream.close()
            tempFile
        }
    }

    override fun uploadAudioSampleFile(remoteUrl: String, file: File): Completable {
        val fileName = fileUtils.getFileName(remoteUrl)
        Timber.d("${UploadFileImpl.TAG} Upload Audio Sample File Started, fileName: $fileName, remoteUrl: $remoteUrl")

        val subject = PublishSubject.create<String>()
        if (transferUtility == null) {
            initAwsSettings(context)
        }
        transferUtility!!.upload(IUploadAudioSampleFile.AWS_AUDIO_SAMPLES_BUCKET_NAME, fileName, file)
            .setTransferListener(object : TransferListener {
                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}

                override fun onStateChanged(id: Int, state: TransferState?) {
                    Timber.d("${UploadFileImpl.TAG} State changed to $state")
                    when (state) {
                        TransferState.COMPLETED -> {
                            if (file.exists()) file.delete()
                            subject.onCompleteIfObserving()
                        }

                        TransferState.FAILED -> {
                            subject.onErrorIfObserving(java.lang.RuntimeException("upload failed"))
                        }

                        TransferState.WAITING_FOR_NETWORK -> {
                            subject.onErrorIfObserving(java.lang.RuntimeException("upload failed"))
                        }
                    }
                }

                override fun onError(id: Int, ex: Exception?) {
                    Timber.e("${UploadFileImpl.TAG} Upload Error ${ex?.message}")
                    subject.onErrorIfObserving(ex as Throwable)
                }
            })
        return subject.ignoreElements()
    }

    companion object {
        const val TIMEOUT_DURATION_FOR_AWS = "timeout_duration_for_aws"
    }
}
