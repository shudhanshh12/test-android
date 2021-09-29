package merchant.android.okstream.sdk

import android.content.Context
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck
import io.reactivex.Observable
import io.reactivex.Single
import kotlin.reflect.KFunction3

interface OkStreamSdk {
    fun connect(
        context: Context,
        clientId: String,
        username: String,
        password: String,
        connectFlowId: String,
        invokeConnectFunction: KFunction3<String, String, String, Unit>,
    ): Single<Mqtt5ConnAck>

    fun disconnect(context: Context)
    fun okStreamSubscribe(topic: String): Observable<String>
    fun okStreamPublish(topic: String, message: String)
}
