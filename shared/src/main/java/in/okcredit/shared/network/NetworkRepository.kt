package `in`.okcredit.shared.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class NetworkRepository @Inject constructor(private val connectivityManager: Lazy<ConnectivityManager>) {

    fun getConnectionStatus(): Observable<Boolean> {
        return Observable.create { emitter ->
            val connectivityManagerCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network?) {
                    emitter.onNext(true)
                }

                override fun onLost(network: Network?) {
                    emitter.onNext(false)
                }
            }
            val builder = NetworkRequest.Builder()
                .addTransportType(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(android.net.NetworkCapabilities.TRANSPORT_WIFI)
            connectivityManager.get().registerNetworkCallback(builder.build(), connectivityManagerCallback)
            emitter.setCancellable { connectivityManager.get().unregisterNetworkCallback(connectivityManagerCallback) }
        }
    }
}
