package tech.okcredit.okstream.usecase

import com.squareup.moshi.Moshi
import dagger.Lazy
import merchant.android.okstream.contract.OkStreamConstant
import merchant.android.okstream.sdk.OkStreamSdk
import merchant.android.okstream.sdk.OkStreamSdkImpl
import merchant.android.okstream.sdk.model.ActivityPublishEvent
import merchant.android.okstream.sdk.model.Device
import merchant.android.okstream.sdk.model.Meta
import tech.okcredit.android.base.utils.DateTimeUtils
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class OkStreamPublishActivityEvent @Inject constructor(
    private val okStreamSdk: Lazy<OkStreamSdk>,
    moshi: Lazy<Moshi>,
) {

    private val adapter = moshi.get().adapter(ActivityPublishEvent::class.java)

    companion object {
        private const val ACTIVITY_TOPIC = "/activity"
    }

    fun execute(data: Any, businessId: String, receiver: String?, type: String) {
        val meta = Meta(UUID.randomUUID().toString())
        val currentTimeStamp = DateTimeUtils.currentDateTime().millis
        val device = Device(OkStreamSdkImpl.Config.clientId ?: "")

        val eventObj = ActivityPublishEvent(
            UUID.randomUUID().toString(),
            OkStreamSdkImpl.Config.username ?: "",
            meta,
            businessId,
            type, currentTimeStamp, receiver, data,
            device
        )
        var event: String? = null
        try {
            event = adapter.toJson(eventObj)
        } catch (e: Exception) {
            Timber.e("${OkStreamConstant.TAG} OkStreamPublishActivityEvent Parse Error")
        }
        Timber.i("${OkStreamConstant.TAG} OkStreamPublishAppSessionEvent $event")
        if (event != null) {
            okStreamSdk.get().okStreamPublish(ACTIVITY_TOPIC, event)
        } else {
            Timber.e("${OkStreamConstant.TAG} OkStreamPublishActivityEvent Null Event")
        }
    }

    enum class Type(val type: String) {
        ADD_CUSTOMER_SUCCESS("add_customer_success"),
        ADD_TXN_SUCCESS("add_transaction_success")
    }
}
