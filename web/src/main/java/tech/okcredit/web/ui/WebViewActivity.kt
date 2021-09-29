package tech.okcredit.web.ui

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.merchant.device.DeviceUtils
import `in`.okcredit.web.BuildConfig
import `in`.okcredit.web.WebExperiment
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import dagger.Lazy
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_web_view.*
import tech.okcredit.android.auth.AccessTokenProvider
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.disableScreanCapture
import tech.okcredit.android.base.extensions.fromJson
import tech.okcredit.android.base.extensions.ifNullOrBlank
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.json.GsonUtils
import tech.okcredit.android.base.json.json
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.utils.FileUtils
import tech.okcredit.android.base.utils.GpsUtils
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.exceptions.ExceptionUtils
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.web.R
import tech.okcredit.web.WebTracker
import tech.okcredit.web.WebUrlNavigator
import tech.okcredit.web.utils.SmsBroadcastReceiver
import tech.okcredit.web.utils.SmsReceivedListener
import tech.okcredit.web.utils.WebShareUtils
import tech.okcredit.web.utils.isThirdPartyUrl
import tech.okcredit.web.web_clients.CustomWebChromeClient
import tech.okcredit.web.web_clients.CustomWebViewClient
import tech.okcredit.web.web_clients.CustomWebViewClientCompat
import tech.okcredit.web.web_interfaces.ThirdPartyWebAppInterface
import tech.okcredit.web.web_interfaces.WebAppInterface
import tech.okcredit.web.web_interfaces.WebViewCallbackListener
import tech.okcredit.web.web_interfaces.WebViewDataLayerCallbackListenerImpl
import tech.okcredit.web.web_interfaces.sendOtpEvent
import tech.okcredit.web.web_interfaces.sendSmsPermissionEvent
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class WebViewActivity : AppCompatActivity(), WebViewCallbackListener {

    companion object {

        private const val TAG = "<<<<WebViewActivity"

        const val EXPERIMENT_FLAG = "experiment_flag"
        const val QUERY_PARAMETERS = "query_parameters"
        const val EXTRA_WEB_URL = "web_url"
        const val TAG_ANDROID_WEBVIEW = "Android Webview: %s"
        const val STORAGE_REQUEST_CODE = 101
        const val CAMERA_REQUEST_CODE = 201

        const val CAMERA_PERMISSION_CODE = 101
        const val STORAGE_PERMISSION_CODE = 202

        const val SOURCE_PAYTM = "-PAYTMB"

        enum class UserPermission {
            CAMERA,
            STORAGE
        }

        @JvmStatic
        fun startingIntentForExperiment(
            context: Context,
            experiment: String,
            queryParams: Map<String, String>? = null,
        ): Intent {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(EXPERIMENT_FLAG, experiment)

            queryParams?.let {
                intent.putExtra(QUERY_PARAMETERS, queryParams.json())
            }

            return intent
        }

        @JvmStatic
        fun startingIntentForWebView(context: Context, url: String?): Intent =
            Intent(context, WebViewActivity::class.java).putExtra(EXTRA_WEB_URL, url)

        @JvmStatic
        fun start(context: Context, url: String?) {
            context.startActivity(startingIntentForWebView(context, url))
        }
    }

    @Inject
    lateinit var tokenProvider: Lazy<AccessTokenProvider>

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    @Inject
    lateinit var deviceUtils: Lazy<DeviceUtils>

    @Inject
    lateinit var communicationApi: Lazy<CommunicationRepository>

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var webTracker: Lazy<WebTracker>

    @Inject
    lateinit var webUrlNavigator: Lazy<WebUrlNavigator>

    @Inject
    lateinit var webViewDataLayerCallbackListenerImpl: Lazy<WebViewDataLayerCallbackListenerImpl>

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var lastKnownLocation: Location? = null

    var webviewFilePaths: ValueCallback<Array<Uri>>? = null

    private var cameraPhotoPath: String = ""

    var startBootTime: Long? = null

    internal var smsReceiverListener: SmsBroadcastReceiver? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation = locationResult.lastLocation
            Timber.d("$TAG Location Accessed from requestNewLocationData ${lastLocation.latitude}")
            lastKnownLocation = lastLocation
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        window.disableScreanCapture()
        super.onCreate(savedInstanceState)
        initViews()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        LocaleManager.fixWebViewLocale(this)
    }

    private fun initViews() {
        startBootTime = System.currentTimeMillis()
        setContentView(R.layout.activity_web_view)

        toolbar.setNavigationOnClickListener { onBackPressed() }

        tryAgain.setOnClickListener { tryAgainAction() }

        cancel.setOnClickListener { onBackPressed() }

        webview.apply {
            settings.apply {
                domStorageEnabled = true
                javaScriptEnabled = true
                setGeolocationEnabled(true)
                setSupportMultipleWindows(true)
                javaScriptCanOpenWindowsAutomatically = true
                setAppCachePath(context.applicationContext.cacheDir.absolutePath)
            }
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            webViewClient = getWebViewClientAccToAndroidVersion()
            webChromeClient = CustomWebChromeClient(WeakReference(this@WebViewActivity), webview, video_frame)
        }

        webview.setDownloadListener { url, _, _, _, _ ->
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            startActivity(intent)
        }

        loadWebView()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun loadWebView() {
        manageWebViewContainer(true)
        val webUrl: String? = intent.getStringExtra(EXTRA_WEB_URL).ifNullOrBlank { null }

        val experiment: WebExperiment = WebExperiment.getExperiment(intent.getStringExtra(EXPERIMENT_FLAG))

        var url: String = Uri.decode(
            when {
                webUrl.isNotNullOrBlank() -> webUrl!!
                BuildConfig.DEBUG -> experiment.stagingUrl
                else -> experiment.prodUrl
            }
        )

        val queryParamsString = intent.getStringExtra(QUERY_PARAMETERS)
        queryParamsString?.let {
            val queryParams = requireNotNull(GsonUtils.gson().fromJson<Map<String, String>>(it))
            val uriBuilder = Uri.parse(url).buildUpon()

            queryParams.forEach { (key, value) ->
                uriBuilder.appendQueryParameter(key, value)
            }

            url = uriBuilder.build().toString()
        }
        showLoading()
        addJavascriptInterface(url)
        if (url.isThirdPartyUrl()) {
            webUrlNavigator.get().openUrl(this, url)
            finish()
        } else {
            webview.loadUrl(url)
        }
    }

    private fun addJavascriptInterface(url: String) {
        webview.removeJavascriptInterface(WebAppInterface.JAVASCRIPT_WEB_INTERFACE)
        if (url.isThirdPartyUrl()) {
            webview.addJavascriptInterface(
                ThirdPartyWebAppInterface(this),
                WebAppInterface.JAVASCRIPT_WEB_INTERFACE
            )
        } else {
            webview.addJavascriptInterface(
                WebAppInterface(this, webViewDataLayerCallbackListenerImpl.get()),
                WebAppInterface.JAVASCRIPT_WEB_INTERFACE
            )
        }
    }

    fun manageWebViewContainer(showWebView: Boolean) {
        webview.isVisible = showWebView
        appbar.isVisible = !showWebView
        network_err_container.isVisible = !showWebView
    }

    fun showLoading() {
        loading.visibility = View.VISIBLE
    }

    fun hideLoading() {
        loading.visibility = View.GONE
    }

    private fun tryAgainAction() {
        loadWebView()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()) {
            webview.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun requestPermission(permission: UserPermission) {
        when (permission) {
            UserPermission.CAMERA -> ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
            UserPermission.STORAGE -> ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private fun checkPermission(permission: UserPermission): Boolean {
        return when (permission) {
            UserPermission.CAMERA -> ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            UserPermission.STORAGE -> ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun handleCameraUpload() {
        if (checkPermission(UserPermission.CAMERA)) {

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(this@WebViewActivity.packageManager) != null) {
                var photoFile: File? = null
                try {
                    photoFile = createImage()
                    takePictureIntent.putExtra("PhotoPath", cameraPhotoPath)
                } catch (ex: IOException) {
                    Timber.e(TAG_ANDROID_WEBVIEW, "Image file creation failed $ex")
                    ExceptionUtils.Companion.logException("Error: Webview Image file creation failed ", ex)
                    RecordException.recordException(ex)
                }
                if (photoFile != null) {
                    cameraPhotoPath = "file:" + photoFile.absolutePath
                    takePictureIntent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile)
                    )
                }
            }

            val contentSelectionIntent =
                Intent(Intent.ACTION_GET_CONTENT)

            val chooserIntent = Intent(Intent.ACTION_CHOOSER).apply {
                putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePictureIntent))
            }
            startActivityForResult(
                chooserIntent,
                CAMERA_REQUEST_CODE
            )
        } else {
            requestPermission(UserPermission.CAMERA)
        }
    }

    fun handleGalleryOpen() {
        if (checkPermission(UserPermission.STORAGE)) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(
                intent,
                STORAGE_REQUEST_CODE
            )
        } else {
            requestPermission(UserPermission.STORAGE)
        }
    }

    override fun onDestroy() {
        webview?.destroy()
        stopListeningSms()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        webview.onResume()
    }

    override fun onPause() {
        webview.onPause()
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // -------------------------  WebView callbacks -------------------------
    // ----------------------------------------------------------------------
    override fun backPress() {
        finish()
    }

    override fun pageBack() {
        runOnUiThread {
            if (webview.canGoBack()) {
                webview.goBack()
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun shareOnWhatsApp(msg: String, phone: String, url: String?) {
        Timber.i("Check_Webview_msg $msg")
        if (deviceUtils.get().isWhatsAppInstalled().not()) {
            shortToast(R.string.whatsapp_not_installed)
        } else {
            val localFileName = "web_share.jpg"
            val localFolderName = "web"

            communicationApi.get().goToWhatsApp(
                ShareIntentBuilder(
                    shareText = msg,
                    phoneNumber = if (phone.isEmpty()) null else phone,
                    imageFrom = if (url.isNullOrEmpty()) null else ImagePath.ImageUriFromRemote(
                        file = FileUtils.getLocalFile(this, localFileName, localFolderName),
                        localFolderName = localFolderName,
                        fileUrl = url,
                        localFileName = localFileName
                    )
                )
            ).map {
                startActivity(it)
            }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        }
    }

    override fun shareOnAnyApp(msg: String, imageUrl: String?) {
        if (msg.isEmpty() and imageUrl.isNullOrEmpty()) return
        WebShareUtils.getGeneralShareIntent(msg, imageUrl, this)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { intent, throwable ->
                run {
                    if (throwable != null) {
                        Toast.makeText(this, R.string.err_default, Toast.LENGTH_SHORT).show()
                    } else {
                        startActivity(intent)
                    }
                }
            }
    }

    override fun call(phone: String) {

        Permission.requestCallPermission(
            this,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                }

                override fun onPermissionGranted() {
                    Timber.d("Inside onPermissionGranted")
                    val intent = Intent(Intent.ACTION_CALL)
                    intent.data = Uri.parse(getString(R.string.call_template, phone))
                    startActivity(intent)
                }

                override fun onPermissionDenied() {
                }
            }
        )
    }

    override fun requestLocationPermission() {
        Permission.requestLocationPermission(
            this,
            object :
                IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                    tracker.get().trackRuntimePermission("Web Screen", PropertyValue.LOCATION, true)
                }

                override fun onPermissionGranted() {
                    GpsUtils(this@WebViewActivity).turnGPSOn(object : GpsUtils.onGpsListener {
                        override fun gpsStatus(isGPSEnable: Boolean) {
                            Timber.d("$TAG Start Location")
                            startLocationUpdates()
                        }
                    })
                }

                override fun onPermissionDenied() {
                    tracker.get().trackRuntimePermission("Web Screen", PropertyValue.LOCATION, false)
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        fusedLocationClient.lastLocation.addOnCompleteListener {
            lastKnownLocation = it.result
            Timber.d("$TAG Location Accessed from last location ${it.result?.latitude}")
            if (lastKnownLocation == null) {
                Timber.d("$TAG Requesting Location Access")
                requestNewLocationData()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest()

        locationRequest.apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    override fun getLocation(): String {
        return if (lastKnownLocation != null) {
            val longitude = lastKnownLocation?.longitude
            val latitude = lastKnownLocation?.latitude
            "$latitude,$longitude"
        } else {
            ""
        }
    }

    override fun requestSmsPermission() {
        Permission.requestSmsPermission(
            this,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {}

                override fun onPermissionGranted() {
                    webview.sendSmsPermissionEvent(true)
                    smsReceiverListener =
                        SmsBroadcastReceiver(
                            SOURCE_PAYTM, this@WebViewActivity,
                            object : SmsReceivedListener {
                                override fun onMessageReceived(msg: String) {
                                    webview.sendOtpEvent(msg)
                                }
                            }
                        )
                }

                override fun onPermissionDenied() {
                    webview.sendSmsPermissionEvent(false)
                }
            }
        )
    }

    override fun stopListeningSms() {
        smsReceiverListener?.stopListening()
    }

    override fun makeToast(msg: String) {
        longToast(msg)
    }

    override fun debug(msg: String) {
        Timber.d("Android Webview: %s", msg)
    }

    override fun navigate(deepLink: String) {
        legacyNavigator.get().goToDeeplinkScreen(this, deepLink)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, intent)
        var results: Array<Uri>? = null
        if (resultCode == Activity.RESULT_CANCELED) {
            Timber.d(TAG_ANDROID_WEBVIEW, "Result Cancelled")
        } else if (resultCode == Activity.RESULT_OK) {
            Timber.d(TAG_ANDROID_WEBVIEW, "Result ok")
            if (requestCode == CAMERA_REQUEST_CODE) {
                if (null == webviewFilePaths) {
                    return
                }
                results = arrayOf(Uri.parse(cameraPhotoPath))
            } else if (requestCode == STORAGE_REQUEST_CODE) {
                if (intent?.data != null) {
                    Timber.v(TAG_ANDROID_WEBVIEW, "Data from file chooser => ${intent.data!!.path}")
                    results = arrayOf(intent.data!!)
                }
            }
        }

        webviewFilePaths?.onReceiveValue(results)
        webviewFilePaths = null
    }

    private fun createImage(): File? {
        return try {
            val fileName =
                SimpleDateFormat("yyyy_mm_ss").format(Date())
            val newName = "file_${fileName}_"
            val sdDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            File.createTempFile(newName, ".jpg", sdDirectory)
        } catch (e: IOException) {
            RecordException.recordException(e)
            null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    longToast(R.string.camera_permission_denied)
                    tracker.get().trackRuntimePermission("Web Screen", PropertyValue.CAMERA, false)
                    webviewFilePaths?.onReceiveValue(null)
                    webviewFilePaths = null
                } else {
                    tracker.get().trackRuntimePermission("Web Screen", PropertyValue.CAMERA, true)
                    handleCameraUpload()
                }
            }
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    longToast(R.string.storage_permission_denied)
                    tracker.get().trackRuntimePermission("Web Screen", PropertyValue.STORAGE, false)
                    webviewFilePaths?.onReceiveValue(null)
                    webviewFilePaths = null
                } else {
                    tracker.get().trackRuntimePermission("Web Screen", PropertyValue.STORAGE, true)
                    handleGalleryOpen()
                }
            }
        }
    }

    fun loadInBrowser(request: WebResourceRequest): Boolean {
        val loadInBrowser = with(request.url) {
            getQueryParameter("browser")?.toBoolean()
                ?: path?.startsWith("upi://")
        }

        if (loadInBrowser == true) {
            kotlin.runCatching {
                val intent = Intent(Intent.ACTION_VIEW, request.url)
                startActivity(intent)
            }
        }

        return loadInBrowser == true
    }

    private fun getWebViewClientAccToAndroidVersion(): WebViewClient {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CustomWebViewClient(
                activity = WeakReference(this@WebViewActivity),
                startBootTime_ = startBootTime!!,
                webTracker_ = webTracker.get()
            )
        } else {
            CustomWebViewClientCompat(
                activity = WeakReference(this@WebViewActivity),
                startBootTime = startBootTime!!,
                webTracker = webTracker.get()
            )
        }
    }
}
