package `in`.okcredit.ui._utils

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.AppAnalytics.Event.DEVICE_STATUS
import `in`.okcredit.analytics.AppAnalytics.Event.DEVICE_STORAGE
import `in`.okcredit.analytics.AppAnalytics.Key.DATE_TIME_SETTING
import `in`.okcredit.analytics.AppAnalytics.Value.MANUAL
import `in`.okcredit.analytics.UserProperties
import `in`.okcredit.merchant.device.DeviceInfoUtils
import android.content.Context
import android.provider.Settings
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import tech.okcredit.android.base.crashlytics.RecordException
import javax.inject.Inject

class TrackDeviceInfo @Inject constructor(
    private val deviceInfoUtils: Lazy<DeviceInfoUtils>,
    private val analyticsProvider: Lazy<AnalyticsProvider>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val context: Lazy<Context>,
) {

    companion object {
        const val FILE_STORAGE_TRACKING_SAMPLING = "file_storage_tracking_sampling"
    }

    fun execute() {
        analyticsProvider.get()
            .setUserProperty(
                mutableMapOf(
                    UserProperties.ARE_NOTIFICATIONS_ENABLED to deviceInfoUtils.get().areNotificationsEnabled()
                )
            )

        if (deviceInfoUtils.get().isMarshMallow()) {
            analyticsProvider.get()
                .setUserProperty(
                    mutableMapOf(
                        UserProperties.IS_IGNORING_BATTERY_OPTIMIZATIONS to deviceInfoUtils.get()
                            .isIgnoringBatteryOptimizations()
                    )
                )
        }

        val isDeviceDateTimeManuallySet =
            Settings.Global.getInt(context.get().contentResolver, Settings.Global.AUTO_TIME, 0) != 1

        if (isDeviceDateTimeManuallySet) {
            analyticsProvider.get().trackEngineeringMetricEvents(DEVICE_STATUS, mapOf(DATE_TIME_SETTING to MANUAL))
        }

        try {
            val sampling = firebaseRemoteConfig.get().getLong(FILE_STORAGE_TRACKING_SAMPLING).toInt()
            if ((0..100).random() < sampling) {
                analyticsProvider.get()
                    .trackEngineeringMetricEvents(
                        DEVICE_STORAGE,
                        mapOf(
                            "Internal Cache Directory Size" to deviceInfoUtils.get()
                                .getInternalCacheDirectorySizeInMB(),
                            "Internal File Directory Size" to deviceInfoUtils.get()
                                .getInternalFileDirectorySizeInMB(),
                            "External Storage Directory Size" to (
                                deviceInfoUtils.get()
                                    .getExternalFileDirectorySizeInMB() ?: ""
                                )
                        )
                    )
            }
        } catch (e: Exception) {
            RecordException.recordException(e)
        }
    }
}
