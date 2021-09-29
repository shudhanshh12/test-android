package merchant.android.okstream.sdk.instrumentation

import `in`.okcredit.analytics.AnalyticsProvider
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.base.di.AppScope
import javax.inject.Inject

@AppScope
class OkStreamTracker @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
) {

    companion object {
        const val OKSTREAM_TRACKING_SAMPLING_RATE = "okstream_tracking_sampling_rate"
    }

    init {
        val sampling = firebaseRemoteConfig.get().getLong(OKSTREAM_TRACKING_SAMPLING_RATE).toInt()
        if ((0..100).random() < sampling) {
            OkStreamInstrumentationDataObject.isTrackingEnabled = true
        }
    }

    @NonNls
    fun trackDebugConnect(type: String, flowId: String, errorMessage: String? = null) {
        if (OkStreamInstrumentationDataObject.isTrackingEnabled) {
            val properties = HashMap<String, Any>().apply {
                this["Type"] = type
                this["Flow ID"] = flowId
            }
            errorMessage?.let {
                properties["Error Message"] = it
            }
            analyticsProvider.get().trackEngineeringMetricEvents("OkStream: Debug Connect", properties)
        }
    }

    @NonNls
    fun trackDebugPublish(type: String, flowId: String, topic: String, payload: String, errorMessage: String? = null) {
        if (OkStreamInstrumentationDataObject.isTrackingEnabled) {
            val properties = HashMap<String, Any>().apply {
                this["Type"] = type
                this["ID"] = flowId
                this["Topic"] = topic
                this["payload"] = payload
            }
            errorMessage?.let {
                properties["Error Message"] = it
            }
            analyticsProvider.get().trackEngineeringMetricEvents("OkStream: Debug Publish", properties)
        }
    }

    @NonNls
    fun trackDebugPublishBlocked(errorMessage: String? = null) {
        if (OkStreamInstrumentationDataObject.isTrackingEnabled) {
            val properties = HashMap<String, Any>()
            errorMessage?.let {
                properties["Error Message"] = it
            }
            analyticsProvider.get().trackEngineeringMetricEvents("OkStream: Publish Blocked", properties)
        }
    }

    @NonNls
    fun trackDebugSubscribe(
        type: String,
        flowId: String,
        topic: String,
        payload: String? = null,
        errorMessage: String? = null,
    ) {
        if (OkStreamInstrumentationDataObject.isTrackingEnabled) {
            val properties = HashMap<String, Any>().apply {
                this["Type"] = type
                this["ID"] = flowId
                this["Topic"] = topic
            }
            errorMessage?.let {
                properties["Error Message"] = it
            }
            payload?.let {
                properties["Payload"] = it
            }
            analyticsProvider.get().trackEngineeringMetricEvents("OkStream: Debug Subscribe", properties)
        }
    }

    @NonNls
    fun trackReceiveSubscribe(
        id: String,
        name: String,
        type: Int,
        version: Int,
    ) {
        if (OkStreamInstrumentationDataObject.isTrackingEnabled) {
            val properties = HashMap<String, Any>().apply {
                this["Name"] = name
                this["Type"] = type
                this["ID"] = id
                this["version"] = version
            }
            analyticsProvider.get().trackEngineeringMetricEvents("OkStream: Receive Subscribe", properties)
        }
    }

    @NonNls
    fun trackReceiveParseError(
        payload: String,
    ) {
        if (OkStreamInstrumentationDataObject.isTrackingEnabled) {
            val properties = HashMap<String, Any>().apply {
                this["Payload"] = payload
            }
            analyticsProvider.get().trackEngineeringMetricEvents("OkStream: Receive Subscribe Parse Error", properties)
        }
    }
}
