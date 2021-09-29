package `in`.okcredit.fileupload.usecase

import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.fileupload.usecase.IImageLoader.Companion.CENTER_CROP
import `in`.okcredit.fileupload.usecase.IImageLoader.Companion.CENTER_INSIDE
import `in`.okcredit.fileupload.usecase.IImageLoader.Companion.CIRCLE_CROP
import `in`.okcredit.fileupload.usecase.IImageLoader.Companion.FIT_CENTER
import `in`.okcredit.fileupload.utils.FileUtils
import `in`.okcredit.fileupload.utils.IResourceFinder
import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import dagger.Lazy
import dagger.Reusable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import tech.okcredit.android.base.extensions.ifLet
import java.io.File
import java.lang.ref.WeakReference
import javax.inject.Inject

@Deprecated(message = "https://okcredit.atlassian.net/wiki/spaces/AN/pages/363069567/ImageLoader+-+deprecating+now+move+back+to+Glide.with")
@Reusable
class GlideLoadImp @Inject constructor(
    private val fileUtils: Lazy<FileUtils>,
    private val resourceFinder: Lazy<IResourceFinder>
) : IImageLoader {

    private lateinit var context: WeakReference<Context>
    private var default: Drawable? = null
    private lateinit var imageView: WeakReference<ImageView>
    private var requestOptions = RequestOptions()
    internal var loadCallback: WeakReference<IImageLoadStatus?> = WeakReference(null)
    private var storageCallback: IImageStorageStatus? = null
    private var remoteUrl: String? = null
    private var priority: Priority = Priority.NORMAL
    private var thumbnail = 1f

    override fun context(@NonNull context: Context): GlideLoadImp {
        this.context = WeakReference(context)
        return this
    }

    override fun context(@NonNull context: Fragment): GlideLoadImp {
        this.context = WeakReference(context.context as Context)
        return this
    }

    override fun context(@NonNull context: AppCompatActivity): GlideLoadImp {
        this.context = WeakReference(context)
        return this
    }

    override fun load(remoteUrl: String?): GlideLoadImp {
        this.remoteUrl = remoteUrl
        return this
    }

    override fun placeHolder(@NonNull drawable: Drawable): GlideLoadImp {
        this.default = drawable
        return this
    }

    override fun placeHolder(@DrawableRes drawable: Int): GlideLoadImp {
        this.default = resourceFinder.get().getDrawable(drawable)
        return this
    }

    override fun apply(requestOptions: RequestOptions): GlideLoadImp {
        this.requestOptions = requestOptions

        return this
    }

    override fun scaleType(scaleType: Int): GlideLoadImp {

        when (scaleType) {
            CIRCLE_CROP -> {
                requestOptions.circleCrop()
            }
            CENTER_CROP -> {
                requestOptions.centerCrop()
            }
            FIT_CENTER -> {
                requestOptions.fitCenter()
            }
            CENTER_INSIDE -> {
                requestOptions.centerInside()
            }
        }

        return this
    }

    override fun listener(callback: IImageLoadStatus): GlideLoadImp {
        this.loadCallback = WeakReference(callback)
        return this
    }

    override fun into(imageView: ImageView): GlideLoadImp {
        this.imageView = WeakReference(imageView)
        return this
    }

    override fun thumbnail(thumbnails: Float): GlideLoadImp {
        this.thumbnail = thumbnails
        return this
    }

    override fun priority(priority: Priority): GlideLoadImp {
        this.priority = priority
        return this
    }

    override fun build(): Disposable {
        return fileUtils.get().getFile(remoteUrl)
            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { fileInfo ->

                var image: Any? = returnFile(fileInfo)
                if (image == null) {
                    image = default
                }

                imageView.get()?.let { imageView ->
                    try {
                        context.get()?.let { context ->
                            GlideApp.with(context)
                                .load(image)
                                .apply(requestOptions)
                                .fallback(default)
                                .placeholder(default)
                                .error(default)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        loadCallback.get()?.onFailed()
                                        return false
                                    }

                                    override fun onResourceReady(
                                        resource: Drawable?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        dataSource: DataSource?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        loadCallback.get()?.onSuccess()
                                        return false
                                    }
                                })
                                .into(imageView)
                        }
                    } catch (e: Exception) {
                        imageView.setImageDrawable(default)
                    }
                }
            }
    }

    /**
     *  This just returns the storage location of the file
     *  local file (onLocalFile) or Remote url (onRemoteUrl)
     */
    override fun storage(callback: IImageStorageStatus): Disposable {
        this.storageCallback = callback
        return fileUtils.get().getFile(remoteUrl)
            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { fileInfo ->
                returnFile(fileInfo)
            }
    }

    private fun returnFile(file: Any?): Any? {
        return when (file) {

            is IAwsServiceImp.FileInfo -> {
                if (file.isLocal) {
                    storageCallback?.onLocalFile(file.localFile)
                    file.localFile
                } else {
                    storageCallback?.onRemoteUrl(file.url)
                    file.url
                }
            }
            else -> {
                file
            }
        }
    }

    fun buildNormal() {

        val image: Any? = returnFile(fileUtils.get().getNormalFile(remoteUrl))
        var refinedImage: String? = ""
        if (image is File) {
            refinedImage = image.path
        } else if (image is String) {
            refinedImage = image
        }
        refinedImage?.let {
            if (it.startsWith("https:/s3")) {
                refinedImage = refinedImage?.replace("https:/s3", "https://s3")
            }
        }
        ifLet(imageView.get(), context.get()) { imageView, context ->
            GlideApp.with(context)
                .load(refinedImage)
                .apply(requestOptions)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        loadCallback.get()?.onFailed()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        loadCallback.get()?.onSuccess()
                        return false
                    }
                })
                .dontAnimate()
                .thumbnail(thumbnail)
                .priority(priority)
                .into(imageView)
        }
    }

    // TODO: Clean build
    fun buildNormalWithPlaceholder() {
        val image: Any? = returnFile(fileUtils.get().getNormalFile(remoteUrl))
        var refinedImage: String? = ""
        if (image is File) {
            refinedImage = image.path
        } else if (image is String) {
            refinedImage = image
        }
        refinedImage?.let {
            if (it.startsWith("https:/s3")) {
                refinedImage = refinedImage?.replace("https:/s3", "https://s3")
            }
        }
        ifLet(imageView.get(), context.get()) { imageView, context ->
            GlideApp.with(context)
                .load(refinedImage)
                .apply(requestOptions)
                .placeholder(default)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        loadCallback.get()?.onFailed()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        loadCallback.get()?.onSuccess()
                        return false
                    }
                })
                .dontAnimate()
                .thumbnail(thumbnail)
                .priority(priority)
                .into(imageView)
        }
    }
}
