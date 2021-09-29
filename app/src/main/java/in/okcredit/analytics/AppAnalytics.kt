package `in`.okcredit.analytics

import `in`.okcredit.analytics.AppAnalytics.Event.APPSFLYER_ACQUISITION_DATA
import `in`.okcredit.analytics.AppAnalytics.Event.GOOGLE_ACQUISITION_DATA
import `in`.okcredit.analytics.AppAnalytics.Event.LEAK_CANARY_DATA
import `in`.okcredit.analytics.AppAnalytics.Event.LEAK_CANARY_FAILED
import `in`.okcredit.analytics.AppAnalytics.Event.OK_CREDIT_APP_UPDATE
import `in`.okcredit.analytics.AppAnalytics.Key.APPS_FLYER_CALLBACK_TIME
import `in`.okcredit.analytics.AppAnalytics.Key.GOOGLE_CALLBACK_TIME
import `in`.okcredit.analytics.AppAnalytics.Key.LEAKED_CLASS
import `in`.okcredit.analytics.AppAnalytics.Key.LEAKED_OBJ
import `in`.okcredit.analytics.AppAnalytics.Key.RETAINED_HEAP_SIZE
import `in`.okcredit.analytics.AppAnalytics.Key.RETAINED_OBJ_COUNT
import dagger.Lazy
import javax.inject.Inject

class AppAnalytics @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>
) {

    object Event {
        const val APPSFLYER_ACQUISITION_DATA = "Appsflyer Acquisition Data"
        const val GOOGLE_ACQUISITION_DATA = "Google Acquisition Data"
        const val LEAK_CANARY_DATA = "Leak Canary Data"
        const val LEAK_CANARY_FAILED = "Leak Canary Failed"
        const val DEVICE_STATUS = "Device Status"
        const val DEVICE_STORAGE = "Device Storage Data"
        const val OK_CREDIT_APP_UPDATE = "OkCredit App Update"
    }

    object Key {
        const val APPS_FLYER_CALLBACK_TIME = "Appsflyer Callback Time"
        const val GOOGLE_CALLBACK_TIME = "Google Callback Time"
        const val LEAKED_CLASS = "Leaked Class"
        const val LEAKED_OBJ = "Leaked Obj"
        const val RETAINED_HEAP_SIZE = "Retained Heap Byte Size"
        const val RETAINED_OBJ_COUNT = "Retained Obj Count"
        const val GENERATED_BY = "generated_by"
        const val SESSION_ID = "session_id"
        const val DATE_TIME_SETTING = "date_time_setting"
    }

    object Value {
        const val ANDROID_APP = "android_app"
        const val MANUAL = "manual"
    }

    fun trackAppsFlyerAcquisition(callbackTime: Double, properties: MutableMap<String, String>?) {
        if (properties.isNullOrEmpty()) return
        properties[APPS_FLYER_CALLBACK_TIME] = "$callbackTime seconds"
        analyticsProvider.get().trackEvents(APPSFLYER_ACQUISITION_DATA, properties)
    }

    fun trackInstallRefererAcquisition(callbackTime: Double, properties: MutableMap<String, String>?) {
        if (properties.isNullOrEmpty()) return
        properties[GOOGLE_CALLBACK_TIME] = "$callbackTime seconds"
        analyticsProvider.get().trackEvents(GOOGLE_ACQUISITION_DATA, properties)
    }

    fun trackLeakCanaryData(leakedClass: String, leakedObj: String, leakedSize: String, leakedObjCount: String) {
        val properties = mapOf(
            LEAKED_CLASS to leakedClass,
            LEAKED_OBJ to leakedObj,
            RETAINED_HEAP_SIZE to leakedSize,
            RETAINED_OBJ_COUNT to leakedObjCount
        )
        analyticsProvider.get().trackEvents(LEAK_CANARY_DATA, properties)
    }

    fun trackLeakCanaryAnalysisFailed() {
        analyticsProvider.get().trackEvents(LEAK_CANARY_FAILED, null)
    }

    fun trackAppUpdate() {
        analyticsProvider.get().trackEvents(OK_CREDIT_APP_UPDATE, null)
    }
}
