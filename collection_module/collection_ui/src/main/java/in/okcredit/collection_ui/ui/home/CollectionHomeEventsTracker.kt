package `in`.okcredit.collection_ui.ui.home

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.collection_ui.ui.home.CollectionHomeEventsTracker.Type.INVITE_AND_EARN
import `in`.okcredit.collection_ui.ui.home.CollectionHomeEventsTracker.Type.KYC_LIMIT_FOR_COLLECTION
import `in`.okcredit.collection_ui.ui.home.CollectionHomeEventsTracker.Type.KYC_LIMIT_REACHED
import `in`.okcredit.collection_ui.ui.home.CollectionHomeEventsTracker.Type.NON_KYC_LIMIT_REACHED
import `in`.okcredit.collection_ui.ui.home.CollectionHomeEventsTracker.Type.NON_KYC_PENDING
import `in`.okcredit.collection_ui.ui.home.CollectionHomeEventsTracker.Type.NON_KYC_REJECTED
import `in`.okcredit.collection_ui.ui.home.CollectionHomeEventsTracker.Type.NON_KYC_TRIAL_LIMIT_FOR_COLLECTION
import `in`.okcredit.collection_ui.ui.home.CollectionHomeEventsTracker.Type.SETTLEMENT_BLOCKED
import `in`.okcredit.collection_ui.ui.home.usecase.FindInfoBannerForMerchantQr
import dagger.Lazy
import javax.inject.Inject

class CollectionHomeEventsTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    object Events {
        const val COLLECTIONS_ADOPTION_SCREEN_MESSAGE_DISPLAYED = "collections_adoption_screen_message_displayed QR"
        const val SEND_PAYMENT_REQUEST = "send_payment_request"
        const val SELECT_CONTACT_FOR_SEND_PAYMENT_REQUEST = "select_contact_for_send_payment_request"
        const val VIEW_MERCHANT_QR = "view_merchant_qr"
        const val DISMISS_MERCHANT_QR = "dismiss_merchant_qr"
        const val ORDER_QR = "order_qr"
    }

    object Params {
        const val MESSAGE_TYPE = "message_type"
        const val TYPE = "type"
        const val ACCOUNT_ID = "account_id"
    }

    object Type {
        const val NON_KYC_TRIAL_LIMIT_FOR_COLLECTION = "non_kyc_trial_limit_for_collection"
        const val NON_KYC_LIMIT_REACHED = "non_kyc_limit_reached"
        const val NON_KYC_PENDING = "non_kyc_pending"
        const val NON_KYC_REJECTED = "non_kyc_rejected"
        const val KYC_LIMIT_FOR_COLLECTION = "kyc_limit_for_collection"
        const val KYC_LIMIT_REACHED = "kyc_limit_reached"
        const val SETTLEMENT_BLOCKED = "settlement_blocked"
        const val INVITE_AND_EARN = "invite_and_earn"

        const val SEND = "send"
        const val RECEIVE = "receive"
    }

    fun trackInfoMessageDisplayed(messageType: FindInfoBannerForMerchantQr.InfoBanner) {
        val eventProperties = mapOf(
            Params.MESSAGE_TYPE to findMessageTypeFromInfoBanner(messageType),
        )
        analyticsProvider.get().trackEvents(Events.COLLECTIONS_ADOPTION_SCREEN_MESSAGE_DISPLAYED, eventProperties)
    }

    private fun findMessageTypeFromInfoBanner(messageType: FindInfoBannerForMerchantQr.InfoBanner): Any {
        return when (messageType) {
            is FindInfoBannerForMerchantQr.InfoBanner.DailyLimitAvailable -> KYC_LIMIT_FOR_COLLECTION
            is FindInfoBannerForMerchantQr.InfoBanner.DailyLimitReached -> NON_KYC_LIMIT_REACHED
            is FindInfoBannerForMerchantQr.InfoBanner.KycDoneLimitAvailable -> KYC_LIMIT_FOR_COLLECTION
            is FindInfoBannerForMerchantQr.InfoBanner.KycDoneLimitReached -> KYC_LIMIT_REACHED
            FindInfoBannerForMerchantQr.InfoBanner.KycFailed -> NON_KYC_REJECTED
            FindInfoBannerForMerchantQr.InfoBanner.KycPending -> NON_KYC_PENDING
            is FindInfoBannerForMerchantQr.InfoBanner.RefundAlert -> SETTLEMENT_BLOCKED
            FindInfoBannerForMerchantQr.InfoBanner.TargetedReferral -> INVITE_AND_EARN
            is FindInfoBannerForMerchantQr.InfoBanner.TrialLimitAvailable -> NON_KYC_TRIAL_LIMIT_FOR_COLLECTION
            is FindInfoBannerForMerchantQr.InfoBanner.TrialLimitReached -> NON_KYC_LIMIT_REACHED
            else -> ""
        }
    }

    fun trackSendPaymentRequest(type: String) {
        val eventProperties = mapOf<String, Any>(
            Params.TYPE to type,
        )
        analyticsProvider.get().trackEvents(Events.SEND_PAYMENT_REQUEST, eventProperties)
    }

    fun trackSelectContactSendPaymentRequest(type: String, accountId: String) {
        val eventProperties = mapOf<String, Any>(
            Params.TYPE to type,
            Params.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(Events.SELECT_CONTACT_FOR_SEND_PAYMENT_REQUEST, eventProperties)
    }

    fun trackViewMerchantQr() {
        analyticsProvider.get().trackEvents(Events.VIEW_MERCHANT_QR)
    }

    fun trackDismissMerchantQr() {
        analyticsProvider.get().trackEvents(Events.DISMISS_MERCHANT_QR)
    }

    fun trackOrderQr() {
        analyticsProvider.get().trackEvents(Events.ORDER_QR)
    }
}
