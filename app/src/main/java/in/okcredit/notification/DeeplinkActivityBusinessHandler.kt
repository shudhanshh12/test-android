package `in`.okcredit.notification

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.contract.GetBusiness
import `in`.okcredit.merchant.ui.switch_business.SwitchBusinessAnalytics
import `in`.okcredit.merchant.usecase.SwitchBusiness
import android.os.Bundle
import com.google.gson.Gson
import dagger.Lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.awaitFirst
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.communication.NotificationData
import javax.inject.Inject

class DeeplinkActivityBusinessHandler @Inject constructor(
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val switchBusiness: Lazy<SwitchBusiness>,
    private val switchBusinessAnalytics: Lazy<SwitchBusinessAnalytics>,
    private val getBusiness: Lazy<GetBusiness>,
) {

    fun checkActiveBusinessAndDispatchDeeplink(activity: DeepLinkActivity) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                checkActiveBusinessAndUpdateIfRequired(activity.intent.extras)
            }
            activity.dispatchDeeplink()
        }
    }

    private suspend fun checkActiveBusinessAndUpdateIfRequired(extras: Bundle?) {
        try {
            val data = extras?.get("data") as? String ?: return

            val notificationData = Gson().fromJson(data, NotificationData::class.java)
            val businessIdInDeeplink = notificationData.businessId

            if (businessIdInDeeplink.isNullOrBlank()) return

            val activeBusinessId = getActiveBusinessId.get().execute().await()
            if (businessIdInDeeplink != activeBusinessId) {
                val business = getBusiness.get().execute(businessIdInDeeplink).awaitFirst()
                switchBusiness.get().execute(businessIdInDeeplink, business.name)
                switchBusinessAnalytics.get().trackAutoSwitchedBusiness("deeplink", businessIdInDeeplink)
            }
        } catch (exception: Exception) {
            RecordException.recordException(exception)
        }
    }
}
