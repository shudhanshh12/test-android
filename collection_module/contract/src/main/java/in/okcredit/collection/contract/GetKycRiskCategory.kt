package `in`.okcredit.collection.contract

import io.reactivex.Observable

interface GetKycRiskCategory {
    fun execute(shouldFetchWhenCollectionNotAdopted: Boolean = false): Observable<KycRisk>
}

enum class KycRiskCategory(val value: String) {
    NO_RISK("NO_RISK"),
    HIGH("HIGH"),
    LOW("LOW")
}

data class KycExternalInfo(
    val merchantId: String = "",
    val kyc: String = "",
    val upiDailyLimit: Long = 0L,
    val nonUpiDailyLimit: Long = 0L,
    val upiDailyTransactionAmount: Long = 0L,
    val nonUpiDailyTransactionAmount: Long = 0L,
    val category: String = ""
)

data class KycRisk(
    val kycRiskCategory: KycRiskCategory,
    val isLimitReached: Boolean,
    val limitType: String,
)
