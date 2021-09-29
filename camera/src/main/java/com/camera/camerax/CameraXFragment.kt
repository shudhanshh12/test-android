package com.camera.camerax

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.Metadata
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.camera.R
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.camera_interaction_container.*
import kotlinx.android.synthetic.main.fragment_camerax.*
import tech.okcredit.base.exceptions.ExceptionUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraXFragment : Fragment(), CameraInteractionLayout.Interactor {

    private var foregroundRunnable: Runnable? = null
    private var foregroundNullRunnable: Runnable? = null
    var canCaptureImage = true
    private lateinit var mainExecutor: Executor
    private lateinit var viewFinder: PreviewView
    private lateinit var broadcastManager: LocalBroadcastManager
    private var displayId: Int = -1
    var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var listener: ICamera? = null
    private lateinit var cameraExecutor: ExecutorService
    var isFlashEnable = false

    private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@CameraXFragment.displayId) {
                imageCapture?.targetRotation = view.display.rotation
                imageAnalyzer?.targetRotation = view.display.rotation
            }
        } ?: Unit
    }

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        mainExecutor = ContextCompat.getMainExecutor(requireContext())
        cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())
    }

    @SuppressLint("NewApi")
    override fun onDestroyView() {
        foregroundNullRunnable?.let {
            camera_container?.removeCallbacks(foregroundNullRunnable)
        }
        foregroundRunnable?.let {
            camera_container?.removeCallbacks(foregroundRunnable)
        }
        super.onDestroyView()
        cameraExecutor.shutdown()
        displayManager.unregisterDisplayListener(displayListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_camerax, container, false)

    @SuppressLint("MissingPermission", "NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewFinder = view.findViewById(R.id.view_finder)
        broadcastManager = LocalBroadcastManager.getInstance(view.context)
        cameraExecutor = Executors.newSingleThreadExecutor()
        displayManager.registerDisplayListener(displayListener, null)
        viewFinder.post {
            displayId = viewFinder.display.displayId
            bindCameraUseCases()
        }
        camera_interaction_layout.addInteractor(this)
    }

    @SuppressLint("NewApi")
    private fun bindCameraUseCases() {
        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        val rotation = viewFinder.display.rotation
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            Runnable {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder()
                    .setTargetAspectRatio(screenAspectRatio)
                    .setTargetRotation(rotation)
                    .build()
                preview?.setSurfaceProvider(viewFinder.surfaceProvider)
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetAspectRatio(screenAspectRatio)
                    .setTargetRotation(rotation)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture
                    )
                } catch (exc: Exception) {
                    ExceptionUtils.logException(exc)
                }
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    companion object {
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(
                baseFolder,
                SimpleDateFormat(format, Locale.US)
                    .format(System.currentTimeMillis()) + extension
            )

        private var cameraX: CameraXFragment? = null
        fun getInstance(): CameraXFragment {

            return CameraXFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ICamera) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onClick() {
        if (canCaptureImage) {
            canCaptureImage = false
            imageCapture?.let { imageCapture ->
                val photoFile = CameraUtils.getFilePath(view?.context)
                val metadata = Metadata().apply {
                    isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
                }
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                    .setMetadata(metadata)
                    .build()
                imageCapture.takePicture(
                    outputOptions, cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                                requireActivity().sendBroadcast(
                                    Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri)
                                )
                            }
                            val mimeType = MimeTypeMap.getSingleton()
                                .getMimeTypeFromExtension(savedUri.toFile().extension)
                            MediaScannerConnection.scanFile(
                                context,
                                arrayOf(savedUri.toString()),
                                arrayOf(mimeType)
                            ) { _, uri ->
                            }
                            view?.post {
                                listener!!.onCameraCapturedImage(
                                    com.camera.models.models.Picture(
                                        savedUri.path!!,
                                        true
                                    )
                                )
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                camera_container.postDelayed(
                                    getForegroundRunnable(camera_container),
                                    ANIMATION_SLOW_MILLIS
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    /**
     * Not private so it can be called from an inner class without going
     * through a thunk.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    internal fun getForegroundNullRunnable(cameraContainer: RelativeLayout?): Runnable? {
        cameraContainer?.let {
            foregroundNullRunnable = Runnable {
                cameraContainer.foreground = null
            }
        }
        return foregroundNullRunnable
    }

    /**
     * Not private so it can be called from an inner class without going
     * through a thunk.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    internal fun getForegroundRunnable(cameraContainer: RelativeLayout?): Runnable? {
        cameraContainer?.let {
            foregroundRunnable = Runnable {
                cameraContainer.foreground = ColorDrawable(Color.WHITE)
                cameraContainer.postDelayed(
                    getForegroundNullRunnable(cameraContainer),
                    ANIMATION_FAST_MILLIS
                )
            }
        }
        return foregroundRunnable
    }

    override fun onFlashClicked(torchOn: Boolean) {

        if (!torchOn) {
            camera?.cameraControl?.enableTorch(false)?.addListener(
                Runnable {
                    activity?.let {
                        requireActivity().runOnUiThread {
                            camera_interaction_layout?.offTorch()
                        }
                    }
                },
                cameraExecutor
            )
        } else {
            camera?.cameraControl?.enableTorch(true)?.addListener(
                Runnable {
                    activity?.let {
                        requireActivity().runOnUiThread {
                            camera_interaction_layout?.onTorch()
                        }
                    }
                },
                cameraExecutor
            )
        }
    }

    override fun onBackClicked() {
        listener!!.goBack()
    }

    fun flipCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        bindCameraUseCases()
    }
}
