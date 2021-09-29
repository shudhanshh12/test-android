package `in`.okcredit.frontend.usecase.merchant

import `in`.okcredit.merchant.contract.BusinessErrors
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.Single
import java.io.IOException
import java.util.*
import javax.inject.Inject

class GetAddress @Inject constructor(private val context: Context) :
    UseCase<GetAddress.Request, GetAddress.Response> {

    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    override fun execute(req: Request): Observable<Result<Response>> {

        // user has dragged the map, we have center lat long
        // we would auto fetch address of the lat long
        if (req.centerPositionAddress) {

            return if (req.latlong == null || (req.latlong.latitude == 0.0 && req.latlong.longitude == 0.0)) {
                UseCase.wrapSingle(
                    getCurrentLocation()
                        .flatMap {
                            getCorrectedAddress(it)
                        }
                )
            } else {
                UseCase.wrapSingle(
                    getAddress(req.latlong!!)
                )
            }
        } else {
            // no address was saved by merchant, so we get current location
            if (req.address.isNullOrBlank() && (req.latlong == null || (req.latlong.latitude == 0.0 && req.latlong.longitude == 0.0))) {
                return UseCase.wrapSingle(
                    getCurrentLocation()
                        .flatMap {
                            getAddress(it)
                        }
                )
            }

            // manual address was entered by merchant, we don't have lat long
            else if (req.address.isNullOrBlank().not() && (req.latlong == null || (req.latlong.latitude == 0.0 && req.latlong.longitude == 0.0))) {
                return UseCase.wrapSingle(
                    getLatLong(manualAddress = req.address!!)
                )
            }

            // location permission was granted at the time of saving address , so we have proper lat long and address auto fetched by location api
            else {
                return UseCase.wrapSingle(
                    Single.just(Response(address = req.address, latlong = req.latlong!!))
                )
            }
        }
    }

    data class Response(val address: String? = null, val latlong: LatLng, val correctedLocation: Boolean = false)
    data class Request(val address: String? = null, val latlong: LatLng? = null, val centerPositionAddress: Boolean = false, val correctedLocation: Boolean = false)

    private fun getAddress(latlong: LatLng): Single<Response> {

        return Single.create { single ->

            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>?
            try {
                addresses = geocoder.getFromLocation(latlong.latitude, latlong.longitude, 1)
                single.onSuccess(Response(latlong = latlong, address = addresses[0].getAddressLine(0)))
            } catch (e: IOException) {
                single.onError(BusinessErrors.ServiceNotAvailable())
            } catch (e: Exception) {
                single.onError(BusinessErrors.NoInternet())
            }
        }
    }

    private fun getCorrectedAddress(latlong: LatLng): Single<Response> {

        return Single.create { single ->

            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>?
            try {
                addresses = geocoder.getFromLocation(latlong.latitude, latlong.longitude, 1)
                single.onSuccess(
                    Response(
                        latlong = latlong,
                        address = addresses[0].getAddressLine(0),
                        correctedLocation = true
                    )
                )
            } catch (e: IOException) {
                single.onError(BusinessErrors.ServiceNotAvailable())
            } catch (e: Exception) {
                single.onError(BusinessErrors.NoInternet())
            }
        }
    }

    // we try to get LatLng from address manually enter by merchant
    // there is very less possibility of getting LatLng from this address
    // because , merchant may have entered short address (Geocoder needs proper address to fetch lat long

    private fun getLatLong(manualAddress: String): Single<Response> {

        return Single.create { single ->
            val coder = Geocoder(context)
            val address: List<Address>?

            try {
                address = coder.getFromLocationName(manualAddress, 5)
                if (address.isNullOrEmpty().not()) {
                    val location = address[0]
                    single.onSuccess(Response(manualAddress, LatLng(location.latitude, location.longitude)))
                } else {
                    single.onError(BusinessErrors.LatLongNotFound())
                }
            } catch (e: IOException) {
                single.onError(BusinessErrors.ServiceNotAvailable())
            } catch (e: Exception) {
                single.onError(BusinessErrors.NoInternet())
            }
        }
    }

    private fun getCurrentLocation(): Single<LatLng> {
        return Single.create { single ->
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    single.onSuccess(LatLng(location.latitude, location.longitude))
                } else {
                    single.onSuccess(LatLng(0.0, 0.0))
                }
            }
        }
    }
}
