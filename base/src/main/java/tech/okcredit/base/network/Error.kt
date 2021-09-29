package tech.okcredit.base.network

import androidx.annotation.Keep
import com.google.gson.Gson
import tech.okcredit.android.base.error.check
import tech.okcredit.android.base.error.extract
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

// denotes any network related error (like dns failure, ssl handshake failure, timeouts, etc.)
@Keep
class NetworkError(error: String = "network_error", cause: Throwable) : IOException(error, cause)

@Keep
data class ApiError(val code: Int = 500, val error: String? = "unknown", val url: String? = "unknown") : IOException() {

    override val message: String
        get() = if (error.isNullOrBlank()) {
            "HTTP request failed (code=$code)"
        } else {
            error
        }

    fun mapError(errorMappings: Map<String, Throwable>): Throwable = errorMappings[error] ?: this

    fun mapError(vararg errorMappings: Pair<String, Throwable>): Throwable = mapError(errorMappings.toMap())

    fun mapError(errorMapping: Pair<String, Throwable>): Throwable = mapError(mapOf(errorMapping))

    fun mapCode(errorMappings: Map<Int, Throwable>): Throwable = errorMappings[code] ?: this

    fun mapCode(vararg errorMapping: Pair<Int, Throwable>): Throwable = mapCode(errorMapping.toMap())

    fun mapCode(errorMapping: Pair<Int, Throwable>): Throwable = mapCode(mapOf(errorMapping))
}

// helpers for building NetworkError
fun Throwable?.asNetworkError(): NetworkError? {
    if (check<NetworkError>()) return extract()

    val asNetworkException: (IOException) -> NetworkError? = {
        // all network related errors are sub classes of IOException

        when (it) {
            // Thrown to indicate that the IP address of a host could not be determined
            // DNS resolution failed
            is UnknownHostException -> NetworkError("unknown_host", it)

            // Signals that a timeout has occurred on a socket read or accept
            is SocketTimeoutException -> NetworkError("timeout", it)

            // Signals that an error occurred while attempting to connect a socket to a remote address and port
            is ConnectException -> NetworkError("conn_error", it)

            // Indicates that the client and server could not negotiate the desired level of security
            is SSLHandshakeException -> NetworkError("ssl_handshake_error", it)

            // Thrown to indicate that there is an error creating or accessing a Socket
            is SocketException -> NetworkError("network_error", it)

            // Indicates some kind of error detected by an SSL subsystem
            is SSLException -> NetworkError("ssl_error", it)

            else -> null
        }
    }

    var throwable = this
    while (throwable != null) {
        if (throwable is IOException) {
            val networkError = asNetworkException(throwable)
            if (networkError != null) return networkError
        }
        throwable = throwable.cause
    }
    return null
}

// helpers for building ApiError
fun okhttp3.Response.asError(): ApiError {
    if (this.isSuccessful) throw IllegalStateException("cannot parse ApiError from a successful api call")
    return try {
        Gson().fromJson(body?.string(), ApiError::class.java)!!
    } catch (e: Exception) {
        ApiError(code = code, url = this.request.url.toString())
    }
}

fun retrofit2.Response<*>.asError(): ApiError {
    if (this.isSuccessful) throw IllegalStateException("cannot parse ApiError from a successful api call")
    return try {
        Gson().fromJson(errorBody()?.string(), ApiError::class.java)!!
    } catch (e: Exception) {
        ApiError(code = code())
    }
}
