package tech.okcredit.okstream.usecase

import com.squareup.moshi.Moshi
import dagger.Lazy
import io.reactivex.Completable
import merchant.android.okstream.contract.OkStreamConstant
import merchant.android.okstream.contract.model.OkStreamNotification
import merchant.android.okstream.sdk.OkStreamSdk
import merchant.android.okstream.sdk.instrumentation.OkStreamTracker
import tech.okcredit.android.base.di.AppScope
import timber.log.Timber
import javax.inject.Inject

@AppScope
class OkStreamSubscribeSyncCustomerTxn @Inject constructor(
    private val okStreamSdk: Lazy<OkStreamSdk>,
    private val moshi: Lazy<Moshi>,
    private val okStreamTracker: Lazy<OkStreamTracker>,
) {
    companion object {
        const val SUBSCRIBE_USER_TOPIC = "/user/"
    }

    private val adapter = moshi.get().adapter(OkStreamNotification::class.java)

    fun execute(merchantId: String) {
        // TODO: Test, Subscription always open in the App.
        okStreamSdk.get().okStreamSubscribe("$SUBSCRIBE_USER_TOPIC$merchantId")
            .map {
                Timber.d("${OkStreamConstant.TAG} SubscribeSyncCustomerTxn New Emission Value=$it")
                var okStreamNotification: OkStreamNotification? = null
                try {
                    okStreamNotification = adapter.fromJson(it)
                } catch (e: Exception) {
                    okStreamTracker.get().trackReceiveParseError(it)
                    Timber.e("${OkStreamConstant.TAG} OkStreamNotification Parsing Error")
                }
                if (okStreamNotification != null) {
                    okStreamTracker.get()
                        .trackReceiveSubscribe(
                            okStreamNotification.id,
                            okStreamNotification.name,
                            okStreamNotification.type,
                            okStreamNotification.version
                        )
                    return@map Completable.complete()
                } else {
                    return@map Completable.complete()
                }
            }.subscribe(
                {
                    Timber.d("${OkStreamConstant.TAG} SubscribeSyncCustomerTxn New Emission Process Completed")
                },
                {
                    Timber.e("${OkStreamConstant.TAG} SubscribeSyncCustomerTxn Error ${it.message}")
                }
            )
    }
}
