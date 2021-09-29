package tech.okcredit.base.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.util.*

object Permission {

    /*************************** Storage  **********************************/
    private val storagePermission = arrayListOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val storageAndCameraPermission = arrayListOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA
    )

    fun isStoragePermissionAlreadyGranted(context: Context): Boolean {
        return context.checkCallingOrSelfPermission(storagePermission[0]) == PackageManager.PERMISSION_GRANTED
    }

    fun requestStoragePermission(activity: FragmentActivity, listener: IPermissionListener) {
        requestMultiplePermission(activity, storagePermission, listener)
    }

    fun requestStorageAndCameraPermission(activity: FragmentActivity, listener: IPermissionListener) {
        requestMultiplePermission(activity, storageAndCameraPermission, listener)
    }

    /*************************** Location  **********************************/
    private val locationPermission = arrayListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun requestLocationPermission(activity: FragmentActivity, listener: IPermissionListener) {
        requestMultiplePermission(activity, locationPermission, listener)
    }

    fun isLocationPermissionAlreadyGranted(context: Context): Boolean {
        return context.checkCallingOrSelfPermission(locationPermission[0]) == PackageManager.PERMISSION_GRANTED
    }

    /*************************** Contact  **********************************/

    private val contactPermission = arrayListOf(
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_CONTACTS
    )

    fun isContactPermissionAlreadyGranted(context: Context): Boolean {
        return context.checkCallingOrSelfPermission(contactPermission[1]) == PackageManager.PERMISSION_GRANTED
    }

    fun isContactWritePermissionAlreadyGranted(context: Context): Boolean {
        return context.checkCallingOrSelfPermission(contactPermission[0]) == PackageManager.PERMISSION_GRANTED
    }

    fun requestContactPermission(
        activity: FragmentActivity,
        listener: IPermissionListener,
    ) {
        requestMultiplePermission(activity, contactPermission, listener)
    }

    /*************************** SMS  **********************************/
    private const val smsReceivePermission = Manifest.permission.RECEIVE_SMS

    fun requestSmsPermission(activity: FragmentActivity, listener: IPermissionListener) {
        requestSinglePermission(smsReceivePermission, listener, activity)
    }

    /*************************** Call  **********************************/
    private const val callPermission = Manifest.permission.CALL_PHONE

    fun requestCallPermission(activity: FragmentActivity, listener: IPermissionListener) {
        requestSinglePermission(callPermission, listener, activity)
    }

    /*************************** Camera  **********************************/
    private const val cameraPermission = Manifest.permission.CAMERA

    fun requestCameraPermission(activity: FragmentActivity, listener: IPermissionListener) {
        requestSinglePermission(cameraPermission, listener, activity)
    }

    /*************************** Record Audio  **********************************/
    private const val recordAudioPermission = Manifest.permission.RECORD_AUDIO

    fun isRecordAudioPermissionAlreadyGranted(context: Context): Boolean {
        return context.checkCallingOrSelfPermission(recordAudioPermission) == PackageManager.PERMISSION_GRANTED
    }

    fun requestRecordAudioPermission(activity: FragmentActivity, listener: IPermissionListener) {

        requestSinglePermission(recordAudioPermission, listener, activity)
    }

    private fun requestMultiplePermission(
        activity: FragmentActivity,
        permissionList: ArrayList<String>,
        listener: IPermissionListener
    ) {
        // `isPermissionGranted` helps to check checking permission granted for first time.
        var isPermissionGranted = true
        if (permissionList.size > 0) {
            for (permission in permissionList) {
                if (activity.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    isPermissionGranted = false
                    break
                }
            }
        }

        Dexter.withActivity(activity)
            .withPermissions(permissionList)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    when {
                        report.isAnyPermissionPermanentlyDenied -> {
                            listener.onPermissionPermanentlyDenied()
                            showEnablePermissionDialog(activity = activity)
                        }
                        report.areAllPermissionsGranted() -> {
                            if (!isPermissionGranted) {
                                listener.onPermissionGrantedFirstTime()
                            }
                            listener.onPermissionGranted()
                        }
                        else -> {
                            listener.onPermissionDenied()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener {}.check()
    }

    private fun requestSinglePermission(
        permission: String,
        listener: IPermissionListener,
        activity: FragmentActivity
    ) {
        // `isPermissionGranted` helps to check checking permission granted for first time.
        var isPermissionGranted = false
        if (activity.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = true
        }
        Dexter.withActivity(activity)
            .withPermission(permission)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    if (!isPermissionGranted) {
                        listener.onPermissionGrantedFirstTime()
                    }
                    listener.onPermissionGranted()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    when {
                        response.isPermanentlyDenied -> {
                            listener.onPermissionPermanentlyDenied()
                            showEnablePermissionDialog(activity)
                        }
                        else -> listener.onPermissionDenied()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener { }.check()
    }

    // If permission permanently disable , this dialog takes user to app setting page to turn on permission manually
    internal fun showEnablePermissionDialog(activity: FragmentActivity) {
        PermissionDialog.show(
            activity,
            object : PermissionDialog.Listener {
                override fun onOpenSetting() {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:" + activity.packageName)
                    activity.startActivity(intent)
                }
            }
        ).show()
    }
}
