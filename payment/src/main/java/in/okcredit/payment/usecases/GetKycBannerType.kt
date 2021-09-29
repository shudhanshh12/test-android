package `in`.okcredit.payment.usecases

import `in`.okcredit.analytics.PropertyValue.KYC_BANNER_TYPE_INFO
import `in`.okcredit.analytics.PropertyValue.KYC_BANNER_TYPE_LIMIT_REACHED
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.collection.contract.KycStatus
import dagger.Reusable
import tech.okcredit.android.base.crashlytics.RecordException
import java.lang.IllegalStateException
import javax.inject.Inject

@Reusable
class GetKycBannerType @Inject constructor() {

    fun execute(
        isKycBannerEnabled: Boolean,
        kycStatus: KycStatus,
        kycRiskCategory: KycRiskCategory,
        shouldShowKycBannerOnPaymentEditAmountPage: Boolean,
        currentAmountSelected: Long,
        maxDailyLimit: Long,
        remainingDailyLimit: Long,
        futureAmountLimit: Long,
    ): KycBannerType {
        val doesEnteredAmountExceedLimit = maxDailyLimit != -1L && remainingDailyLimit < currentAmountSelected

        return if (doesEnteredAmountExceedLimit) {
            getBannerTypeForLimitReached(isKycBannerEnabled, kycStatus, maxDailyLimit, remainingDailyLimit)
        } else {
            if (isKycBannerEnabled) {
                getBannerTypeForLimitNotReached(
                    kycStatus,
                    kycRiskCategory,
                    shouldShowKycBannerOnPaymentEditAmountPage,
                    futureAmountLimit
                )
            } else {
                KycBannerType.None
            }
        }
    }

    private fun getBannerTypeForLimitReached(
        isKycBannerEnabled: Boolean,
        kycStatus: KycStatus,
        maxDailyLimit: Long,
        remainingDailyLimit: Long,
    ): KycBannerType {

        if (!isKycBannerEnabled) {
            return KycBannerType.LimitReachedWithoutKycEntryPoint(maxDailyLimit, remainingDailyLimit)
        }

        return when (kycStatus) {
            KycStatus.NOT_SET, KycStatus.FAILED -> KycBannerType.LimitReachedWithKycEntryPoint(
                maxDailyLimit, remainingDailyLimit
            )
            KycStatus.PENDING, KycStatus.COMPLETE -> KycBannerType.LimitReachedWithoutKycEntryPoint(
                maxDailyLimit, remainingDailyLimit
            )
        }
    }

    private fun getBannerTypeForLimitNotReached(
        kycStatus: KycStatus,
        kycRiskCategory: KycRiskCategory,
        shouldShowKycBannerOnPaymentEditAmountPage: Boolean,
        futureAmountLimit: Long,
    ): KycBannerType {

        if (!shouldShowKycBannerOnPaymentEditAmountPage) {
            return KycBannerType.None
        }

        return when (kycStatus) {
            KycStatus.NOT_SET, KycStatus.FAILED ->
                KycBannerType.InformationWithKycEntryPoint(
                    futureAmountLimit, kycRiskCategory
                )
            KycStatus.PENDING, KycStatus.COMPLETE -> KycBannerType.None
        }
    }
}

sealed class KycBannerType {
    data class InformationWithKycEntryPoint(
        val futureAmountLimit: Long,
        val kycRiskCategory: KycRiskCategory
    ) : KycBannerType()

    data class LimitReachedWithKycEntryPoint(
        val maxDailyLimit: Long,
        val remainingDailyLimit: Long
    ) : KycBannerType()

    data class LimitReachedWithoutKycEntryPoint(
        val maxDailyLimit: Long,
        val remainingDailyLimit: Long
    ) : KycBannerType()

    object None : KycBannerType()
}

fun KycBannerType.hasKycEntryPoint(): Boolean {
    return when (this) {
        is KycBannerType.InformationWithKycEntryPoint, is KycBannerType.LimitReachedWithKycEntryPoint -> true
        is KycBannerType.LimitReachedWithoutKycEntryPoint, is KycBannerType.None -> false
    }
}

fun KycBannerType.getTypeForAnalytics(): String {
    return when (this) {
        is KycBannerType.InformationWithKycEntryPoint -> KYC_BANNER_TYPE_INFO
        is KycBannerType.LimitReachedWithKycEntryPoint -> KYC_BANNER_TYPE_LIMIT_REACHED
        else -> {
            RecordException.recordException(
                IllegalStateException("Must not be called for KycBannerType(s) without kyc entry point")
            )
            ""
        }
    }
}
