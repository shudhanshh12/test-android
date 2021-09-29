package `in`.okcredit.fileupload.usecase

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File

interface IUploadFile {

    fun schedule(photoType: String, remoteUrl: String, localPath: String): Completable

    fun loadContactPhotoThumbnail(photoData: String, context: Context): String?

    fun execute(fileType: String?, remoteUrl: String, filePath: String, flowId: String): Completable

    fun getMerchantImageFile(remoteUrl: String): Single<File?>

    fun saveMerchantImageFile(remoteUrl: String): Completable

    companion object {
        const val RECEIPT_PHOTO = "receipt photo"
        const val CUSTOMER_PHOTO = "customer photo"
        const val CONTACT_PHOTO = "contact photo"

        // AWS Credentials
        const val AWS_BUCKET_NAME = "receipts.okcredit.in"
        const val AWS_USER_MIGRATION_BUCKET_NAME = "easy-um"
        const val AWS_IDENTITY_POOL_ID = "us-east-1:c08b0413-b75a-48b8-8dfe-22a4ee4a8ed8"
        const val AWS_RECEIPT_BASE_URL = "https://s3.amazonaws.com/receipts.okcredit.in"
        const val AWS_MIGRATION_BASE_URL = "https://s3.amazonaws.com/easy-um"
        const val AWS_MIGRATION_PULL_URL = "https://easy-um.s3.ap-south-1.amazonaws.com/"
    }
}
