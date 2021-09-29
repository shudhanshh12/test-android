package `in`.okcredit.collection_ui.ui.home.merchant_qr

import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.QrCodeBuilder
import `in`.okcredit.collection_ui.ui.home.merchant_qr.QrCodeContract.*
import `in`.okcredit.collection_ui.ui.home.usecase.FindInfoBannerForMerchantQr
import `in`.okcredit.shared.base.Reducer
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.itOrBlank

object QrCodeStateReducer : Reducer<State, PartialState> {

    override fun reduce(current: State, partial: PartialState): State {
        val tempState = when (partial) {
            is PartialState.ErrorState -> current
            is PartialState.NoChange -> current
            is PartialState.SetCollectionMerchantProfile -> setStateFromMerchantProfile(current, partial)
            is PartialState.SetBusiness -> current.copy(
                responseData = current.responseData.copy(business = partial.business),
            )
            is PartialState.SetKycRiskCategory -> setStateForKyc(current, partial)
            is PartialState.SetLatestPayment -> current.copy(
                onlinePaymentState = setOnlinePaymentState(current, partial)
            )
            is PartialState.SetNewCount -> current.copy(
                onlinePaymentState = setOnlinePaymentState(current, partial)
            )
            is PartialState.SetIsPasswordEnabled -> current.copy(
                responseData = current.responseData.copy(isSetPassword = partial.isSetPassword),
            )
            is PartialState.SetIsFourDigitPin -> current.copy(
                responseData = current.responseData.copy(isSetPassword = partial.isFourDigitPinSet),
            )
            is PartialState.SetIsMerchantPrefSync -> current.copy(
                responseData = current.responseData.copy(isSetPassword = partial.isMerchantPrefSync),
            )
            is PartialState.SetEducationType -> current
            is PartialState.SetUnSettleAmountDueToInvalidBank -> {
                setStateForUnsettledAmount(current, partial)
            }
            is PartialState.ShouldShowReferralBanner -> current.copy(
                responseData = current.responseData.copy(showReferralBanner = partial.show)
            )
            is PartialState.SetTotalOnlinePaymentCounts -> current.copy(
                responseData = current.responseData.copy(totalOnlinePaymentTxnCount = partial.count),
                showOnlinePayments = partial.count > 0
            )
            is PartialState.SetShowOrderQr -> current.copy(
                showOrderQr = partial.show
            )
            is PartialState.SetInfoBanner -> current.copy(
                infoBanner = partial.infoBanner
            )
        }

        val showAddBankDetails = tempState.responseData.collectionMerchantProfile.payment_address.isBlank() ||
            tempState.infoBanner is FindInfoBannerForMerchantQr.InfoBanner.RefundAlert
        return tempState.copy(
            infoBanner = tempState.infoBanner,
            merchantCollectionState = buildMerchantCollectionUiState(tempState),
            showAddBankDetails = showAddBankDetails,
        )
    }

    private fun buildMerchantCollectionUiState(tempState: State): MerchantCollectionState {
        val showPaymentViews = tempState.responseData.collectionMerchantProfile.payment_address.isNotBlank() &&
            tempState.responseData.unSettleAmountDueToInvalidBank == 0.0

        val showQr = !tempState.responseData.collectionMerchantProfile.merchant_vpa.isNullOrBlank() &&
            tempState.responseData.unSettleAmountDueToInvalidBank == 0.0 &&
            tempState.responseData.collectionMerchantProfile.merchantQrEnabled

        return tempState.merchantCollectionState.copy(
            merchantName = findMerchantName(tempState),
            merchantProfileImage = tempState.responseData.business?.profileImage,
            paymentAddress = tempState.responseData.collectionMerchantProfile.payment_address,
            qrIntent = buildQrIntent(tempState.responseData.collectionMerchantProfile),
            showPaymentViews = showPaymentViews,
            showQrLocked = !showQr,
        )
    }

    private fun setStateForKyc(
        current: State,
        partial: PartialState.SetKycRiskCategory,
    ): State {
        return current.copy(
            responseData = current.responseData.copy(
                kycStatus = partial.kycStatus,
                kycRiskCategory = partial.kycRiskCategory,
            ),
        )
    }

    private fun setStateForUnsettledAmount(
        current: State,
        partial: PartialState.SetUnSettleAmountDueToInvalidBank,
    ): State {
        return current.copy(
            responseData = current.responseData.copy(unSettleAmountDueToInvalidBank = partial.amount),
        )
    }

    private fun setStateFromMerchantProfile(
        current: State,
        partial: PartialState.SetCollectionMerchantProfile,
    ): State {
        return current.copy(
            responseData = current.responseData.copy(collectionMerchantProfile = partial.collectionMerchantProfile),
        )
    }

    private fun setOnlinePaymentState(
        current: State,
        partial: PartialState,
    ): OnlinePaymentState {
        val currentPaymentState = current.onlinePaymentState
        return when (partial) {
            is PartialState.SetLatestPayment -> currentPaymentState.copy(
                amount = partial.onlinePayment.amount,
                type = partial.onlinePayment.type,
            )
            is PartialState.SetNewCount -> currentPaymentState.copy(
                newCount = partial.count
            )
            else -> current.onlinePaymentState
        }
    }

    private fun buildQrIntent(collectionMerchantProfile: CollectionMerchantProfile): String? {
        if (collectionMerchantProfile.merchant_vpa.isNullOrEmpty()) return null
        return QrCodeBuilder.buildQrCode {
            it.payeeAddress = collectionMerchantProfile.merchant_vpa!!
            it.payeeName = collectionMerchantProfile.name
        }
    }

    private fun findMerchantName(current: State): String {
        if (current.responseData.collectionMerchantProfile.name.isNotNullOrBlank()) {
            return current.responseData.collectionMerchantProfile.name.itOrBlank()
        }

        return current.responseData.business?.name.itOrBlank()
    }
}
