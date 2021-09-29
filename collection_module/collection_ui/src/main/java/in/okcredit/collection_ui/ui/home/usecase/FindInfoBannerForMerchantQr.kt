package `in`.okcredit.collection_ui.ui.home.usecase

import `in`.okcredit.collection.contract.*
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsContract
import `in`.okcredit.collection_ui.usecase.GetUnSettledAmountDueToInvalidBankDetails
import `in`.okcredit.collection_ui.usecase.ShouldShowReferralBanner
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class FindInfoBannerForMerchantQr @Inject constructor(
    private val getCollectionMerchantProfile: Lazy<GetCollectionMerchantProfile>,
    private val shouldShowReferralBanner: Lazy<ShouldShowReferralBanner>,
    private val getKycStatus: Lazy<GetKycStatus>,
    private val getUnSettledAmountDueToInvalidBankDetails: Lazy<GetUnSettledAmountDueToInvalidBankDetails>,
) {

    fun execute() = Observable.combineLatest(
        getCollectionMerchantProfile.get().execute(),
        shouldShowReferralBanner.get().execute(),
        getKycStatus.get().execute(),
        getUnSettledAmountDueToInvalidBankDetails.get().execute(
            OnlinePaymentErrorCode.EP001.value,
            OnlinePaymentsContract.PaymentStatus.PAYOUT_FAILED.value
        ),
        ::convertToInfoBanner
    ).distinctUntilChanged()

    private fun convertToInfoBanner(
        collectionMerchantProfile: CollectionMerchantProfile,
        showReferralBanner: Boolean,
        kycStatus: KycStatus,
        unsettledAmount: Double,
    ): InfoBanner {
        if (collectionMerchantProfile.payment_address.isEmpty()) {
            return InfoBanner.None
        }

        return when (kycStatus) {
            KycStatus.FAILED -> InfoBanner.KycFailed
            KycStatus.PENDING -> InfoBanner.KycPending
            KycStatus.COMPLETE -> checkInfoForCompleteKyc(
                collectionMerchantProfile,
                showReferralBanner,
                unsettledAmount
            )
            KycStatus.NOT_SET -> checkInfoForDailyOrTrialLimit(collectionMerchantProfile, unsettledAmount)
        }
    }

    private fun checkInfoForCompleteKyc(
        collectionMerchantProfile: CollectionMerchantProfile,
        showReferralBanner: Boolean,
        unsettledAmount: Double,
    ): InfoBanner {
        if (collectionMerchantProfile.remainingLimit <= 0L) {
            return InfoBanner.KycDoneLimitReached(collectionMerchantProfile.limit)
        }

        if (unsettledAmount > 0) {
            return InfoBanner.RefundAlert(unsettledAmount)
        }

        if (showReferralBanner) {
            return InfoBanner.TargetedReferral
        }

        return InfoBanner.KycDoneLimitAvailable(
            limit = collectionMerchantProfile.limit,
            remainingLimit = collectionMerchantProfile.remainingLimit
        )
    }

    private fun checkInfoForDailyOrTrialLimit(
        collectionMerchantProfile: CollectionMerchantProfile,
        unsettledAmount: Double,
    ): InfoBanner {
        return if (collectionMerchantProfile.remainingLimit <= 0L) {
            if (collectionMerchantProfile.limitType == CollectionMerchantProfile.TRIAL) {
                InfoBanner.TrialLimitReached(collectionMerchantProfile.limit)
            } else {
                InfoBanner.DailyLimitReached(collectionMerchantProfile.limit)
            }
        } else if (unsettledAmount > 0) {
            InfoBanner.RefundAlert(unsettledAmount)
        } else {
            if (collectionMerchantProfile.limitType == CollectionMerchantProfile.TRIAL) {
                InfoBanner.TrialLimitAvailable(collectionMerchantProfile.limit)
            } else {
                InfoBanner.DailyLimitAvailable(collectionMerchantProfile.limit)
            }
        }
    }

    sealed class InfoBanner {
        object None : InfoBanner()

        data class DailyLimitAvailable(val limit: Long) : InfoBanner()

        data class TrialLimitAvailable(val limit: Long) : InfoBanner()

        data class DailyLimitReached(val limit: Long) : InfoBanner()

        data class TrialLimitReached(val limit: Long) : InfoBanner()

        object KycPending : InfoBanner()

        object KycFailed : InfoBanner()

        data class KycDoneLimitAvailable(val limit: Long, val remainingLimit: Long) : InfoBanner()

        data class KycDoneLimitReached(val limit: Long) : InfoBanner()

        data class RefundAlert(val unSettleAmountDueToInvalidBank: Double) : InfoBanner()

        object TargetedReferral : InfoBanner()
    }
}
