package tech.okcredit.okstream.usecase

import `in`.okcredit.individual.contract.GetIndividual
import `in`.okcredit.merchant.device.Device
import `in`.okcredit.merchant.device.DeviceRepository
import android.content.Context
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.rxSingle
import merchant.android.okstream.contract.OkStreamConstant
import merchant.android.okstream.sdk.OkStreamSdk
import merchant.android.okstream.sdk.instrumentation.OkStreamTracker
import tech.okcredit.android.auth.AccessTokenProvider
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.utils.ThreadUtils
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AppScope
class OkStreamConnect @Inject constructor(
    private val accessTokenProvider: Lazy<AccessTokenProvider>,
    private val getIndividual: Lazy<GetIndividual>,
    private val deviceRepository: Lazy<DeviceRepository>,
    private val okStreamSdk: Lazy<OkStreamSdk>,
    private val okStreamPublishAppSessionEvent: Lazy<OkStreamPublishAppSessionEvent>,
    private val okStreamSubscribeSyncCustomerTxn: Lazy<OkStreamSubscribeSyncCustomerTxn>,
    private val okStreamTracker: Lazy<OkStreamTracker>,
) {

    fun invokeConnect(merchantId: String, deviceId: String, connectFlowId: String) {
        okStreamTracker.get().trackDebugConnect("Completed", connectFlowId)
        Timber.d("${OkStreamConstant.TAG} Connection Success. Start subscription and publish")
        // subscribe
        okStreamSubscribeSyncCustomerTxn.get().execute(merchantId)
        // publish
        okStreamPublishAppSessionEvent.get().execute(merchantId, deviceId)
    }

    fun execute(context: Context) {
        val zipper = BiFunction<String, Device, OkStreamConnectConfig> { id, device ->
            OkStreamConnectConfig(individualId = id, device.id, null)
        }

        val getAccessToken = Single.fromCallable {
            accessTokenProvider.get().getAccessToken()
        }.subscribeOn(ThreadUtils.newThread())

        Single.zip(
            rxSingle { getIndividual.get().execute().first() }.map { it.id },
            deviceRepository.get().getDevice().firstOrError(),
            zipper
        )
            .subscribeOn(ThreadUtils.worker())
            .flatMap { data ->
                getAccessToken
                    .map {
                        return@map OkStreamConnectConfig(data.individualId, data.deviceId, it)
                    }
            }
            .flatMapCompletable { data ->
                if (data.accessToken != null) {
                    val connectFlowId = UUID.randomUUID().toString()
                    okStreamTracker.get().trackDebugConnect("Started", connectFlowId)
                    Timber.d("${OkStreamConstant.TAG} emitted merchant and device data. Preparing to connect $data")
                    return@flatMapCompletable okStreamSdk.get()
                        .connect(
                            context,
                            data.deviceId,
                            data.individualId,
                            data.accessToken,
                            connectFlowId,
                            ::invokeConnect
                        )
                        .ignoreElement() // TODO: V1: Filter Ack Code 16.
                        .doOnComplete {
                            okStreamTracker.get().trackDebugConnect("Completed", connectFlowId)
                            Timber.d("${OkStreamConstant.TAG} Connection Success. Start subscription and publish")
                            // subscribe
                            okStreamSubscribeSyncCustomerTxn.get().execute(data.individualId)
                            // publish
                            okStreamPublishAppSessionEvent.get().execute(data.individualId, data.deviceId)
                        }.doOnError {
                            okStreamTracker.get().trackDebugConnect("Error", connectFlowId, it.message)
                            Timber.e("${OkStreamConstant.TAG} Connection error $it")
                        }
                } else {
                    Timber.d("${OkStreamConstant.TAG}: emitted merchant and device data. waiting for auth token")
                    return@flatMapCompletable Completable.complete()
                }
            }.subscribe({}, {})
    }

    data class OkStreamConnectConfig(val individualId: String, val deviceId: String, val accessToken: String?)
}
