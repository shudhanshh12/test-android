package tech.okcredit.android.base.utils

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber
import java.io.IOException
import java.util.*

class GpsUtils constructor(private val context: Activity) {
    private val mSettingsClient: SettingsClient = LocationServices.getSettingsClient(context)
    private val mLocationSettingsRequest: LocationSettingsRequest
    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val locationRequest: LocationRequest = LocationRequest.create()

    init {

        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = (10 * 1000).toLong()
        locationRequest.fastestInterval = (2 * 1000).toLong()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        mLocationSettingsRequest = builder.build()

        // **************************
        builder.setAlwaysShow(true) // this is the key ingredient
        // **************************
    }

    fun isGPSOn(): Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    // method for turn on GPS
    fun turnGPSOn(onGpsListener: onGpsListener?) {

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            onGpsListener?.gpsStatus(true)
        } else {
            mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(context as Activity) {
                    //  GPS is already enable, callback GPS status through listener
                    onGpsListener?.gpsStatus(true)
                }
                .addOnFailureListener(context) { e ->
                    val statusCode = (e as ApiException).statusCode
                    when (statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->

                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().

                                // context.startIntentSenderForResult(rae.resolution.intentSender, REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null)

                                val rae = e as ResolvableApiException
                                rae.startResolutionForResult(
                                    context,
                                    GPS_REQUEST
                                )
                            } catch (sie: IntentSender.SendIntentException) {
                                Timber.i("PendingIntent unable to execute request.")
                            }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            val errorMessage = "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
                            Timber.e(errorMessage)

                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }
    }

    fun getAddress(lat: Double, long: Double): String {
        var address = ""
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>?
            addresses = geocoder.getFromLocation(lat, long, 1)
            if (addresses.isNullOrEmpty().not()) {
                address = addresses[0].getAddressLine(0)
            }
        } catch (e: Exception) {
        }
        return address
    }

    // we try to get LatLng from address manually enter by merchant
    // there is very less possibility of getting LatLng from this address
    // because , merchant may have entered short address (Geocoder needs proper address to fetch lat long)
    fun getLatLong(manualAddress: String): LatLng {
        val coder = Geocoder(context)
        val address: List<Address>?
        var latlong = LatLng(0.0, 0.0)

        try {
            // May throw an IOException
            address = coder.getFromLocationName(manualAddress, 5)
            if (address == null) {
                latlong
            }
            if (address.isNullOrEmpty().not()) {
                val location = address[0]
                latlong = LatLng(location.latitude, location.longitude)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        return latlong
    }

    interface onGpsListener {
        fun gpsStatus(isGPSEnable: Boolean)
    }

    companion object {
        const val GPS_REQUEST = 1001
    }
}
