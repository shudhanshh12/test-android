package tech.okcredit.base.network

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import dagger.Lazy
import org.joda.time.DateTimeUtils
import org.junit.Before
import org.junit.Test
import tech.okcredit.base.network.EventListener.Companion.EventName
import tech.okcredit.base.network.TestData.URL
import tech.okcredit.base.network.utils.TrackNetworkPerformanceBinding

internal class EventListenerTest {

    lateinit var eventListener: EventListener
    private val trackNetworkPerformanceBinding: TrackNetworkPerformanceBinding = mock()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        eventListener = EventListener(Lazy { trackNetworkPerformanceBinding })
        DateTimeUtils.setCurrentMillisFixed(1000)
    }

    @Test
    fun `remove params from Url String 1`() {
        val url =
            eventListener.removeParams(
                "https://api.staging.okcredit.in/v1.0/" +
                    "customer?merchant_id=0135f371-ab17-410b-80d9-54e4b3ef0d5f&lang=en"
            )

        assertThat(url == URL).isTrue()
    }

    @Test
    fun `remove params from Url String 2`() {
        val url =
            eventListener.removeParams(
                "https://api.staging.okcredit.in/v1.0/sc/transactions?start_time=2020-06-13T15%3A35%3A54.618%2B05%3A30"
            )

        assertThat(url == "https://api.staging.okcredit.in/v1.0/sc/transactions").isTrue()
    }

    @Test
    fun `remove params from Url String 3`() {
        val url =
            eventListener.removeParams(
                "https://collection.staging.okcredit.io/v1/ListCollections?after=1612444554"
            )

        assertThat(url == "https://collection.staging.okcredit.io/v1/ListCollections").isTrue()
    }

    @Test
    fun `remove UUID From url String 1`() {
        val url =
            eventListener.removeUUIDFromUrl(
                "https://api.staging.okcredit.in/v1.0/" +
                    "customer/3b2707bf-29dc-4db2-9026-48cd8b999d5b"
            )

        assertThat(url == "https://api.staging.okcredit.in/v1.0/customer/").isTrue()
    }

    @Test
    fun `remove UUID From url String 2`() {
        val url =
            eventListener.removeUUIDFromUrl(
                "https://api.staging.okcredit.in/v1.0" +
                    "/customer/3b2707bf-29dc-4db2-9026-48cd8b999d5b/transaction"
            )

        assertThat(url == "https://api.staging.okcredit.in/v1.0/customer//transaction").isTrue()
    }

    @Test
    fun `remove UUID From url String 3`() {
        val url =
            eventListener.removeUUIDFromUrl(
                "https://api.staging.okcredit.in/v1.0/devices/91b17fac-7062-42ca-8402-78dae2946de5/link"
            )

        assertThat(url == "https://api.staging.okcredit.in/v1.0/devices//link").isTrue()
    }

    @Test
    fun `print event should record in values with the event time`() {
        DateTimeUtils.setCurrentMillisFixed(1000)
        eventListener.printEvent("callStart", URL)

        DateTimeUtils.setCurrentMillisFixed(1010)
        eventListener.printEvent("proxySelectStart", URL)

        DateTimeUtils.setCurrentMillisFixed(1020)
        eventListener.printEvent("proxySelectEnd", URL)

        DateTimeUtils.setCurrentMillisFixed(1030)
        eventListener.printEvent("dnsStart", URL)

        DateTimeUtils.setCurrentMillisFixed(1040)
        eventListener.printEvent("dnsEnd", URL)

        DateTimeUtils.setCurrentMillisFixed(1050)
        eventListener.printEvent("connectStart", URL)

        DateTimeUtils.setCurrentMillisFixed(1060)
        eventListener.printEvent("connectStart", URL)

        DateTimeUtils.setCurrentMillisFixed(1070)
        eventListener.printEvent("callEnd", URL)

        assertThat(
            eventListener.listValues == listOf(
                ("callStart" to 1000),
                ("proxySelectStart" to 1010),
                ("proxySelectEnd" to 1020),
                ("dnsStart" to 1030),
                ("dnsEnd" to 1040),
                ("connectStart" to 1050),
                ("connectStart" to 1060),
                ("callEnd" to 1070)
            )
        )
    }

    @Test
    fun `callstart event should clear events from the list`() {
        DateTimeUtils.setCurrentMillisFixed(3438)
        eventListener.printEvent("proxySelectStart", URL)

        DateTimeUtils.setCurrentMillisFixed(1000)
        eventListener.printEvent("callStart", URL)

        assertThat(
            eventListener.listValues == listOf(
                mapOf(URL to ("callStart" to 1000))
            )
        )
    }

    @Test
    fun `findTimeDifferenceBetweenEvents() should return null if startEvent never logged`() {
        DateTimeUtils.setCurrentMillisFixed(3438)
        eventListener.printEvent("connectStart", URL)
        eventListener.printEvent("proxySelectStart", URL)
        eventListener.printEvent("proxySelectEnd", URL)
        eventListener.printEvent("callEnd", URL)

        DateTimeUtils.setCurrentMillisFixed(1000)
        val duration = eventListener.findTimeDifferenceBetweenEvents("callStart", "callEnd")

        assertThat(
            duration == null
        ).isTrue()
    }

    @Test
    fun `findTimeDifferenceBetweenEvents() should return null if endEvent never logged`() {
        DateTimeUtils.setCurrentMillisFixed(3438)
        eventListener.printEvent("callStart", URL)
        eventListener.printEvent("connectStart", URL)
        eventListener.printEvent("proxySelectStart", URL)
        eventListener.printEvent("proxySelectEnd", URL)

        DateTimeUtils.setCurrentMillisFixed(1000)
        val duration = eventListener.findTimeDifferenceBetweenEvents("callStart", "callEnd")

        assertThat(
            duration == null
        ).isTrue()
    }

    @Test
    fun `findTimeDifferenceBetweenEvents() should return time between events if it both the events are logged `() {
        DateTimeUtils.setCurrentMillisFixed(1000)
        eventListener.printEvent("callStart", URL)
        DateTimeUtils.setCurrentMillisFixed(1020)
        eventListener.printEvent("connectStart", URL)
        DateTimeUtils.setCurrentMillisFixed(1040)
        eventListener.printEvent("proxySelectStart", URL)
        DateTimeUtils.setCurrentMillisFixed(1060)
        eventListener.printEvent("proxySelectEnd", URL)
        DateTimeUtils.setCurrentMillisFixed(1080)
        eventListener.printEvent("callEnd", URL)

        val duration = eventListener.findTimeDifferenceBetweenEvents("callStart", "callEnd")

        assertThat(
            duration == 80L
        ).isTrue()
    }

    @Test
    fun `findTimeDifferenceBetweenEvents() should return time between first events if endEvents are logged multiple times`() {
        DateTimeUtils.setCurrentMillisFixed(1000)
        eventListener.printEvent("callStart", URL)
        DateTimeUtils.setCurrentMillisFixed(1020)
        eventListener.printEvent("connectStart", URL)
        DateTimeUtils.setCurrentMillisFixed(1040)
        eventListener.printEvent("proxySelectStart", URL)
        DateTimeUtils.setCurrentMillisFixed(1060)
        eventListener.printEvent("proxySelectEnd", URL)
        DateTimeUtils.setCurrentMillisFixed(1080)
        eventListener.printEvent("callEnd", URL)
        DateTimeUtils.setCurrentMillisFixed(1100)
        eventListener.printEvent("proxySelectStart", URL)
        DateTimeUtils.setCurrentMillisFixed(1500)
        eventListener.printEvent("callEnd", URL)

        val duration = eventListener.findTimeDifferenceBetweenEvents("callStart", "callEnd")

        assertThat(
            duration == 80L
        ).isTrue()
    }

    @Test
    fun `findTimeDifferenceBetweenEvents() should return time between first events if startEvents are logged multiple times`() {
        DateTimeUtils.setCurrentMillisFixed(1000)
        eventListener.printEvent("callStart", URL)
        DateTimeUtils.setCurrentMillisFixed(1020)
        eventListener.printEvent("connectStart", URL)
        DateTimeUtils.setCurrentMillisFixed(1040)
        eventListener.printEvent("callStart", URL)
        DateTimeUtils.setCurrentMillisFixed(1060)
        eventListener.printEvent("proxySelectEnd", URL)
        DateTimeUtils.setCurrentMillisFixed(1080)
        eventListener.printEvent("callEnd", URL)
        DateTimeUtils.setCurrentMillisFixed(1100)
        eventListener.printEvent("proxySelectStart", URL)
        DateTimeUtils.setCurrentMillisFixed(1500)
        eventListener.printEvent("callEnd", URL)

        val duration = eventListener.findTimeDifferenceBetweenEvents("callStart", "callEnd")

        assertThat(
            duration == 80L
        ).isTrue()
    }

    @Test
    fun `printEvent should track connection failure on connectFailed`() {
        DateTimeUtils.setCurrentMillisFixed(1000)
        eventListener.printEvent("callStart", URL)

        DateTimeUtils.setCurrentMillisFixed(1500)
        eventListener.printEvent("connectFailed", URL)

        verify(
            trackNetworkPerformanceBinding
        ).trackNetworkPerformance(
            EventName,
            mapOf(
                "End Point" to URL,
                "Filtered URL" to URL,
                "Duration" to 500L
            )
        )
    }

    @Test
    fun `printEvent should track connection failure on requestFailed`() {
        DateTimeUtils.setCurrentMillisFixed(1000)
        eventListener.printEvent("callStart", URL)

        DateTimeUtils.setCurrentMillisFixed(1500)
        eventListener.printEvent("requestFailed", URL)

        verify(
            trackNetworkPerformanceBinding
        ).trackNetworkPerformance(
            EventName,
            mapOf(
                "End Point" to URL,
                "Filtered URL" to URL,
                "Duration" to 500L
            )
        )
    }

    @Test
    fun `printEvent should track connection failure on responseFailed`() {
        DateTimeUtils.setCurrentMillisFixed(1000)
        eventListener.printEvent("callStart", URL)

        DateTimeUtils.setCurrentMillisFixed(1500)
        eventListener.printEvent("responseFailed", URL)

        verify(
            trackNetworkPerformanceBinding
        ).trackNetworkPerformance(
            EventName,
            mapOf(
                "End Point" to URL,
                "Filtered URL" to URL,
                "Duration" to 500L
            )
        )
    }

    @Test
    fun `printEvent should track connection failure on callFailed`() {
        DateTimeUtils.setCurrentMillisFixed(1000)
        eventListener.printEvent("callStart", URL)

        DateTimeUtils.setCurrentMillisFixed(1500)
        eventListener.printEvent("callFailed", URL)

        verify(
            trackNetworkPerformanceBinding
        ).trackNetworkPerformance(
            EventName,
            mapOf(
                "End Point" to URL,
                "Filtered URL" to URL,
                "Duration" to 500L
            )
        )
    }

    @Test
    fun `printEvent should track events call end to end metrics`() {
        DateTimeUtils.setCurrentMillisFixed(1000)
        eventListener.printEvent("callStart", URL)

        DateTimeUtils.setCurrentMillisFixed(1500)
        eventListener.printEvent("callEnd", URL)

        verify(
            trackNetworkPerformanceBinding
        ).trackNetworkPerformance(
            EventName,
            mapOf(
                "End Point" to URL,
                "Filtered URL" to URL,
                "Duration" to 500L
            )
        )
    }

    @Test
    fun `printEvent should track right dns lookup time`() {
        DateTimeUtils.setCurrentMillisFixed(1000)
        eventListener.printEvent("callStart", URL)

        DateTimeUtils.setCurrentMillisFixed(1100)
        eventListener.printEvent("dnsStart", URL)

        DateTimeUtils.setCurrentMillisFixed(1200)
        eventListener.printEvent("dnsEnd", URL)

        DateTimeUtils.setCurrentMillisFixed(1500)
        eventListener.printEvent("callEnd", URL)

        verify(
            trackNetworkPerformanceBinding
        ).trackNetworkPerformance(
            EventName,
            mapOf(
                "End Point" to URL,
                "Filtered URL" to URL,
                "Duration" to 500L,
                "DNS LookUp Duration" to 100L
            )
        )
    }

    @Test
    fun `printEvent should not track any other events than completed or errors`() {
        DateTimeUtils.setCurrentMillisFixed(1000)
        eventListener.printEvent("callStart", URL)

        DateTimeUtils.setCurrentMillisFixed(1100)
        eventListener.printEvent("dnsStart", URL)

        DateTimeUtils.setCurrentMillisFixed(1200)
        eventListener.printEvent("dnsEnd", URL)

        verifyZeroInteractions(
            trackNetworkPerformanceBinding
        )
    }
}
