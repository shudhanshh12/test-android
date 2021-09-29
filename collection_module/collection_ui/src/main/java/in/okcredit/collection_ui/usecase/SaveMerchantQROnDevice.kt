package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.utils.CollectionFileUtils
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import io.reactivex.Single
import tech.okcredit.android.base.crashlytics.RecordException
import javax.inject.Inject

class SaveMerchantQROnDevice @Inject constructor(
    private val context: Context,
    private val getMerchantQRBitmap: GetMerchantQRBitmap,
) {
    companion object {
        const val mimeType = "image/*"
        const val fileName = "merchant_qr.jpeg"
    }

    fun execute(): Single<String> {
        return getMerchantQRBitmap.execute()
            .map {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    CollectionFileUtils.saveBitmap(
                        context,
                        it.bitmap,
                        Bitmap.CompressFormat.JPEG,
                        mimeType,
                        "${System.currentTimeMillis()}_$fileName"
                    )
                } else {
                    CollectionFileUtils.saveBitmapPreQ(
                        context,
                        it.bitmap,
                        "${System.currentTimeMillis()}_$fileName"
                    )
                }
                return@map context.getString(R.string.merchant_qr_saved_successfully)
            }
            .doOnError {
                RecordException.recordException(it)
                throw Exception(context.getString(R.string.unable_to_save_merchant_qr))
            }
    }
}
