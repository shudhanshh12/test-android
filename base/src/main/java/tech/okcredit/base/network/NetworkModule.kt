package tech.okcredit.base.network

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.time.DateTime
import tech.okcredit.android.base.BuildConfig
import tech.okcredit.android.base.flipper.FlipperUtils
import tech.okcredit.base.network.utils.TrackNetworkPerformanceBinding
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.roundToLong

@dagger.Module
abstract class NetworkModule {

    companion object {

        // default end to end timeout (in seconds) for a http request
        private const val DEFAULT_E2E_TIMEOUT: Long = 120

        // default timeout (in seconds) for connect, read, and write stages of a http request
        private const val DEFAULT_TIMEOUT: Long = 30

        // default timeout (in seconds) for connect, read, and write stages of a http request
        private const val LONG_DEFAULT_TIMEOUT: Long = 120

        // default ping interval (in seconds)
        private const val DEFAULT_PING_INTERVAL: Long = 10

        // default max http requests
        private const val DEFAULT_MAX_HTTP_REQUESTS = 64

        // Firebase remote config key for default retry count for network error. Also, disable retry for debug builds
        private const val NETWORK_ERROR_MAX_TRY_COUNT_FIREBASE_REMOTE_CONFIG_KEY = "network_error_max_try_count"

        // Firebase remote config key for default retry backoff delay in millis for network error
        private const val NETWORK_ERROR_RETRY_BACKOFF_DELAY_FIREBASE_REMOTE_CONFIG_KEY =
            "network_error_retry_backoff_delay_millis"

        // max try count for debug builds, has to be > 0
        private const val NETWORK_ERROR_MAX_TRY_COUNT_FOR_DEBUG_BUILD = 1

        // format for http header `Date`
        private const val FORMAT_HEADER_DATE = "EEE, dd MMM yyyy HH:mm:ss zzz"

        // default locale
        private val DEFAULT_LOCALE = Locale("en")

        // standard headers
        private const val HEADER_REQUEST_ID = "X-Request-ID"
        private const val HEADER_DATE = "Date"
        private const val HEADER_APP_VERSION = "OKC_APP_VERSION"
        private const val HEADER_USER_AGENT = "User-Agent"

        // sampling rate of network instrumentation
        const val NETWORK_INSTRUMENTATION_SAMPLING = "network_instrumentation_sampling"

        internal val loggingInterceptor by lazy {
            HttpLoggingInterceptor { message -> Timber.tag("http").v(message) }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        }

        internal val stdHeadersInterceptor by lazy {
            Interceptor {
                it.proceed(
                    it.request()
                        .newBuilder()
                        .header(HEADER_REQUEST_ID, UUID.randomUUID().toString())
                        .header(HEADER_DATE, DateTime.now().toString(FORMAT_HEADER_DATE, DEFAULT_LOCALE))
                        .header(HEADER_USER_AGENT, "${BuildConfig.LIBRARY_PACKAGE_NAME}/${BuildConfig.VERSION_CODE}")
                        .header(HEADER_APP_VERSION, BuildConfig.VERSION_CODE.toString())
                        .build()
                )
            }
        }

        private var networkInterceptor: Interceptor? = null

        private fun getNetworkErrorInterceptor(maxTryCount: Int, retryBackoffDelayInMillis: Int): Interceptor {
            if (networkInterceptor != null) return networkInterceptor!!

            networkInterceptor = Interceptor { chain ->
                // Cannot have non-blocking implementation for retry delay: https://github.com/square/okhttp/issues/3714
                val request = chain.request()
                lateinit var response: Response
                var tryCount = 0

                do {
                    try {
                        // Exponential backoff
                        Thread.sleep(retryBackoffDelayInMillis * (2.0.pow(tryCount).roundToLong() - 1))

                        response = chain.proceed(request)
                        return@Interceptor response
                    } catch (exception: IOException) {
                        // Throw error only if this was the last try
                        if (tryCount + 1 == maxTryCount) {
                            val networkError = exception.asNetworkError()
                            if (networkError != null) throw networkError
                            throw exception
                        }
                    } finally {
                        tryCount++
                    }
                } while (tryCount < maxTryCount)
                response
            }

            return networkInterceptor!!
        }

        @Provides
        @DefaultOkHttpClient
        fun createHttpClient(
            trackNetworkPerformanceBinding: Lazy<TrackNetworkPerformanceBinding>,
            firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
        ): OkHttpClient =
            OkHttpClient
                .Builder()
                .apply {
                    run {
                        val sampling = firebaseRemoteConfig.get().getLong(NETWORK_INSTRUMENTATION_SAMPLING).toInt()
                        if ((0..99).random() < sampling) {
                            eventListenerFactory { EventListener(trackNetworkPerformanceBinding) }
                        }
                    }
                    callTimeout(DEFAULT_E2E_TIMEOUT, TimeUnit.SECONDS)
                    connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    pingInterval(DEFAULT_PING_INTERVAL, TimeUnit.SECONDS)
                    retryOnConnectionFailure(true)
                    addInterceptor(
                        getNetworkErrorInterceptor(
                            if (BuildConfig.DEBUG) NETWORK_ERROR_MAX_TRY_COUNT_FOR_DEBUG_BUILD else
                                firebaseRemoteConfig.get()
                                    .getLong(NETWORK_ERROR_MAX_TRY_COUNT_FIREBASE_REMOTE_CONFIG_KEY)
                                    .toInt(),
                            firebaseRemoteConfig.get()
                                .getLong(NETWORK_ERROR_RETRY_BACKOFF_DELAY_FIREBASE_REMOTE_CONFIG_KEY)
                                .toInt()
                        )
                    )
                    addInterceptor(stdHeadersInterceptor)
                    addNetworkInterceptor(loggingInterceptor)

                    val flipperNetworkInterceptor = FlipperUtils.networkInterceptor
                    flipperNetworkInterceptor?.let { addNetworkInterceptor(flipperNetworkInterceptor) }
                }
                .build()
                .apply {
                    dispatcher.apply {
                        maxRequests = DEFAULT_MAX_HTTP_REQUESTS
                        maxRequestsPerHost = DEFAULT_MAX_HTTP_REQUESTS
                    }
                }

        @Provides
        @LongOkHttpClient
        fun createLongHttpClient(
            firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
        ): OkHttpClient =
            OkHttpClient
                .Builder()
                .apply {
                    callTimeout(DEFAULT_E2E_TIMEOUT, TimeUnit.SECONDS)
                    connectTimeout(LONG_DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    readTimeout(LONG_DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    writeTimeout(LONG_DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    pingInterval(DEFAULT_PING_INTERVAL, TimeUnit.SECONDS)
                    retryOnConnectionFailure(true)
                    addInterceptor(
                        getNetworkErrorInterceptor(
                            if (BuildConfig.DEBUG) NETWORK_ERROR_MAX_TRY_COUNT_FOR_DEBUG_BUILD else
                                firebaseRemoteConfig.get()
                                    .getLong(NETWORK_ERROR_MAX_TRY_COUNT_FIREBASE_REMOTE_CONFIG_KEY)
                                    .toInt(),
                            firebaseRemoteConfig.get()
                                .getLong(NETWORK_ERROR_RETRY_BACKOFF_DELAY_FIREBASE_REMOTE_CONFIG_KEY)
                                .toInt()
                        )
                    )
                    addInterceptor(stdHeadersInterceptor)
                    addNetworkInterceptor(loggingInterceptor)

                    val flipperNetworkInterceptor = FlipperUtils.networkInterceptor
                    flipperNetworkInterceptor?.let { addNetworkInterceptor(flipperNetworkInterceptor) }
                }
                .build()
                .apply {
                    dispatcher.apply {
                        maxRequests = DEFAULT_MAX_HTTP_REQUESTS
                        maxRequestsPerHost = DEFAULT_MAX_HTTP_REQUESTS
                    }
                }
    }
}
