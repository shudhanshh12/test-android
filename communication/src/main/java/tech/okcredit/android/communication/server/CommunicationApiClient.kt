package tech.okcredit.android.communication.server

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import androidx.annotation.Keep
import io.reactivex.Completable
import org.joda.time.DateTime
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiClient {
    @POST("notification/ack")
    fun ack(
        @Body req: AckRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Completable
}

@Keep
data class AckRequest(
    val notification_id: String,
    val ack_at: Long
)

@Keep
data class AnswerRequest(
    val msg_id: String,
    val response: DateTime
)
