package `in`.okcredit.fileupload.user_migration.data.repository

import `in`.okcredit.fileupload.usecase.IUploadFile
import `in`.okcredit.fileupload.user_migration.domain.model.UploadStatus
import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationLocalSource
import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationRemoteSource
import `in`.okcredit.fileupload.utils.AwsHelper
import `in`.okcredit.fileupload.utils.Constants
import `in`.okcredit.fileupload.utils.FileUtils
import android.content.Context
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import tech.okcredit.android.base.rxjava.SchedulerProvider
import java.io.File
import java.util.*
import javax.inject.Inject

class MigrationRemoteSourceImpl @Inject constructor(
    private val context: Lazy<Context>,
    private val awsHelper: Lazy<AwsHelper>,
    private val fileUtils: Lazy<FileUtils>,
    private val migrationLocalSource: Lazy<MigrationLocalSource>,
    private val schedulerProvider: Lazy<SchedulerProvider>
) : MigrationRemoteSource {

    private var transferUtility: TransferUtility? = null

    /**
     *  initializes AWS with id, region and client
     */
    private fun initAwsSettings(context: Context) {
        val credentialsProvider = CognitoCachingCredentialsProvider(
            context,
            Constants.AWS_IDENTITY_POOL_ID,
            Regions.US_EAST_1
        )
        TransferNetworkLossHandler.getInstance(context)
        val clientConfiguration = ClientConfiguration()
        clientConfiguration.connectionTimeout = 45 * 1000
        clientConfiguration.socketTimeout = 45 * 1000
        val s3 = AmazonS3Client(credentialsProvider, clientConfiguration)
        transferUtility = TransferUtility.builder().s3Client(s3).context(context).build()
    }

    override fun uploadFile(filePath: List<String>, businessId: String): Completable {
        return Completable.fromAction {
            filePath.forEach { filePath ->
                val remoteUrl = IUploadFile.AWS_MIGRATION_BASE_URL + "/" + UUID.randomUUID() + ".pdf"
                val fileNameKey = fileUtils.get().getFileName(remoteUrl)
                if (transferUtility == null) {
                    initAwsSettings(context.get())
                }
                transferUtility!!.upload(IUploadFile.AWS_USER_MIGRATION_BUCKET_NAME, fileNameKey, File(filePath))
                    .setTransferListener(createTransferListener(businessId = businessId))
            }
        }
    }

    override fun uploadImage(imagePath: String, businessId: String): Single<String> {
        val randomId = UUID.randomUUID()
        val remotePushUrl = IUploadFile.AWS_MIGRATION_BASE_URL + "/" + randomId + ".jpeg"
        val remotePullUrl = IUploadFile.AWS_MIGRATION_PULL_URL + randomId + ".jpeg"
        val fileNameKey = fileUtils.get().getFileName(remotePushUrl)
        if (transferUtility == null) {
            initAwsSettings(context.get())
        }
        val uploadStatusEmitter = PublishSubject.create<UploadStatus>()
        transferUtility!!.upload(IUploadFile.AWS_USER_MIGRATION_BUCKET_NAME, fileNameKey, File(imagePath))
            .setTransferListener(createTransferListener(uploadStatusEmitter, businessId))

        return uploadStatusEmitter.filter {
            it.status == AwsHelper.COMPLETED
        }.take(1).ignoreElements().toSingleDefault(remotePullUrl)
    }

    override fun retryUploadFile(uploadStatus: UploadStatus?, businessId: String): Completable {
        return Completable.fromAction {
            if (transferUtility == null) {
                initAwsSettings(context.get())
            }
            transferUtility!!.resume(uploadStatus!!.id)
                .setTransferListener(createTransferListener(businessId = businessId))
        }
    }

    override fun cancelUploadFile(uploadStatus: UploadStatus?): Completable {
        return Completable.fromAction {
            if (transferUtility == null) {
                initAwsSettings(context.get())
            }
            transferUtility!!.cancel(uploadStatus!!.id)
        }
    }

    private fun createTransferListener(emitter: Subject<UploadStatus>? = null, businessId: String): TransferListener {
        return object : TransferListener {
            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val percentage = ((bytesCurrent * 100) / bytesTotal).toInt()
                // TODO If we get zero kb File from user then it will be a crash
                if (bytesCurrent < bytesTotal) {
                    migrationLocalSource.get()
                        .saveUploadStatus(getUploadStatus(id, null), businessId)
                        .subscribeOn(schedulerProvider.get().io())
                        .subscribe()
                }
            }

            override fun onStateChanged(id: Int, state: TransferState?) {
                migrationLocalSource.get()
                    .saveUploadStatus(getUploadStatus(id, state), businessId)
                    .subscribeOn(schedulerProvider.get().io())
                    .subscribe()

                emitter?.onNext(getUploadStatus(id, state))
            }

            override fun onError(id: Int, ex: Exception?) {
                emitter?.onError(ex as Throwable)
            }
        }
    }

    internal fun getUploadStatus(
        id: Int,
        state: TransferState?
    ): UploadStatus {

        if (transferUtility == null) {
            initAwsSettings(context.get())
        }

        if (state == TransferState.COMPLETED) {
            return UploadStatus(
                id = id,
                status = awsHelper.get().getState(TransferState.COMPLETED),
                color = awsHelper.get().getStateColor(TransferState.COMPLETED),
                percentage = 100,
                cancelled = false,
            )
        } else {
            return UploadStatus(
                id = id,
                filePath = transferUtility!!.getTransferById(id).absoluteFilePath,
                remoteUrl = "https://easy-um.s3.ap-south-1.amazonaws.com/${transferUtility!!.getTransferById(id).key}",
                status = awsHelper.get().getState(transferUtility!!.getTransferById(id).state),
                uploadedSize = transferUtility!!.getTransferById(id).bytesTransferred,
                totalSize = transferUtility!!.getTransferById(id).bytesTotal,
                color = awsHelper.get().getStateColor(transferUtility!!.getTransferById(id).state),
                percentage = (
                    (transferUtility!!.getTransferById(id).bytesTransferred * 100) /
                        transferUtility!!.getTransferById(id).bytesTotal
                    ).toInt(),
                cancelled = false
            )
        }
    }
}
