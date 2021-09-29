package com.camera

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.fileupload.utils.FileUtils
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.camera.camerax.CameraProvider
import com.camera.camerax.ICamera
import com.camera.gallery.GalleryUtils
import com.camera.models.models.Picture
import com.google.firebase.perf.metrics.AddTrace
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.BaseLanguageActivity
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.base.Traces
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.camera_contract.CapturedImage
import java.io.File
import javax.inject.Inject

class CameraActivity : BaseLanguageActivity(), ICamera {
    private var existingImages: Int = 0
    private var cameraProvider: CameraProvider? = null
    private var screenSource: String? = null
    private var flow: String? = null
    private var relation: String? = null
    private var account: String? = null
    private var type: String? = null
    private var screen: String? = null
    private var mobile: String? = null
    private var txnId: String? = null
    internal var oldImageCount = 0
    private val photoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                it.getSerializableExtra(GalleryUtils.LOCALBROADCAST.PHOTOS)?.let {
                    isGalleryUsed = true
                    val listPhotos =
                        LinkedHashSet(intent.getSerializableExtra(GalleryUtils.LOCALBROADCAST.PHOTOS) as ArrayList<Picture>)
                    if (listPhotos.size == 1 && oldImageCount == 0) {
                        openImagePreviewScreen(listPhotos.first())
                    } else {
                        setUi()
                    }
                    oldImageCount = listPhotos.size
                }
            }
        }
    }
    private var isCameraUsed = false
    internal var isGalleryUsed = false

    @Inject
    internal lateinit var tracker: Tracker

    @Inject
    internal lateinit var ab: AbRepository

    internal fun openImagePreviewScreen(picture: Picture) {
        startActivityForResult(
            CapturedImageActivity.createIntent(
                this,
                picture,
                flow,
                relation,
                type,
                screen,
                mobile,
                account
            ),
            SINGLE_IMAGE_ACTIVITY_REQUEST_CODE
        )
    }

    @AddTrace(name = Traces.onCreateCameraActivity)
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        makeStatusBarTransparent()
        getDataFromIntent()
        screenSource?.let {
            if (it == GalleryUtils.SCREEN_SOURCE.MULTI_IMAGE_ACTIVITY) {
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left)
            }
        }
        checkForPermission()
        go.setOnClickListener {
            val listPhotos = gallery.getList()
            tracker.trackAddReceiptCompleted(
                flow,
                relation,
                type,
                getCaptureMethod(),
                screen,
                listPhotos.size.toString(),
                existingImages.toString(),
                account,
                txnId
            )
            returnImages()
        }
        image_gallery.setOnClickListener {
            pickFromGallery()
        }
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(photoReceiver, IntentFilter(GalleryUtils.INTENTFILTER.selectedPhtots))
    }

    private fun checkForPermission() {
        Permission.requestCameraPermission(
            this,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                }

                override fun onPermissionGranted() {
                    cameraProvider = CameraProvider.getInstance()
                    cameraProvider?.beginCamera(supportFragmentManager, R.id.container)
                    supportFragmentManager.beginTransaction()
                }

                override fun onPermissionDenied() {
                    longToast(R.string.camera_permission_denied)
                    finish()
                }

                override fun onPermissionPermanentlyDenied() {
                }
            }
        )
    }

    private fun pickFromGallery() {
        tracker.trackV1("pick_from_gallery", screen ?: "")
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/*"
        startActivityForResult(pickIntent, PICK_IMAGE)
    }

    private fun getCaptureMethod(): String {
        var method = ""
        if (isCameraUsed && isGalleryUsed) {
            method = "Camera and Gallery"
        } else if (isCameraUsed) {
            method = "Camera"
        } else if (isGalleryUsed) {
            method = "Gallery"
        }
        return method
    }

    private fun makeStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }
                statusBarColor = Color.TRANSPARENT
            }
        }
    }

    override fun onResume() {
        super.onResume()
        container.postDelayed(
            {
                container.systemUiVisibility = FLAGS_FULLSCREEN
            },
            IMMERSIVE_FLAG_TIMEOUT
        )
    }

    private fun getDataFromIntent() {
        intent?.let {
            screenSource = it.getStringExtra("SOURCE")
            flow = it.getStringExtra("flow")
            relation = it.getStringExtra("relation")
            type = it.getStringExtra("type")
            screen = it.getStringExtra("screen")
            mobile = it.getStringExtra("mobile")
            account = it.getStringExtra("account")
            existingImages = it.getIntExtra("existingImages", 0)
            txnId = it.getStringExtra("txnId")
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(photoReceiver)
        super.onDestroy()
    }

    private fun returnImages() {
        val listPhotos = ArrayList<CapturedImage>()
        for (i in gallery.getList()) {
            listPhotos.add(CapturedImage(File(i.path)))
        }
        val retunIntent = Intent()
        retunIntent.putExtra(ADDED_IMAGES, listPhotos)
        setResult(Activity.RESULT_OK, retunIntent)
        finish()
    }

    override fun onBackPressed() {
        cameraProvider?.onFlashClicked(false)
        super.onBackPressed()
        screenSource?.let {
            if (it == Utils.SCREEN_SOURCE.MULTI_IMAGE_ACTIVITY) {
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left)
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        cameraProvider?.setCanCaptureImage(true)
        setUi()
        gallery.notifyDataSetChanged()
    }

    internal fun setUi() {
        val galleryList = gallery.getList()
        if (galleryList.isNotEmpty()) {
            count_contianer.visibility = View.VISIBLE
            count.text = galleryList.size.toString()
        } else {
            count_contianer.visibility = View.GONE
            count.text = galleryList.size.toString()
        }
    }

    private fun addCameraImageToGallery(picture: Picture) {
        gallery.addCameraImage(0, picture)
    }

    companion object {

        const val ADDED_IMAGES = "addedImages"
        private const val SINGLE_IMAGE_ACTIVITY_REQUEST_CODE: Int = 1
        private const val PICK_IMAGE = 2
        private const val IMMERSIVE_FLAG_TIMEOUT = 500L

        @JvmStatic
        fun createIntent(
            context: @NotNull Context,
            flow: @NotNull String,
            relation: @NotNull String,
            type: @NotNull String,
            screen: @NotNull String,
            account: @Nullable String?,
            mobile: @Nullable String?,
            existingImages: Int,
        ): @NotNull Intent {
            val intent = Intent(context, CameraActivity::class.java)
            intent.putExtra("flow", flow)
            intent.putExtra("relation", relation)
            intent.putExtra("type", type)
            intent.putExtra("screen", screen)
            intent.putExtra("account", account)
            intent.putExtra("mobile", mobile)
            intent.putExtra("existingImages", existingImages)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }

        @JvmStatic
        fun startActivityForResultFromFragment(
            context: Context,
            fragment: Fragment,
            requestCode: Int,
            flow: @NotNull String,
            relation: @NotNull String,
            type: @NotNull String,
            screen: @NotNull String,
            account: @NotNull String,
            mobile: @Nullable String?,
            existingImagesCount: Int,
        ) {
            val starter = Intent(context, CameraActivity::class.java)
                .putExtra("flow", flow)
                .putExtra("relation", relation)
                .putExtra("type", type)
                .putExtra("screen", screen)
                .putExtra("account", account)
                .putExtra("mobile", mobile)
                .putExtra("existingImages", existingImagesCount)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            fragment.startActivityForResult(starter, requestCode)
        }

        fun createSelectedImageIntent(
            context: Context,
            filteredList: ArrayList<CapturedImage>,
            source: String,
            flow: String?,
            relation: String?,
            type: String?,
            screen: String?,
            account: String?,
            mobile: String?,
            existingImages: Int,
            txnId: String? = null,
        ) = Intent(context, CameraActivity::class.java)
            .putExtra("filtered_list", filteredList)
            .putExtra("flow", flow)
            .putExtra("relation", relation)
            .putExtra("type", type)
            .putExtra("screen", screen)
            .putExtra("account", account)
            .putExtra("mobile", mobile)
            .putExtra("existingImages", existingImages)
            .putExtra("SOURCE", source)
            .putExtra("txnId", txnId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SINGLE_IMAGE_ACTIVITY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    tracker.trackAddReceiptCompleted(
                        flow,
                        relation,
                        type,
                        getCaptureMethod(),
                        screen,
                        "1",
                        existingImages.toString(),
                        account,
                        txnId
                    )
                    returnImages()
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    if (data?.getSerializableExtra("deleted_photo") != null) {
                        val deletedPhoto = data.getSerializableExtra("deleted_photo") as Picture
                        deletedPhoto.let {
                            gallery.removeItem(deletedPhoto)
                        }
                        setUi()
                    }
                }
            }
            PICK_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    lifecycleScope.launchWhenResumed {
                        var absolutePath: String? = null
                        val selectedImageURI: Uri? = data?.data
                        withContext(Dispatchers.IO) {
                            selectedImageURI?.let {
                                absolutePath = FileUtils.getPath(this@CameraActivity, it)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            if (absolutePath.isNotNullOrBlank()) {
                                val picture = Picture(absolutePath!!)
                                val galleryList = gallery.getList()

                                if (galleryList.size == 0) {
                                    openImagePreviewScreen(picture)
                                }

                                galleryList.add(picture)
                                addCameraImageToGallery(picture)
                                if (galleryList.size >= 1) {
                                    cameraProvider?.setCanCaptureImage(true)
                                }
                                setUi()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun goBack() {
        onBackPressed()
    }

    override fun onCameraCapturedImage(picture: Picture) {
        isCameraUsed = true
        val galleryList = gallery.getList()

        if (galleryList.size == 0) {
            openImagePreviewScreen(picture)
        }

        galleryList.add(picture)
        addCameraImageToGallery(picture)
        if (galleryList.size >= 1) {
            cameraProvider?.setCanCaptureImage(true)
        }
        setUi()
    }
}
