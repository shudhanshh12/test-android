package `in`.okcredit.storesms.data.server

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface StoreSmsApiClient {
    @POST("StoreSms")
    suspend fun sendRawSmsToServer(
        @Body rawSmsRequestBody: RawSmsRequestBody,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    )

    @GET("GetLastSync")
    suspend fun getLasRawSmsSyncedTime(
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): RawSmsSyncLastTimeResponse
}

data class RawSms(
    @SerializedName("raw_message")
    val rawMessage: String,
    @SerializedName("sender_address")
    val senderAddress: String,
    @SerializedName("message_received_time")
    val messageReceivedTime: Long,
    @SerializedName("creation_time")
    val creationTime: Long,
)

data class RawSmsRequestBody(
    @SerializedName("type")
    val type: String = "Message",
    @SerializedName("texts")
    val texts: List<RawSms>,
)

data class RawSmsSyncLastTimeResponse(
    @SerializedName("time")
    val time: Long,
)
