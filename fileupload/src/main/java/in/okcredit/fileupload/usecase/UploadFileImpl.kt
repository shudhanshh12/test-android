package `in`.okcredit.fileupload.usecase

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.fileupload.utils.Constants.FILE_UPLOAD
import `in`.okcredit.fileupload.utils.FileUtils
import `in`.okcredit.fileupload.utils.ISchedulerProvider
import `in`.okcredit.fileupload.utils.ImageUtil.compressedStream
import `in`.okcredit.fileupload.utils.ImageUtil.correctOrientation
import `in`.okcredit.fileupload.utils.ImageUtil.scale
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.work.*
import com.google.common.io.BaseEncoding
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.android.base.workmanager.WorkerConfig
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UploadFileImpl @Inject constructor(
    private val awsService: Lazy<IAwsService>,
    private val fileUtils: Lazy<FileUtils>,
    private val tracker: Lazy<Tracker>,
    private val workManager: Lazy<OkcWorkManager>,
    private val context: Lazy<Context>,
    private val schedulers: Lazy<ISchedulerProvider>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>
) : IUploadFile {

    /**
     *  1st call
     *  This is called by the uploading screen with @param remoteUrl, localPath and context
     *  If @param localPath file doesn't exist in storage , then with the help of it, photo is retrieved from
     *  'ContactProvider' ( @fun loadContactPhotoThumbnail)
     *
     *  @variable contactPhotoPath and absolutePath is retrieved from @fun loadContactPhotoThumbnail
     *  then , put in  @variable localFilePath
     *
     * The @variable localFilePath is written as local file in storage(aws-storage dir) and copied to @variable file
     *
     * Once @variable file is available in storage, it is (@param remoteUrl, localPath) given to WorkManager to upload
     * when Network is connected
     */

    companion object {
        const val TAG = "<<<<FileUpload"
        const val FILE_UPLOAD_SCALE_IMAGE_BITMAP_WIDTH_HEIGHT_KEY = "file_upload_scale_image_bitmap_width_height"
        const val FILE_UPLOAD_COMPRESSED_IMAGE_QUALITY = "file_upload_compressed_image_quality"
    }

    override fun schedule(photoType: String, remoteUrl: String, localPath: String): Completable {
        return Completable.fromAction {

            val flowId = UUID.randomUUID().toString() // Added for analysing funnels
            tracker.get().trackFileUpload("0", remoteUrl, flowId)

            Timber.d("$TAG Scheduling Started")
            var localFilePath = localPath
            var file = File(localFilePath)
            if (photoType == IUploadFile.CONTACT_PHOTO) {
                Timber.d("$TAG File Not exist in local path")
                tracker.get().trackFileUpload("0.1", remoteUrl, flowId)
                val contactPhotoPath = loadContactPhotoThumbnail(localFilePath, context.get())
                tracker.get().trackFileUpload("0.2", remoteUrl, flowId)
                val absolutePath = FileUtils.getPath(context.get(), Uri.parse(contactPhotoPath))
                val file1 = File(absolutePath)
                if (contactPhotoPath != null && file1.exists()) {
                    tracker.get().trackFileUpload("0.5", remoteUrl, flowId)
                    Timber.d("$TAG Contact File Exist")
                    file = file1
                    localFilePath = absolutePath.toString()
                }
            }

            tracker.get().trackFileUpload("1", remoteUrl, flowId)

            val localCopy = File(fileUtils.get().getAwsStorageDir(), fileUtils.get().getFileName(remoteUrl))
            file.copyTo(localCopy)

            tracker.get().trackFileUpload("2", remoteUrl, flowId)
            Timber.d("$TAG File Copied: file=${file.absolutePath} localCopy=${localCopy.absolutePath} remoteUrl=$remoteUrl")

            val fileSize = File(localFilePath).length() / 1024

            Timber.d("$TAG File Size=$fileSize")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequest.Builder(RxUploadWorker::class.java)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
                .setInputData(
                    Data.Builder()
                        .putString(RxUploadWorker.REMOTE_URL, remoteUrl)
                        .putString(RxUploadWorker.FILE_PATH, localFilePath)
                        .putString(RxUploadWorker.FILE_TYPE, photoType)
                        .putString(RxUploadWorker.FILE_SIZE, fileSize.toString())
                        .putString(RxUploadWorker.FLOW_ID, flowId)
                        .build()
                )
                .build()
                .enableWorkerLogging()

            workManager.get()
                .schedule(FILE_UPLOAD.plus(remoteUrl), Scope.Individual, ExistingWorkPolicy.KEEP, workRequest)
        }.subscribeOn(schedulers.get().newThread())
    }

    /**
     * 2nd call
     * This takes photo from contact provider with the help of @param photoData
     */

    override fun loadContactPhotoThumbnail(photoData: String, context: Context): String? {
        // Creates an asset file descriptor for the thumbnail file.
        var afd: AssetFileDescriptor? = null
        var outputStream: FileOutputStream? = null
        var inputStream: InputStream? = null
        // try-catch block for file not found
        try {
            // Creates a holder for the URI.
            val thumbUri: Uri = Uri.parse(photoData)
            // If Android 3.0 or later
            /*
             * Retrieves an AssetFileDescriptor object for the thumbnail
             * URI
             * using ContentResolver.openAssetFileDescriptor
             */
            afd = context.contentResolver.openAssetFileDescriptor(thumbUri, "r")

            val fdd = afd!!.fileDescriptor
            inputStream = FileInputStream(fdd)
            val file = File.createTempFile("PhoneContactProvider", "tmp")
            file.deleteOnExit()
            outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)

            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } >= 0) {
                outputStream.write(buffer, 0, bytesRead)
            }

            inputStream.close()
            outputStream.close()
            return "file://" + file.absolutePath
        } catch (e: Exception) {
            Timber.e(this.javaClass.simpleName, e.message)
        } finally {
            if (afd != null) {
                try {
                    afd.close()
                } catch (e: IOException) {
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                }
            }
        } // In all cases, close the asset file descriptor
        return null
    }

    /**
     * 3rd call
     *  This worker is responsible for calling @fun execute()
     *  If upload fails , then it retries automatically until it is success
     *
     */

    class RxUploadWorker(context: Context, workerParams: WorkerParameters, private val uploadFile: Lazy<IUploadFile>) :
        BaseRxWorker(context, workerParams, workerConfig = WorkerConfig(allowUnlimitedRun = true)) {

        @SuppressLint("RestrictedApi")
        override fun doRxWork(): Completable {
            val receiptUrl = inputData.getString(REMOTE_URL)
            val filePath = inputData.getString(FILE_PATH)
            val fileType = inputData.getString(FILE_TYPE)
            val flowId = inputData.getString(FLOW_ID)

            return when {
                this.inputData.size() == 0 -> Completable.fromCallable { Result.failure() }
                receiptUrl == null || filePath == null -> Completable.fromCallable { Result.failure() }
                else -> {
                    uploadFile.get().execute(fileType, receiptUrl, filePath, flowId ?: "")
                }
            }
        }

        class Factory @Inject constructor(private val uploadFile: Lazy<IUploadFile>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return RxUploadWorker(context, params, uploadFile)
            }
        }

        companion object {
            const val REMOTE_URL = "remote_url"
            const val FILE_PATH = "file_path"
            const val FILE_TYPE = "file_type"
            const val FILE_SIZE = "file_size"
            const val FLOW_ID = "flow_id"
        }
    }

    /**
     * 4rth call
     *  This does 1. orientation correction, 2. scaling, 3. compression of @variable file
     *  then , calls @class awsService for upload
     *  onError , it logs in Analytics
     */
    override fun execute(fileType: String?, remoteUrl: String, filePath: String, flowId: String): Completable {
        tracker.get().trackFileUpload("3", remoteUrl, flowId)
        val file = File(filePath)
        if (!file.exists()) {
            tracker.get().trackFileUploadError("3", null)
            return Completable.error(FileNotFoundException())
        }
        tracker.get().trackFileUpload("4", remoteUrl, flowId)

        return Single.just(file)
            .subscribeOn(schedulers.get().computation())
            .map {
                correctOrientation(it)
            }
            .doOnSuccess {
                tracker.get().trackFileUpload("5", remoteUrl, flowId)
                Timber.d("$TAG correctOrientation completed ${it.config}")
                Timber.d("$TAG started scale byteCount before=${it.byteCount}")
            }
            .doOnError {
                RecordException.recordException(it)
                tracker.get().trackFileUploadError("5", it)
                Timber.e("$TAG correctOrientation error ${it.message}")
            }
            .map {
                val maxWidthAndHeight =
                    firebaseRemoteConfig.get().getLong(FILE_UPLOAD_SCALE_IMAGE_BITMAP_WIDTH_HEIGHT_KEY).toInt()
                scale(it, maxWidthAndHeight, maxWidthAndHeight)
            }
            .doOnSuccess {
                tracker.get().trackFileUpload("6", remoteUrl, flowId)
                Timber.d("$TAG scale 1000x1000 completed byteCount after=${it.byteCount}")
            }
            .doOnError {
                RecordException.recordException(it)
                tracker.get().trackFileUploadError("6", it)
                Timber.e("$TAG scale 1000x1000 error ${it.message}")
            }
            .map {
                val compressedImageQuality =
                    firebaseRemoteConfig.get().getLong(FILE_UPLOAD_COMPRESSED_IMAGE_QUALITY).toInt()
                compressedStream(it, compressedImageQuality)
            }
            .doOnSuccess {
                tracker.get().trackFileUpload("7", remoteUrl, flowId)
                Timber.d("$TAG compressed completed")
            }
            .doOnError {
                RecordException.recordException(it)
                tracker.get().trackFileUploadError("7", it)
                Timber.e("$TAG compressed error ${it.message}")
            }
            .flatMapCompletable {
                awsService.get().uploadFile(fileType, remoteUrl, it, flowId)
            }.doOnComplete {
                tracker.get().trackFileUpload("8", remoteUrl, flowId)
                Timber.d("$TAG file upload completed")
            }
            .doOnError {
                RecordException.recordException(it)
                tracker.get().trackFileUploadError("8", it)
                Timber.e("$TAG file upload error ${it.message}")
            }
    }

    /**
     *  Saving merchant image file on getExternalFilesDir/merchant_image/Base64encoder(profileImage)
     */
    override fun saveMerchantImageFile(remoteUrl: String): Completable {
        return Completable.fromAction {

            var image: Bitmap? = null
            try {
                val url = URL(remoteUrl)
                image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            } catch (e: IOException) {
            }

            val reminderImage = getMerchantProfileImageFile(remoteUrl)

            try {
                FileOutputStream(reminderImage).use { out ->
                    image?.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            } catch (e: IOException) {
            }
        }.subscribeOn(schedulers.get().newThread())
    }

    /**
     *  Get merchant image file from getExternalFilesDir/merchant_image/Base64encoder(profileImage)
     */
    override fun getMerchantImageFile(remoteUrl: String): Single<File?> {
        return Single.just(getMerchantProfileImageFile(remoteUrl))
    }

    private fun getMerchantProfileImageFile(remoteUrl: String): File {
        val storageDir = File(context.get().getExternalFilesDir(null), "merchant_image")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val fileName = BaseEncoding.base64().encode(remoteUrl.toByteArray())

        return File(context.get().getExternalFilesDir(null), "merchant_image/$fileName")
    }
}
