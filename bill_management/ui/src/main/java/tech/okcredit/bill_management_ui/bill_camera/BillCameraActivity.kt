package tech.okcredit.bill_management_ui.bill_camera

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.shared.utils.AbFeatures
import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.camera.camerax.CameraProvider
import com.camera.camerax.ICamera
import com.camera.gallery.GalleryUtils
import com.camera.models.models.Picture
import com.google.firebase.perf.metrics.AddTrace
import dagger.Lazy
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_bill_camera.*
import kotlinx.coroutines.launch
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import tech.okcredit.BillUtils
import tech.okcredit.BillUtils.FLAGS_FULLSCREEN
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.BaseLanguageActivity
import tech.okcredit.base.Traces
import tech.okcredit.bill_management_ui.R
import tech.okcredit.bill_management_ui.enhance_image.EnhanceImageActivity
import tech.okcredit.bill_management_ui.selected_bills.SelectedBillActivity
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.sdk.analytics.BillTracker
import java.io.File
import javax.inject.Inject

private const val IMMERSIVE_FLAG_TIMEOUT = 500L

class BillCameraActivity : BaseLanguageActivity(), ICamera {

    companion object {
        private const val MY_PERMISSIONS_REQUEST_CAMERA = 1
        private const val SINGLE_IMAGE_ACTIVITY_REQUEST_CODE = 1
        private const val ENHANCE_IMAGE_ACTIVTY_REQUEST_CODE = 2

        @JvmStatic
        fun createIntent(
            context: @NotNull Context,
            flow: @NotNull String,
            relation: @NotNull String,
            type: @NotNull String,
            screen: @NotNull String,
            accountId: @NotNull String,
            mobile: @Nullable String?,
            existingImages: Int,
            activityFlow: @NotNull String,
            billCount: Int,
            billId: String? = null,
            txnId: String? = null
        ): @NotNull Intent {
            return Intent(context, BillCameraActivity::class.java)
                .putExtra("flow", flow)
                .putExtra("relation", relation)
                .putExtra("type", type)
                .putExtra("screen", screen)
                .putExtra("accountId", accountId)
                .putExtra("mobile", mobile)
                .putExtra("existingImages", existingImages)
                .putExtra("activityFlow", activityFlow)
                .putExtra("billCount", billCount)
                .putExtra("billId", billId)
                .putExtra("txnId", txnId).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
    }

    private var billId: String? = null
    private var billCount: Int = 0
    private var activityFlow: String? = ""
    private var existingImages: Int = 0
    private var cameraProvider: CameraProvider? = null
    private var screenSource: String? = null
    private var flow: String? = null
    private var relation: String? = null
    private var accountId: String? = null
    private var type: String? = null
    private var screen: String? = null
    private var mobile: String? = null
    private var txnId: String? = null
    private var oldImageCount = 0
    var isCameraUsed = false
    var isGalleryUsed = false

    @Inject
    lateinit var billTracker: Lazy<BillTracker>

    // Really?
    private val photoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                it.getSerializableExtra(GalleryUtils.LOCALBROADCAST.PHOTOS)?.let {
                    isGalleryUsed = true
                    val listPhotos =
                        LinkedHashSet(intent.getSerializableExtra(GalleryUtils.LOCALBROADCAST.PHOTOS) as ArrayList<Picture>)
                    billTracker.get().trackChooseImage("Gallery", listPhotos.size, billId)
                    if (listPhotos.size == 1 && oldImageCount == 0) {
                        if (activityFlow == BILL_INTENT_EXTRAS.EDIT_FLOW) {
                            listPhotos.first().selected = true
                            gallery.notifyDataSetChanged()
                            setUi()
                        } else {
                            openImagePreviewScreen(listPhotos.first())
                        }
                    } else {
                        setUi()
                    }
                    oldImageCount = listPhotos.size
                }
            }
        }
    }

    @Inject
    internal lateinit var tracker: Tracker

    @Inject
    internal lateinit var ab: Lazy<AbRepository>

    internal fun openImagePreviewScreen(picture: Picture) {
        trackBillAdded()
        if (ab.get().isFeatureEnabled(AbFeatures.BILL_QUALITY_ENHANCE).blockingFirst()) {
            val listPhotos = ArrayList<CapturedImage>()
            listPhotos.add(CapturedImage(File(picture.path)))
            startActivityForResult(
                EnhanceImageActivity.createIntent(
                    this,
                    flow,
                    relation,
                    type,
                    screen,
                    mobile,
                    accountId,
                    addedImages = listPhotos
                ),
                ENHANCE_IMAGE_ACTIVTY_REQUEST_CODE
            )
        } else {
            val listPhotos = ArrayList<CapturedImage>()
            listPhotos.add(CapturedImage(File(picture.path)))
            startActivityForResult(
                SelectedBillActivity.createIntent(
                    this,
                    flow,
                    relation,
                    type,
                    screen,
                    mobile,
                    accountId,
                    addedImages = listPhotos
                ),
                SINGLE_IMAGE_ACTIVITY_REQUEST_CODE
            )
        }
    }

    @AddTrace(name = Traces.onCreateCameraActivity)
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_camera)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        makeStatusBarTransparent()
        getDataFromIntent()
        screenSource?.let {
            if (it == GalleryUtils.SCREEN_SOURCE.MULTI_IMAGE_ACTIVITY) {
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left)
            }
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                MY_PERMISSIONS_REQUEST_CAMERA
            )
        } else {
            cameraProvider = CameraProvider.getInstance()
            if (null == savedInstanceState) {
                cameraProvider?.beginCamera(supportFragmentManager, R.id.container)
            }
        }
        go.setOnClickListener { openSelectedImagesScreen() }
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(photoReceiver, IntentFilter(GalleryUtils.INTENTFILTER.selectedPhtots))
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CAMERA -> {
                val i = 0
                if (grantResults.isNotEmpty() && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    if (cameraProvider == null) {
                        cameraProvider = CameraProvider.getInstance()
                        cameraProvider?.beginCamera(supportFragmentManager, R.id.container)
                        supportFragmentManager.beginTransaction()
                    }
                } else {
                    this.goBack()
                }
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
            accountId = it.getStringExtra("accountId")
            existingImages = it.getIntExtra("existingImages", 0)
            txnId = it.getStringExtra("txnId")
            activityFlow = it.getStringExtra("activityFlow")
            billCount = it.getIntExtra("billCount", 0)
            billId = it.getStringExtra("billId")
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(photoReceiver)
        super.onDestroy()
    }

    private fun openSelectedImagesScreen() {
        trackBillAdded()
        if (activityFlow == BILL_INTENT_EXTRAS.EDIT_FLOW) {
            val listPhotos = ArrayList<CapturedImage>()
            for (i in gallery.getList()) {
                listPhotos.add(CapturedImage(File(i.path)))
            }

            lifecycleScope.launch {

                val modifiedList = if (ab.get().isFeatureEnabled(AbFeatures.BILL_QUALITY_ENHANCE).blockingFirst()) {
                    BillUtils.enhanceImages(listPhotos, this)
                } else {
                    listPhotos
                }

                count_contianer.visibility = View.VISIBLE
                progressBar.visibility = View.GONE

                val intent = Intent()
                intent.putExtra("addedImages", modifiedList)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        } else {
            val listPhotos = ArrayList<CapturedImage>()
            for (i in gallery.getList()) {
                listPhotos.add(CapturedImage(File(i.path)))
            }
            lifecycleScope.launch {

                val modifiedList = if (ab.get().isFeatureEnabled(AbFeatures.BILL_QUALITY_ENHANCE).blockingFirst()) {
                    BillUtils.enhanceImages(listPhotos, this)
                } else {
                    listPhotos
                }

                count_contianer.visibility = View.VISIBLE
                progressBar.visibility = View.GONE

                startActivityForResult(
                    SelectedBillActivity.createIntent(
                        this@BillCameraActivity,
                        flow,
                        relation,
                        type,
                        screen,
                        mobile,
                        accountId,
                        addedImages = modifiedList
                    ),
                    SINGLE_IMAGE_ACTIVITY_REQUEST_CODE
                )
            }
        }
    }

    override fun onBackPressed() {
        cameraProvider?.onFlashClicked(false)
        super.onBackPressed()
        screenSource?.let {
            if (it == BillUtils.SCREEN_SOURCE.MULTI_IMAGE_ACTIVITY) {
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
        } else {
            count_contianer.visibility = View.GONE
        }
        count.text = galleryList.size.toString()
    }

    private fun addCameraImageToGallery(picture: Picture) {
        gallery.addCameraImage(0, picture)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SINGLE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data?.getStringExtra(BILL_INTENT_EXTRAS.STATUS.KEY) == BILL_INTENT_EXTRAS.STATUS.IMAGE_SUCCESS) {
                    onBackPressed()
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

        billTracker.get().trackChooseImage("Camera", galleryList.size, billId)
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

    private fun trackBillAdded() {
        val source = when {
            isCameraUsed -> "Camera"
            isGalleryUsed -> "Gallery"
            else -> "Gallery and Camera"
        }
        billTracker.get().trackAddBillClicked(source, gallery.getList().size, billId)
    }
}
