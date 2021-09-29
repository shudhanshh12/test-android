package `in`.okcredit.backend.server.riskInternal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RiskDetailsRequest(
    @Json(name = "service_name")
    val serviceName: String,
    @Json(name = "client")
    val client: String
)

@JsonClass(generateAdapter = true)
data class RiskDetailsResponse(
    @Json(name = "kyc_info")
    val kycInfo: KycInfo,
    @Json(name = "risk_category")
    val riskCategory: String,
    @Json(name = "limit_info")
    val limitInfo: LimitInfo,
    @Json(name = "payment_instruments")
    val paymentInstruments: List<PaymentInstruments>,
    @Json(name = "future_limit")
    val futureLimit: FutureLimit
)

@JsonClass(generateAdapter = true)
data class KycInfo(
    @Json(name = "kyc_status")
    val kycStatus: String
)

@JsonClass(generateAdapter = true)
data class LimitInfo(
    @Json(name = "upi_limit_info")
    val upiLimitInfo: LimitInfoDetails,
    @Json(name = "non_upi_limit_info")
    val nonUpiLimitInfo: LimitInfoDetails
)

@JsonClass(generateAdapter = true)
data class FutureLimit(
    @Json(name = "total_amount_limit")
    val totalAmountLimit: Long,
    @Json(name = "total_txn_limit")
    val totalTransactionLimit: Long
)

@JsonClass(generateAdapter = true)
data class LimitInfoDetails(
    @Json(name = "daily_limit_reached")
    val dailyLimitReached: Boolean,
    @Json(name = "total_daily_amount_limit")
    val totalDailyAmountLimit: Long,
    @Json(name = "total_daily_limit_used")
    val totalDailyLimitUsed: Long,
    @Json(name = "remaining_daily_amount_limit")
    val remainingDailyAmountLimit: Long,
    @Json(name = "total_daily_transaction_limit")
    val totalDailyTransactionLimit: Long,
    @Json(name = "remaining_daily_transaction_limit")
    val remainingDailyTransactionLimit: Long
)

@JsonClass(generateAdapter = true)
data class PaymentInstruments(
    @Json(name = "instrument_name")
    val instrumentName: String,
    @Json(name = "enabled")
    val enabled: Boolean,
    @Json(name = "meta_info")
    val metaInfo: MetaInfo,
    @Json(name = "limit_info")
    val limitInfo: LimitInfoDetails
)

@JsonClass(generateAdapter = true)
class MetaInfo
