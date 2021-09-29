package `in`.okcredit.fileupload._id

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey

@GlideModule
class GlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        builder.apply {
            RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(
                    ObjectKey(
                        System.currentTimeMillis()
                            .toShort()
                    )
                )
        }

//        ARGB_8888 decoding format, which would take up 4 bytes each pixel;
//        else use RGB_565, which takes up only 2 bytes each pixel
        if (GlideMemoryPerformanceCheckerHelper.isPerformanceDevice(context)) {
            builder.setDefaultRequestOptions(
                RequestOptions()
                    .format(DecodeFormat.PREFER_ARGB_8888)
            )
        } else {
            builder.setDefaultRequestOptions(
                RequestOptions()
                    .format(DecodeFormat.PREFER_RGB_565)
            )
        }
    }

    companion object {
    }
}
