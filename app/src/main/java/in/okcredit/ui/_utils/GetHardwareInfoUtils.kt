package `in`.okcredit.ui._utils

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.UserProperties
import `in`.okcredit.merchant.device.DeviceUtils
import dagger.Lazy
import javax.inject.Inject

class GetHardwareInfoUtils @Inject constructor(
    private val deviceUtils: Lazy<DeviceUtils>,
    private val analyticsProvider: Lazy<AnalyticsProvider>
) {
    fun execute() {
        val properties = mutableMapOf<String, Any>()
        properties[UserProperties.DEVICE_RAM] = deviceUtils.get().getRandomAccessMemory()
        properties[UserProperties.DEVICE_INTERNAL_STORAGE] = deviceUtils.get().getDeviceInternalStorage()
        properties[UserProperties.DEVICE_SYSTEM_STORAGE] = deviceUtils.get().getDeviceSystemStorage()
        properties[UserProperties.DEVICE_CPU_CORES] = deviceUtils.get().getCpuCores()
        properties[UserProperties.DEVICE_CPU_CLOCK_SPEED] = deviceUtils.get().getCPUClockSpeed()
        properties[UserProperties.DEVICE_NETWORK_CONNECTIVITY_TYPE] = deviceUtils.get().getNetworkConnectivityType()
        analyticsProvider.get().setUserProperty(properties)
    }
}
