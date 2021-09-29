package merchant.android.okstream.sdk

import android.content.Context
import android.os.Build
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.hivemq.client.mqtt.MqttClientSslConfig
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck
import com.hivemq.client.rx.FlowableWithSingle
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import merchant.android.okstream.contract.OkStreamConnectionExistError
import merchant.android.okstream.contract.OkStreamConstant
import merchant.android.okstream.contract.OkStreamNotConnectedError
import merchant.android.okstream.sdk.database.OkStreamDataBaseDao
import merchant.android.okstream.sdk.database.PublishMessage
import merchant.android.okstream.sdk.instrumentation.OkStreamTracker
import tech.okcredit.android.auth.AccessTokenProvider
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.utils.ThreadUtils
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.reflect.KFunction3

@AppScope
open class OkStreamSdkImpl @Inject constructor(
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val okStreamDataBaseDao: Lazy<OkStreamDataBaseDao>,
    private val okStreamTracker: Lazy<OkStreamTracker>,
    private val accessTokenProvider: Lazy<AccessTokenProvider>,
) : OkStreamSdk {

    companion object {
        const val KEEP_ALIVE_TIME_KEY = "okstream_keepalive"
        const val RECONNECT_PERIOD_KEY = "okstream_reconnect_period"
        const val MAX_RECONNECT_DELAY = "okstream_max_reconnect_delay"
        const val CLEAN_SESSION_KEY = "okstream_clean_session"
        const val SESSION_EXPIRY_INTERVAL_KEY = "okstream_session_expiry_interval"
    }

    private var mClient: Mqtt5RxClient? = null

    // TODO: Should remove for V1
    object Config {
        var clientId: String? = null
        var username: String? = null
        var authRefreshCount: Int = 0
    }

    private var tasks: CompositeDisposable = CompositeDisposable()
    private var sendQueuedMessageJob: Disposable? = null

    override fun connect(
        context: Context,
        clientId: String,
        username: String,
        password: String,
        connectFlowId: String,
        invokeConnectFunction: KFunction3<String, String, String, Unit>,
    ): Single<Mqtt5ConnAck> {
        Timber.d("${OkStreamConstant.TAG} Connecting to ${BuildConfig.BROKER_URL} clientId=$clientId username=$username password=$password")
        Config.username = username
        Config.clientId = clientId
        var connectAck: Single<Mqtt5ConnAck>? = null

        if (mClient == null) {
            val expectedCipherSuites = listOf(
                "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
                "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
                "TLS_RSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_RSA_WITH_AES_128_CBC_SHA",
                "TLS_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_RSA_WITH_AES_256_CBC_SHA",
                "TLS_RSA_WITH_AES_256_GCM_SHA384",
            )

            val expectedProtocols =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    listOf("TLSv1.2")
                } else {
                    listOf("TLSv1.3", "TLSv1.2")
                }

            val sslConfig = MqttClientSslConfig.builder()
                .cipherSuites(expectedCipherSuites)
                .protocols(expectedProtocols)
                .build()

            Timber.d("${OkStreamConstant.TAG} onConnect: Creating new MQTT connection")
            mClient = Mqtt5Client.builder()
                .identifier(clientId)
                .sslConfig(sslConfig)
                .simpleAuth()
                .username(username)
                .password(password.toByteArray())
                .applySimpleAuth()
                .addConnectedListener {
                    Config.authRefreshCount = 0
                    Timber.d("${OkStreamConstant.TAG} Connected ${it.clientConfig}")
                }
                .addDisconnectedListener {
                    if (it.cause is Mqtt5ConnAckException) {
                        val errorMessage = (it.cause as Mqtt5ConnAckException).mqttMessage
                        if (errorMessage.reasonCode == Mqtt5ConnAckReasonCode.NOT_AUTHORIZED) {
                            if (Config.authRefreshCount < 3) {
                                Timber.d("${OkStreamConstant.TAG} onConnect: ReConnet Started")
                                it.reconnector.reconnect(false)
                                addTask(
                                    Single.fromCallable { accessTokenProvider.get().getAccessToken(true) }
                                        .flatMapCompletable { token ->
                                            Timber.d("${OkStreamConstant.TAG} onConnect: ReConnet Got New Token")
                                            mClient = null
                                            connect(
                                                context,
                                                clientId,
                                                username,
                                                token,
                                                connectFlowId,
                                                invokeConnectFunction
                                            )
                                                .ignoreElement()
                                        }
                                        .doOnError {
                                            Timber.d("${OkStreamConstant.TAG} onConnect: ReConnet Error ${it.message}")
                                        }
                                        .doOnComplete {
                                            Timber.d("${OkStreamConstant.TAG} onConnect: ReConnet Success")
                                        }
                                        .subscribeOn(ThreadUtils.newThread())
                                        .subscribe(
                                            {
                                                invokeConnectFunction.invoke(username, clientId, connectFlowId)
                                                okStreamTracker.get()
                                                    .trackDebugConnect("ReConnect", connectFlowId)
                                            },
                                            {
                                                okStreamTracker.get()
                                                    .trackDebugConnect(
                                                        "Disconnect",
                                                        connectFlowId,
                                                        "Not Authorized Reconnect: ${it.message}"
                                                    )
                                            }
                                        )
                                )
                            } else {
                                okStreamTracker.get()
                                    .trackDebugConnect("Disconnect", connectFlowId, "Not Authorized Threshold Error")
                            }
                            Config.authRefreshCount++
                        }
                    }
                    //  }
                    okStreamTracker.get().trackDebugConnect("Disconnect", connectFlowId, it.cause.message)
                    Timber.d("${OkStreamConstant.TAG} Disconnect ${it.cause} hashCode=${okStreamTracker.get()}")
                }
                .serverHost(BuildConfig.BROKER_URL)
                .serverPort(BuildConfig.BROKER_PORT)
                .automaticReconnect()
                .initialDelay(firebaseRemoteConfig.get().getLong(RECONNECT_PERIOD_KEY), TimeUnit.SECONDS)
                .maxDelay(firebaseRemoteConfig.get().getLong(MAX_RECONNECT_DELAY), TimeUnit.SECONDS)
                .applyAutomaticReconnect()
                .buildRx()

            val connectMessage = Mqtt5Connect.builder()
                .sessionExpiryInterval(firebaseRemoteConfig.get().getLong(SESSION_EXPIRY_INTERVAL_KEY))
                .cleanStart(firebaseRemoteConfig.get().getBoolean(CLEAN_SESSION_KEY))
                .keepAlive(firebaseRemoteConfig.get().getLong(KEEP_ALIVE_TIME_KEY).toInt()).simpleAuth()
                .username(username)
                .password(password.toByteArray()).applySimpleAuth().build()

            connectAck = mClient!!.connect(connectMessage)
        } else {
            connectAck = reconnect()
        }

        return connectAck?.let {
            it
        } ?: run {
            Single.error(OkStreamNotConnectedError())
        }
    }

    private fun reconnect(): Single<Mqtt5ConnAck>? {
        if (mClient == null) return Single.error(OkStreamNotConnectedError())

        return mClient?.let {
            return if (it.state.isConnected) {
                Timber.d("${OkStreamConstant.TAG} Reconnect, Client already connected, nothing to do")
                return Single.error(OkStreamConnectionExistError())
            } else {
                Timber.d("${OkStreamConstant.TAG} Reconnect, Reconnecting MQTT")
                it.connect()
            }
        }
    }

    private fun checkConnectionStatus(): Boolean {
        return mClient != null && mClient?.state?.isConnected ?: false
    }

    private fun addTask(disposable: Disposable) {
        tasks.add(disposable)
    }

    override fun disconnect(context: Context) {
        if (mClient == null) {
            tasks.clear()
            Timber.d("${OkStreamConstant.TAG} OnDisconnect, No client connected, nothing to disconnect!")
            return
        }
        addTask(
            mClient!!.disconnect().subscribe(
                {
                    sendQueuedMessageJob?.dispose()
                    mClient = null
                    Timber.d("${OkStreamConstant.TAG} OnDisconnect,  disconnected successfully!")
                    tasks.clear()
                },
                {
                    Timber.e("${OkStreamConstant.TAG} OnDisconnect, Error ${it.message}")
                }
            )
        )
    }

    override fun okStreamSubscribe(topic: String): Observable<String> {
        Timber.d("${OkStreamConstant.TAG} Subscribe Subscribing to topic $topic")
        val flowID = UUID.randomUUID().toString()

        okStreamTracker.get()
            .trackDebugSubscribe("Started", flowID, topic)

        if (!checkConnectionStatus()) {
            okStreamTracker.get()
                .trackDebugSubscribe("Error", flowID, topic, null, "OkStream: NotConnectedError")
            Timber.d("${OkStreamConstant.TAG} Can't subscribe to topic, client not connected!")
            return Observable.error(OkStreamNotConnectedError())
        }
        val subAckAndMatchingPublishes: FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck> =
            mClient!!.subscribePublishesWith()
                .topicFilter(topic).qos(MqttQos.AT_LEAST_ONCE)
                .applySubscribe()

        return subAckAndMatchingPublishes
            .subscribeOn(ThreadUtils.api())
            .doOnNext {
                okStreamTracker.get()
                    .trackDebugSubscribe("Completed", flowID, topic, String(it.payloadAsBytes))
                Timber.d("${OkStreamConstant.TAG} Subscribe Emitted from topic ${it.payload}")
            }.doOnError {
                okStreamTracker.get()
                    .trackDebugSubscribe("Error", flowID, topic, null, it.message)
                Timber.d("${OkStreamConstant.TAG} Subscribe Emitted error ${it.message}")
            }
            .toObservable().flatMap {
                return@flatMap Observable.just(String(it.payloadAsBytes))
            }
    }

    private fun publish(topic: String, message: String): Completable {
        Timber.d("${OkStreamConstant.TAG} Publish, Publishing Topic. topic: $topic  with msg:  $message\"")

        if (!checkConnectionStatus() && mClient == null) {
            Timber.d("${OkStreamConstant.TAG} Can't Publish to topic, client not connected!")
            return Completable.error(OkStreamNotConnectedError())
        }

        val publishBuilder = Mqtt5Publish.builder()
            .topic(topic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .payload(message.toByteArray())
            .noMessageExpiry()
            .retain(firebaseRemoteConfig.get().getBoolean(CLEAN_SESSION_KEY))
            .contentType("text/plain")
            .build()

        val ack = mClient!!.publish(Flowable.just(publishBuilder))

        return ack.subscribeOn(ThreadUtils.api())
            .toObservable()
            .doOnNext {
                Timber.d("${OkStreamConstant.TAG} Published message $it")
            }.doOnError {
                Timber.d("${OkStreamConstant.TAG} Publish Emitted error ${it.message}")
            }.firstOrError()
            .ignoreElement()
    }

    private fun sendQueuedMessages() {
        if (!checkConnectionStatus()) {
            okStreamTracker.get().trackDebugPublishBlocked("OkStream Not Connected")
            Timber.d("${OkStreamConstant.TAG} Can't Send Messages Now. OkStream Not Connected")
            return
        }
        if (sendQueuedMessageJob != null && sendQueuedMessageJob?.isDisposed?.not() == true) {
            okStreamTracker.get().trackDebugPublishBlocked("OkStream Publish Job already going On")
            Timber.d("${OkStreamConstant.TAG} One QueuedMessages Job already going On")
            return
        }
        // TODO: V1. Don't expose Dao. Create a repo layer.
        sendQueuedMessageJob = okStreamDataBaseDao.get().getMessages()
            .firstOrError()
            .toObservable()
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.api())
            .flatMapIterable { it }
            .flatMapCompletable {
                okStreamTracker.get()
                    .trackDebugPublish("Sending", it.id, it.topic, it.message)
                return@flatMapCompletable publish(it.topic, it.message)
                    .doOnComplete {
                        okStreamTracker.get()
                            .trackDebugPublish("Acknowledged", it.id, it.topic, it.message)
                        Timber.d("${OkStreamConstant.TAG} Publish Completed. topic=${it.topic} message=${it.message}")
                    }
                    .doOnError {
                        Timber.e("${OkStreamConstant.TAG} Publish Error. message=${it.message}")
                    }
                    .andThen(okStreamDataBaseDao.get().deleteMessage(it.id))
                    .doOnComplete {
                        Timber.d("${OkStreamConstant.TAG} Publish DeleteMessage. topic=${it.topic} message=${it.message}")
                    }
                    .doOnError {
                        Timber.e("${OkStreamConstant.TAG} Publish DeleteMessage. message=${it.message}")
                    }
            }.subscribe(
                {
                    Timber.d("${OkStreamConstant.TAG} Sending Queued Messages Success")
                },
                {
                    Timber.d("${OkStreamConstant.TAG} Sending Queued Messages Error ${it.message}")
                }
            )
    }

    override fun okStreamPublish(topic: String, message: String) {
        val publishMessage = PublishMessage(
            id = UUID.randomUUID().toString(),
            topic = topic,
            message = message
        )

        okStreamTracker.get()
            .trackDebugPublish("Started", publishMessage.id, publishMessage.topic, publishMessage.message)

        addTask(
            okStreamDataBaseDao.get().insertMessage(publishMessage)
                .subscribeOn(ThreadUtils.database())
                .doOnComplete {
                    okStreamTracker.get()
                        .trackDebugPublish("Completed", publishMessage.id, publishMessage.topic, publishMessage.message)
                    Timber.d("${OkStreamConstant.TAG} Publish Payload Saved Into Queue topic=$topic message=$message")
                }
                .doOnError {
                    okStreamTracker.get()
                        .trackDebugPublish(
                            "Error",
                            publishMessage.id,
                            publishMessage.topic,
                            publishMessage.message,
                            it.message
                        )
                    Timber.e("${OkStreamConstant.TAG} Error Saving Data Into DB message=${it.message}")
                }
                .doOnComplete {
                    sendQueuedMessages()
                }
                .subscribe(
                    {
                        Timber.d("${OkStreamConstant.TAG} PublishV2 Completed Topic=$topic message=$message")
                    },
                    {
                        Timber.e("${OkStreamConstant.TAG} Error PublishV2 error=${it.message}")
                    }
                )
        )
    }
}
