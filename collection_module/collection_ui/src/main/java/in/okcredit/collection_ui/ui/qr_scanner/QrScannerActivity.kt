package `in`.okcredit.collection_ui.ui.qr_scanner

import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Screen
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.collection.contract.CollectionConstants
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.analytics.CollectionTracker.CollectionPropertyValue
import `in`.okcredit.collection_ui.databinding.QrScannerLayoutBinding
import `in`.okcredit.collection_ui.ui.qr_scanner.QRScannerContract.Intent
import `in`.okcredit.collection_ui.ui.qr_scanner.QRScannerContract.State
import `in`.okcredit.merchant.collection.analytics.CollectionTraces
import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.CommonUtils
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.content.ContextCompat
import com.google.firebase.perf.metrics.AddTrace
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.jakewharton.rxbinding3.view.clicks
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.base.exceptions.ExceptionUtils
import timber.log.Timber
import zendesk.belvedere.Belvedere
import zendesk.belvedere.Callback
import zendesk.belvedere.MediaResult
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class QrScannerActivity : BaseActivity<State, QRScannerContract.ViewEvent, Intent>(label = "QRScannerScreen") {

    internal var lastSacnUpiResult = ""
    internal lateinit var beepManager: BeepManager
    private val tasks = CompositeDisposable()

    private val binding: QrScannerLayoutBinding by viewLifecycleScoped(QrScannerLayoutBinding::inflate)

    @Inject
    internal lateinit var tracker: Tracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            backIcon.setOnClickListener {
                finish()
            }

            gallery.setOnClickListener {
                tracker.trackEvents(
                    CollectionTracker.CollectionEvent.CLICKED_GALLERY,
                    type = CollectionPropertyValue.UPI,
                    screen = CollectionTracker.CollectionScreen.QR_SCANNER_SCREEN
                )
                getStoragePermissionAndOpenGallery(getCurrentState())
            }
        }
    }

    @AddTrace(name = CollectionTraces.RENDER_QR_SCANNER_SCREEN)
    override fun onResume() {
        super.onResume()

        binding.apply {
            /******* QR Code initialization ********/
            run {
                val formats = listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
                barcodeScanner.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
                barcodeScanner.initializeFromIntent(intent)
                barcodeScanner.decodeContinuous(callback)
                barcodeScanner.statusView.visibility = View.GONE
                beepManager = BeepManager(this@QrScannerActivity)
            }

            barcodeScanner.resume()
        }
    }

    override fun onPause() {
        super.onPause()

        if (!tasks.isDisposed) {
            tasks.dispose()
        }

        binding.barcodeScanner.pause()
    }
    //endregion

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            binding.torch.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    val isTorchOn = getCurrentState().isTorchOn
                    tracker.trackEvents(
                        CollectionTracker.CollectionEvent.CLICKED_TORCH,
                        type = CollectionPropertyValue.UPI,
                        screen = CollectionTracker.CollectionScreen.QR_SCANNER_SCREEN,
                        propertiesMap = PropertiesMap.create()
                            .add(CollectionTracker.CollectionPropertyKey.IS_TORCH_ON, isTorchOn.not())
                    )
                    Intent.SetTorchOn(isTorchOn.not())
                }
        )
    }

    @AddTrace(name = CollectionTraces.RENDER_QR_SCANNER_SCREEN)
    override fun render(state: State) {
        if (state.isTorchOn) {
            binding.barcodeScanner.setTorchOn()
            binding.torch.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_torch_white_on))
        } else {
            binding.barcodeScanner.setTorchOff()
            binding.torch.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_torch_white_off))
        }
    }

    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (!result.text.isNullOrEmpty() && lastSacnUpiResult != result.text) {
                lastSacnUpiResult = result.text
                beepManager.playBeepSoundAndVibrate()
                Timber.d("<<<<Result path: %s", result.text)

                val upiVpa = CommonUtils.parseUpiVpaFromURl(lastSacnUpiResult)
                if (!upiVpa.isNullOrEmpty()) {
                    Timber.d("<<<<Result upiVpa: %s", upiVpa)

                    returnUpiVpaResult(upiVpa, CollectionPropertyValue.CAMERA)
                } else {
                    longToast(R.string.upi_decode_error)
                }
                return
            }
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    internal fun returnUpiVpaResult(upiVpa: String?, scanMethod: String) {
        val resultIntent = android.content.Intent().apply {
            putExtra(CollectionConstants.UPI_ID, upiVpa)
            putExtra(CollectionConstants.METHOD, scanMethod)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun getStoragePermissionAndOpenGallery(state: State) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            tracker.trackEvents(
                CollectionTracker.CollectionEvent.VIEW_STORAGE_PERMISSION,
                type = PropertyValue.STORAGE,
                screen = Screen.QR_SCANNER_SCREEN
            )
        }
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    openGallery()

                    trackEvents(report, state)
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?,
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    internal fun openGallery() {
        Belvedere.from(this)
            .document()
            .contentType("image/*")
            .allowMultiple(false)
            .open(this)
    }

    //region events

    internal fun trackEvents(
        report: MultiplePermissionsReport,
        state: State,
    ) {
        if (report.areAllPermissionsGranted()) {
            if (state.isStoragePermissionGranted) return
            trackAcceptedPermission()
        } else {
            trackPermissionDenied()
            longToast(R.string.camera_permission_denied)
        }
    }

    internal fun trackPermissionDenied() {
        tracker.trackEvents(
            AnalyticsEvents.PERMISSION_DENIED,
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.SCREEN, Screen.QR_SCANNER_SCREEN)
                .add(PropertyKey.TYPE, "storage")
        )
    }

    internal fun trackAcceptedPermission() {
        tracker.trackEvents(
            AnalyticsEvents.PERMISSION_ACCEPT,
            propertiesMap = PropertiesMap.create()
                .add(Screen.QR_SCANNER_SCREEN, Screen.QR_SCANNER_SCREEN)
                .add(PropertyKey.TYPE, "storage")
        )
    }
    //endregion

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            Belvedere.from(this).getFilesFromActivityOnResult(
                requestCode,
                resultCode,
                data,
                object : Callback<List<MediaResult>>() {
                    override fun success(result: List<MediaResult>) {
                        if (result.isNotEmpty()) {
                            result[0].file?.let { file ->
                                val resultUri: Uri = Uri.fromFile(file)

                                try {
                                    val bm = MediaStore.Images.Media.getBitmap(contentResolver, resultUri)
                                    val width = bm.width
                                    val height = bm.height
                                    val pixels = IntArray(width * height)
                                    bm.getPixels(pixels, 0, width, 0, 0, width, height)

                                    val source = RGBLuminanceSource(width, height, pixels)
                                    val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                                    val reader = QRCodeReader()

                                    try {
                                        val text = reader.decode(binaryBitmap).text
                                        val upiVpa = CommonUtils.parseUpiVpaFromURl(text)
                                        if (!upiVpa.isNullOrEmpty()) {
                                            Timber.d("<<<<Result gallery upiVpa: %s", upiVpa)
                                            returnUpiVpaResult(upiVpa, CollectionPropertyValue.GALLERY)
                                        } else {
                                            longToast(R.string.upi_decode_error)
                                        }
                                    } catch (e: Exception) {
                                        longToast(R.string.upi_decode_error)
                                    }
                                } catch (e: Exception) {
                                    Timber.d(e)
                                    ExceptionUtils.logException("Error: Upi Add From Gallery", e)
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    override fun handleViewEvent(event: QRScannerContract.ViewEvent) {
    }
}
