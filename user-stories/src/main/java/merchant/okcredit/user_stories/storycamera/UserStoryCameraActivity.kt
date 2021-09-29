package merchant.okcredit.user_stories.storycamera

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.camera.camerax.CameraProvider
import com.camera.camerax.ICamera
import com.camera.gallery.GalleryUtils
import com.camera.models.models.Picture
import com.google.firebase.perf.metrics.AddTrace
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import merchant.okcredit.user_stories.R
import merchant.okcredit.user_stories.analytics.UserStoriesTracker
import merchant.okcredit.user_stories.analytics.UserStoryTraces
import merchant.okcredit.user_stories.databinding.ActivityUserStoryCameraBinding
import merchant.okcredit.user_stories.storypreview.StoryPreviewActivity
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.toArrayList
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.camera_contract.CapturedImage
import javax.inject.Inject

class UserStoryCameraActivity : OkcActivity(), ICamera {

    internal val binding: ActivityUserStoryCameraBinding by viewLifecycleScoped(ActivityUserStoryCameraBinding::inflate)
    private var cameraProvider: CameraProvider? = null
    private var isCameraUsed = false
    private var isGalleryUsed = false
    private var activeMyStoryCount: Int = 0
    private var activeMerchantId: String = ""

    @Inject
    lateinit var userStoryTracker: UserStoriesTracker

    override fun onBackPressed() {
        cameraProvider?.onFlashClicked(false)
        super.onBackPressed()
    }

    override fun goBack() {
        onBackPressed()
    }

    private fun addChooseImageTracker(source: String) {
        val cameraType = if (cameraProvider?.isBackLens() == true) "Back" else "Front"
        val flash = if (cameraProvider?.isFlashOFF() == true) "Off" else "On"
        userStoryTracker.trackEventStoryChooseImage(activeMerchantId, source, cameraType, flash)
    }

    override fun onCameraCapturedImage(picture: Picture) {
        addChooseImageTracker("camera")

        isCameraUsed = true
        val galleryList = binding.gallery.getList()

        if (!checkImageValidation(galleryList.size + 1)) {
            cameraProvider?.setCanCaptureImage(false)
            return
        }

        if (galleryList.size == 0) {
            goToPreviewScreen(picture)
        }
        galleryList.add(picture)
        addCameraImageToGallery(picture)
        userStoryTracker.trackEventStoryChooseImageSuccess(activeMerchantId, "Gallery", 1)
        if (galleryList.size >= 1) {
            cameraProvider?.setCanCaptureImage(true)
        }
        setUi()
    }

    internal fun checkImageValidation(size: Int): Boolean {
        return if (size > MAX_IMAGE_ALLOWED - activeMyStoryCount) {
            longToast(getString(R.string.max_image_error_msg))
            false
        } else true
    }

    internal fun setUi() {
        val galleryList = binding.gallery.getList()
        if (galleryList.isNotEmpty()) {
            binding.count.visible()
            binding.done.visible()
            binding.count.text = galleryList.size.toString()
        } else {
            binding.done.gone()
            binding.count.gone()
            binding.count.text = galleryList.size.toString()
        }
    }

    private fun addCameraImageToGallery(picture: Picture) {
        binding.gallery.addCameraImage(0, picture)
    }

    internal fun goToPreviewScreen(picture: Picture) {
        // will implement if get the use-case, as of now not required
    }

    @AddTrace(name = UserStoryTraces.ON_CREATE_USER_STORY_CAMERA_ACTIVITY)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        checkCameraAndStoragePermission()
        getExtras()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(photoReceiver, IntentFilter(GalleryUtils.INTENTFILTER.selectedPhtots))
        binding.done.setOnClickListener {
            startActivityForResult(
                StoryPreviewActivity.previewIntent(
                    this,
                    binding.gallery.getList().toMutableList().toArrayList(),
                    captionMap, activeMerchantId
                ),
                REQUEST_CODE_STORY_REVIEW
            )
        }
    }

    private fun getExtras() {
        activeMyStoryCount = intent.extras?.getInt(INTENT_KEY_ACTIVE_COUNT_MY_STORY, 0) ?: 0
        activeMerchantId = intent.extras?.getString(INTENT_KEY_ACTIVE_COUNT_MY_STORY, "") ?: ""
    }

    private fun checkCameraAndStoragePermission() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(permissionListener)
            .check()
    }

    private val permissionListener = object : MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
            if (report.areAllPermissionsGranted()) {
                startCamera()
            }
        }

        override fun onPermissionRationaleShouldBeShown(
            permissions: MutableList<PermissionRequest>?,
            token: PermissionToken?,
        ) {
            token?.continuePermissionRequest()
        }
    }

    internal fun startCamera() {
        if (null == cameraProvider) {
            cameraProvider = CameraProvider.getInstance()
            cameraProvider?.beginCamera(supportFragmentManager, R.id.container)
            binding.flipCamera.setOnClickListener { cameraProvider?.flipCamera() }
        }
    }

    private val photoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            addChooseImageTracker("Gallery")

            intent?.let {
                it.getSerializableExtra(GalleryUtils.LOCALBROADCAST.PHOTOS)?.let {
                    isGalleryUsed = true
                    val listPhotos =
                        LinkedHashSet(intent.getSerializableExtra(GalleryUtils.LOCALBROADCAST.PHOTOS) as ArrayList<Picture>)

                    if (!checkImageValidation(listPhotos.size)) {
                        binding.gallery.removeItem(listPhotos.last())
                        return
                    }

                    cameraProvider?.setCanCaptureImage(true)

                    binding.gallery.notifyDataSetChanged()
                    setUi()
                    if (listPhotos.isNotEmpty()) {
                        userStoryTracker.trackEventStoryChooseImageSuccess(activeMerchantId, "Gallery", listPhotos.size)
                        // goToPreviewScreen(listPhotos.first())
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(photoReceiver)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_STORY_REVIEW && resultCode == Activity.RESULT_OK) {
            captionMap = data?.getSerializableExtra(INTENT_KEY_CAPTION_MAP) as HashMap<CapturedImage?, String>
        }
    }

    var captionMap: HashMap<CapturedImage?, String>? = null

    companion object {
        const val MAX_IMAGE_ALLOWED = 30
        const val REQUEST_CODE_STORY_REVIEW = 0x10
        const val INTENT_KEY_CAPTION_MAP = "caption_map"
        private const val INTENT_KEY_ACTIVE_MERCHANT_ID = "key_active_merchant_id"
        private const val INTENT_KEY_ACTIVE_COUNT_MY_STORY = "active_count_my_story"

        fun openCamera(context: Context, activeMyStoryCount: Int, activeMerchantId: String) {
            Intent(context, UserStoryCameraActivity::class.java).apply {
                putExtra(INTENT_KEY_ACTIVE_COUNT_MY_STORY, activeMyStoryCount)
                putExtra(INTENT_KEY_ACTIVE_MERCHANT_ID, activeMerchantId)
            }.also {
                context.startActivity(it)
            }
        }
    }
}
