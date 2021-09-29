package tech.okcredit.okstream

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import merchant.android.okstream.contract.OkStreamService
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.okstream.usecase.OkStreamConnect
import tech.okcredit.okstream.usecase.OkStreamDisconnect
import tech.okcredit.okstream.usecase.OkStreamPublishActivityEvent
import javax.inject.Inject

@AppScope
class OkStreamServiceImpl @Inject constructor(
    private val okStreamConnect: Lazy<OkStreamConnect>,
    private val okStreamPublishActivityEvent: Lazy<OkStreamPublishActivityEvent>,
    private val okStreamDisconnect: Lazy<OkStreamDisconnect>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : OkStreamService {

    companion object {
        const val OKSTREAM_FLAG_KEY = "okstream_feature_flag_v0"
    }

    override fun connect(context: Context) {
        val okStreamFlag = firebaseRemoteConfig.get().getBoolean(OKSTREAM_FLAG_KEY)
        if (okStreamFlag) {
            okStreamConnect.get().execute(context)
        }
    }

    override fun disconnect(context: Context) {
        val okStreamFlag = firebaseRemoteConfig.get().getBoolean(OKSTREAM_FLAG_KEY)
        if (okStreamFlag) {
            okStreamDisconnect.get().execute(context)
        }
    }

    override fun publishAddCustomerTransaction(data: Any, receiver: String) {
        val okStreamFlag = firebaseRemoteConfig.get().getBoolean(OKSTREAM_FLAG_KEY)
        if (okStreamFlag) {
            getActiveBusinessId.get()
                .execute()
                .subscribeOn(ThreadUtils.database())
                .observeOn(ThreadUtils.newThread())
                .subscribe(
                    { businessId ->
                        okStreamPublishActivityEvent.get()
                            .execute(data, businessId, receiver, OkStreamPublishActivityEvent.Type.ADD_TXN_SUCCESS.type)
                    },
                    {
                        RecordException.recordException(Exception(it))
                    }
                )
        }
    }

    override fun publishAddCustomerSuccess(data: Any, receiver: String) {
        val okStreamFlag = firebaseRemoteConfig.get().getBoolean(OKSTREAM_FLAG_KEY)
        if (okStreamFlag) {
            getActiveBusinessId.get()
                .execute()
                .subscribeOn(ThreadUtils.database())
                .subscribe(
                    { businessId ->

                        okStreamPublishActivityEvent.get()
                            .execute(
                                data,
                                businessId,
                                receiver,
                                OkStreamPublishActivityEvent.Type.ADD_CUSTOMER_SUCCESS.type
                            )
                    },
                    {
                        RecordException.recordException(Exception(it))
                    }
                )
        }
    }
}
