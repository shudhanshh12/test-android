package `in`.okcredit.fileupload.usecase

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import io.reactivex.disposables.Disposable
import java.io.File

@Deprecated(message = "https://okcredit.atlassian.net/wiki/spaces/AN/pages/363069567/ImageLoader+-+deprecating+now+move+back+to+Glide.with")
interface IImageLoader {
    fun load(remoteUrl: String?): GlideLoadImp
    fun context(context: Fragment): GlideLoadImp
    fun context(context: AppCompatActivity): GlideLoadImp
    fun context(context: Context): GlideLoadImp
    fun placeHolder(drawable: Drawable): GlideLoadImp
    fun placeHolder(drawable: Int): GlideLoadImp
    fun apply(requestOptions: RequestOptions): GlideLoadImp
    fun scaleType(scaleType: Int): GlideLoadImp
    fun listener(callback: IImageLoadStatus): GlideLoadImp
    fun storage(callback: IImageStorageStatus): Disposable
    fun into(imageView: ImageView): GlideLoadImp
    fun thumbnail(thumbnails: Float): GlideLoadImp
    fun priority(priority: Priority): GlideLoadImp
    fun build(): Disposable

    companion object {
        const val CIRCLE_CROP = 0
        const val CENTER_CROP = 1
        const val FIT_CENTER = 2
        const val CENTER_INSIDE = 3
    }
}

interface IImageLoadStatus {
    fun onSuccess()
    fun onFailed()
}

interface IImageStorageStatus {
    fun onLocalFile(file: File?)
    fun onRemoteUrl(remoteUrl: String?)
}
