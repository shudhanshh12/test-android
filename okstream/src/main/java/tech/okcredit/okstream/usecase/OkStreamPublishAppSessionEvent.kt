package tech.okcredit.okstream.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.squareup.moshi.Moshi
import dagger.Lazy
import merchant.android.okstream.contract.OkStreamConstant
import merchant.android.okstream.sdk.OkStreamSdk
import merchant.android.okstream.sdk.model.AppSession
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.ThreadUtils
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AppScope
class OkStreamPublishAppSessionEvent @Inject constructor(
    private val okStreamSdk: Lazy<OkStreamSdk>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val moshi: Lazy<Moshi>,
) {
    companion object {
        private const val APP_SESSION_TOPIC = "/appSession"
    }

    private val adapter = moshi.get().adapter(AppSession::class.java)

    fun execute(individualId: String, deviceId: String) {
        getActiveBusinessId.get()
            .execute()
            .subscribeOn(ThreadUtils.newThread())
            .subscribe(
                { businessId ->
                    val currentTimeStamp = DateTimeUtils.currentDateTime().millis
                    val appSessionObj =
                        AppSession(UUID.randomUUID().toString(), individualId, businessId, deviceId, currentTimeStamp)
                    var appSession: String? = null
                    try {
                        appSession = adapter.toJson(appSessionObj)
                    } catch (e: Exception) {
                        Timber.e("${OkStreamConstant.TAG} OkStreamPublishAppSessionEvent Parse Error")
                    }
                    Timber.i("${OkStreamConstant.TAG} Publishing OkStreamPublishAppSessionEvent $appSession")
                    if (appSession != null) {
                        okStreamSdk.get().okStreamPublish(APP_SESSION_TOPIC, appSession)
                    } else {
                        Timber.e("${OkStreamConstant.TAG} OkStreamPublishAppSessionEvent Null Event")
                    }
                },
                {}
            )
    }
}
