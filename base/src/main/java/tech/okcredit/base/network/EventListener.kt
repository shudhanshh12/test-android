package tech.okcredit.base.network

import dagger.Lazy
import okhttp3.*
import okhttp3.EventListener
import org.joda.time.DateTime
import tech.okcredit.base.network.utils.TrackNetworkPerformanceBinding
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

class EventListener(
    private val trackNetworkPerformanceBinding: Lazy<TrackNetworkPerformanceBinding>
) : EventListener() {

    companion object {
        const val CALL_START = "callStart"

        const val PROXY_SELECT_START = "proxySelectStart"
        const val PROXY_SELECT_END = "proxySelectEnd"

        const val DNS_START = "dnsStart"
        const val DNS_END = "dnsEnd"

        const val CONNECT_START = "connectStart"
        const val SECURE_CONNECT_START = "secureConnectStart"
        const val SECURE_CONNECT_END = "secureConnectEnd"

        const val CONNECT_END = "connectEnd"
        const val CONNECT_FAILED = "connectFailed"
        const val CONNECTION_ACQUIRED = "connectionAcquired"
        const val CONNECTION_RELEASED = "connectionReleased"

        const val REQUEST_HEADERS_START = "requestHeadersStart"
        const val REQUEST_HEADERS_END = "requestHeadersEnd"
        const val REQUEST_BODY_START = "requestBodyStart"
        const val REQUEST_BODY_END = "requestBodyEnd"
        const val REQUEST_FAILED = "requestFailed"

        const val RESPONSE_HEADERS_START = "responseHeadersStart"
        const val RESPONSE_HEADERS_END = "responseHeadersEnd"
        const val RESPONSE_BODY_START = "responseBodyStart"
        const val RESPONSE_BODY_END = "responseBodyEnd"
        const val RESPONSE_FAILED = "responseFailed"

        const val CALL_END = "callEnd"
        const val CALL_FAILED = "callFailed"

        const val EventName = "Network Performance"
    }

    val startTime = DateTime.now().millis

    val listValues = mutableListOf<Pair<String, Long>>()
    private var host: String? = null
    private var encodedPath: String? = null
    private var method: String? = null
    private var requestBodyLength: Long? = null
    private var responseBodyLength: Long? = null
    private var isSuccessFull: Boolean? = null
    private var statusCode: Int? = null
    private var errorMessage: String? = null
    private var failureReason: String? = null
    private var protocol: String? = null

    fun printEvent(name: String, url: String) {
        val parsedUrl = removeParams(removeUUIDFromUrl(url))
        listValues.add((name to DateTime.now().millis - startTime))

        val eventProps = mutableMapOf<String, Any>()
        if (name == CALL_END || name == REQUEST_FAILED || name == CONNECT_FAILED || name == RESPONSE_FAILED ||
            name == CALL_FAILED
        ) {
            eventProps.setValue("End Point", url)
            eventProps.setValue("Filtered URL", parsedUrl)
            eventProps.setValue("Host", host)
            eventProps.setValue("Path", encodedPath)
            eventProps.setValue("Method", method)
            eventProps.setValue("RequestBody Length", requestBodyLength)
            eventProps.setValue("ResponseBody Length", responseBodyLength)
            eventProps.setValue("Success", isSuccessFull)
            eventProps.setValue("Status Code", statusCode)
            eventProps.setValue("Error Message", errorMessage)
            eventProps.setValue("Protocol", protocol)

            val overallDuration = findTimeDifferenceBetweenEvents(CALL_START, name)
            overallDuration?.let {
                eventProps["Duration"] = it
            }

            if (name == CALL_END) {
                val dnsDuration = findTimeDifferenceBetweenEvents(DNS_START, DNS_END)
                dnsDuration?.let {
                    eventProps["DNS LookUp Duration"] = it
                }

                val proxyDuration = findTimeDifferenceBetweenEvents(PROXY_SELECT_START, PROXY_SELECT_END)
                proxyDuration?.let {
                    eventProps["Select Proxy Duration"] = it
                }

                val connectionAcquiredDuration = findTimeDifferenceBetweenEvents(CONNECT_START, CONNECTION_ACQUIRED)
                connectionAcquiredDuration?.let {
                    eventProps["Connection Acquired Duration"] = it
                }

                val secureConnectionDuration = findTimeDifferenceBetweenEvents(SECURE_CONNECT_START, SECURE_CONNECT_END)
                secureConnectionDuration?.let {
                    eventProps["Secure Connection Duration"] = it
                }

                val requestHeadersDuration = findTimeDifferenceBetweenEvents(REQUEST_HEADERS_START, REQUEST_HEADERS_END)
                requestHeadersDuration?.let {
                    eventProps["Request Headers Duration"] = it
                }

                val responseHeadersDuration = findTimeDifferenceBetweenEvents(
                    RESPONSE_HEADERS_START,
                    RESPONSE_HEADERS_END
                )
                responseHeadersDuration?.let {
                    eventProps["Response Headers Duration"] = it
                }

                val responseBodyDuration = findTimeDifferenceBetweenEvents(RESPONSE_BODY_START, RESPONSE_BODY_END)
                responseBodyDuration?.let {
                    eventProps["Response Body Duration"] = it
                }
            }

            trackNetworkPerformanceBinding.get().trackNetworkPerformance(
                EventName,
                eventProps
            )
        }
    }

    fun findTimeDifferenceBetweenEvents(startEvent: String, endEvent: String): Long? {
        var startTime: Long? = null
        var endTime: Long? = null

        listValues.forEach lit@{
            if (it.first == startEvent && startTime == null) {
                startTime = it.second
            } else if (it.first == endEvent && endTime == null) {
                endTime = it.second
            }
        }

        if (startTime != null && endTime != null) {
            return endTime!! - startTime!!
        }

        return null
    }

    override fun callStart(call: Call) {
        host = call.request().url.host
        encodedPath = call.request().url.encodedPath
        method = call.request().method
        requestBodyLength = call.request().body?.contentLength()
        printEvent(CALL_START, call.request().url.toString())
    }

    override fun proxySelectStart(call: Call, url: HttpUrl) {
        printEvent(PROXY_SELECT_START, call.request().url.toString())
    }

    override fun proxySelectEnd(
        call: Call,
        url: HttpUrl,
        proxies: List<Proxy>
    ) {
        printEvent(PROXY_SELECT_END, call.request().url.toString())
    }

    override fun dnsStart(call: Call, domainName: String) {
        printEvent(DNS_START, call.request().url.toString())
    }

    override fun dnsEnd(
        call: Call,
        domainName: String,
        inetAddressList: List<InetAddress>
    ) {
        printEvent(DNS_END, call.request().url.toString())
    }

    override fun connectStart(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy
    ) {
        printEvent(CONNECT_START, call.request().url.toString())
    }

    override fun secureConnectStart(call: Call) {
        printEvent(SECURE_CONNECT_START, call.request().url.toString())
    }

    override fun secureConnectEnd(call: Call, handshake: Handshake?) {
        printEvent(SECURE_CONNECT_END, call.request().url.toString())
    }

    override fun connectEnd(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy,
        protocol: Protocol?
    ) {
        printEvent(CONNECT_END, call.request().url.toString())
    }

    override fun connectFailed(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy,
        protocol: Protocol?,
        ioe: IOException
    ) {
        isSuccessFull = false
        errorMessage = ioe.message
        failureReason = "Connect Failed"
        printEvent(CONNECT_FAILED, call.request().url.toString())
    }

    override fun connectionAcquired(call: Call, connection: Connection) {
        printEvent(CONNECTION_ACQUIRED, call.request().url.toString())
    }

    override fun connectionReleased(call: Call, connection: Connection) {
        printEvent(CONNECTION_RELEASED, call.request().url.toString())
    }

    override fun requestHeadersStart(call: Call) {
        printEvent(REQUEST_HEADERS_START, call.request().url.toString())
    }

    override fun requestHeadersEnd(call: Call, request: Request) {
        printEvent(REQUEST_HEADERS_END, call.request().url.toString())
    }

    override fun requestBodyStart(call: Call) {
        printEvent(REQUEST_BODY_START, call.request().url.toString())
    }

    override fun requestBodyEnd(call: Call, byteCount: Long) {
        responseBodyLength = byteCount
        printEvent(REQUEST_BODY_END, call.request().url.toString())
    }

    override fun requestFailed(call: Call, ioe: IOException) {
        isSuccessFull = false
        errorMessage = ioe.message
        failureReason = "Request Failed"
        printEvent(REQUEST_FAILED, call.request().url.toString())
    }

    override fun responseHeadersStart(call: Call) {
        printEvent(RESPONSE_HEADERS_START, call.request().url.toString())
    }

    override fun responseHeadersEnd(call: Call, response: Response) {
        protocol = response.protocol.name
        statusCode = response.code
        isSuccessFull = response.isSuccessful
        printEvent(RESPONSE_HEADERS_END, call.request().url.toString())
    }

    override fun responseBodyStart(call: Call) {
        printEvent(RESPONSE_BODY_START, call.request().url.toString())
    }

    override fun responseBodyEnd(call: Call, byteCount: Long) {
        responseBodyLength = byteCount
        printEvent(RESPONSE_BODY_END, call.request().url.toString())
    }

    override fun responseFailed(call: Call, ioe: IOException) {
        isSuccessFull = false
        errorMessage = ioe.message
        failureReason = "Response Failed"
        printEvent(RESPONSE_FAILED, call.request().url.toString())
    }

    override fun callEnd(call: Call) {
        printEvent(CALL_END, call.request().url.toString())
    }

    override fun callFailed(call: Call, ioe: IOException) {
        isSuccessFull = false
        errorMessage = ioe.message
        printEvent(CALL_FAILED, call.request().url.toString())
    }

    /******* Helper functions ********/

    fun removeUUIDFromUrl(url: String): String {
        val uuid = url.split("/")
            .find { it.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) }
        if (uuid?.isNotEmpty() == true) {
            return url.replace(uuid, "")
        }

        return url
    }

    fun removeParams(url: String): String {
        if (URL(url).query == null) {
            return url
        }
        return url.replace(URL(url).query, "").replace("?", "")
    }

    private fun <String, Any> MutableMap<String, Any>.setValue(key: String, value: Any?) {
        value?.let {
            this[key] = value
        }
    }
}
