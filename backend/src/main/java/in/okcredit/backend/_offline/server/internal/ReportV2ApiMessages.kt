package `in`.okcredit.backend._offline.server.internal

import com.google.gson.annotations.SerializedName

data class GenerateReportUrlRequest(
    @SerializedName("type")
    val type: String,
    @SerializedName("lang")
    val lang: String,
    @SerializedName("account_id")
    val accountId: String?,
    @SerializedName("start_time")
    val startTime: Long?,
    @SerializedName("end_time")
    val endTime: Long?,
    @SerializedName("device_id")
    val deviceId: String
)

data class GenerateReportUrlResponse(
    @SerializedName("report_id")
    val reportId: String,
    @SerializedName("status")
    val status: String
)

data class GetReportUrlResponse(
    @SerializedName("report_id")
    val reportId: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("eta")
    val eta: String?,
    @SerializedName("eta_unit")
    val etaUnit: String?,
    @SerializedName("report_url")
    val reportUrl: String?,
    @SerializedName("error_msg")
    val errorMessage: String?
)
