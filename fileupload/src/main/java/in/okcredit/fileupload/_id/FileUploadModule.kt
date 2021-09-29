package `in`.okcredit.fileupload._id

import `in`.okcredit.fileupload.usecase.*
import `in`.okcredit.fileupload.user_migration.data.database.room.FileUploadDB
import `in`.okcredit.fileupload.user_migration.data.database.room.FileUploadDao
import `in`.okcredit.fileupload.user_migration.data.repository.MigrationLocalSourceImpl
import `in`.okcredit.fileupload.user_migration.data.repository.MigrationRemoteSourceImpl
import `in`.okcredit.fileupload.user_migration.data.repository.MigrationRepoImpl
import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationLocalSource
import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationRemoteSource
import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationRepo
import `in`.okcredit.fileupload.utils.Constants
import `in`.okcredit.fileupload.utils.IResourceFinder
import `in`.okcredit.fileupload.utils.ISchedulerProvider
import `in`.okcredit.fileupload.utils.ResourceFinderImp
import `in`.okcredit.fileupload.utils.SchedulerProvider
import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import android.content.Context
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import dagger.Binds
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoMap
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey

@dagger.Module
abstract class FileUploadModule {

    @Binds
    @Reusable
    abstract fun bindUploadFile(uploadFileImpl: UploadFileImpl): IUploadFile

    @Binds
    @Reusable
    abstract fun bindUploadAudioSampleFile(uploadAudioSampleFileImpl: UploadAudioSampleFileImpl): IUploadAudioSampleFile

    @Binds
    @Reusable
    abstract fun userMigrationRepo(migrationRepo: MigrationRepoImpl): MigrationRepo

    @Binds
    @Reusable
    abstract fun userMigrationServer(migrationServer: MigrationRemoteSourceImpl): MigrationRemoteSource

    @Binds
    @Reusable
    abstract fun userMigrationStore(migrationStore: MigrationLocalSourceImpl): MigrationLocalSource

    @Binds
    @Reusable
    abstract fun bindIAwsService(awsServiceImp: IAwsServiceImp): IAwsService

    @Binds
    @Reusable
    abstract fun bindGlideLoad(glideLoadImp: GlideLoadImp): IImageLoader

    @Binds
    @Reusable
    abstract fun bindSchedulerProvider(schedulerProvider: SchedulerProvider): ISchedulerProvider

    @Binds
    @Reusable
    abstract fun bindResourceFinder(resourceFinder: ResourceFinderImp): IResourceFinder

    @Binds
    @IntoMap
    @WorkerKey(UploadFileImpl.RxUploadWorker::class)
    @Reusable
    abstract fun WorkerCustomerTxnAlertDialogDismissWorker(factory: UploadFileImpl.RxUploadWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(UploadAudioSampleFileImpl.RxUploadWorker::class)
    @Reusable
    abstract fun uploadAudioSampleFileWorker(factory: UploadAudioSampleFileImpl.RxUploadWorker.Factory): ChildWorkerFactory

    companion object {

        @Provides
        fun database(context: Context, migrationHandler: MultipleAccountsDatabaseMigrationHandler): FileUploadDB {
            return FileUploadDB.getInstance(context, migrationHandler)
        }

        @Provides
        @Reusable
        fun dao(database: FileUploadDB): FileUploadDao {
            return database.fileUploadsDataBaseDao()
        }

        @Provides
        fun provideAwsMobileClient(context: Context): AWSCredentialsProvider {
            return CognitoCachingCredentialsProvider(
                context,
                Constants.AWS_IDENTITY_POOL_ID,
                Regions.US_EAST_1
            )
        }

        @Provides
        fun provideS3Client(context: Context, awsCredentialsProvider: AWSCredentialsProvider): AmazonS3Client {
            val regionString = AWSConfiguration(context)
                .optJsonObject("S3TransferUtility")
                .getString("Region")
            return AmazonS3Client(awsCredentialsProvider, Region.getRegion(regionString))
        }

        @Provides
        fun provideTransferUtility(context: Context, s3Client: AmazonS3Client): TransferUtility {
            return TransferUtility.builder()
                .context(context)
                .s3Client(s3Client)
                .awsConfiguration(AWSConfiguration(context))
                .build()
        }
    }
}
