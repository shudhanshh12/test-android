package `in`.okcredit.merchant.rewards.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.InteractionType
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyKey.COLLECTION_ADOPTED
import `in`.okcredit.analytics.PropertyKey.REWARD_AMOUNT
import `in`.okcredit.analytics.PropertyKey.REWARD_ID
import `in`.okcredit.analytics.PropertyKey.SCREEN
import dagger.Lazy
import tech.okcredit.android.base.TempCurrencyUtil
import javax.inject.Inject

class RewardsEventTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {
    companion object {
        private const val CLAIM_REWARD_SUCCESS = "Claim Reward Success"
        private const val CLAIM_REWARD_STATE = "Claim Reward State"

        private const val CLAIM_REWARD_DIALOG_OPENED = "Claim Reward Dialog Opened"
        private const val CLAIM_REWARD_DIALOG = "Claim Reward Dialog"
        private const val SETUP_COLLECTION_DIALOG = "Setup Collection Dialog"
        private const val STATE = "State"
        private const val TYPE = "Type"
        private const val VALUE = "Value"
        private const val SOURCE = "Source"
        private const val REFERENCE_ID = "reference_id"
        private const val PAYMENT_ID = "Payment Id"
        private const val ACCOUNT_ID = "account_id"
        private const val CREATED_BY = "created_by"

        private const val REWARD_POP_UP_UNSCRATCHED = "Reward Pop Up Unscratched"
        private const val REWARD_POP_UP_DISMISSED = "Reward Pop Up Dismissed"
        private const val REWARD_POP_UP_SCRATCHED = "Reward Pop Up Scratched"
        private const val REWARD_POP_UP_SEEN = "Reward Pop Up Seen"
        private const val REWARD_CTA_CLICK = "Reward Status Click"
    }

    fun trackRewardScreenViewed() {
        analyticsProvider.get().trackObjectViewed(Event.REWARD_SCREEN)
    }

    fun trackSetupCollectionViewed() {
        analyticsProvider.get().trackObjectViewed(SETUP_COLLECTION_DIALOG)
    }

    fun trackClaimReward(screen: String, amount: Long, type: String?) {
        val properties = HashMap<String, String>().apply {
            this[PropertyKey.SCREEN] = screen
            this[PropertyKey.REWARD_AMOUNT] = TempCurrencyUtil.formatV2(amount)
            type?.let { this[PropertyKey.TYPE] = it }
        }
        analyticsProvider.get().trackEvents(CLAIM_REWARD_SUCCESS, properties)
    }

    // Todo Harshit: RewardsRevamp Analytics for claimed reward

    fun trackClaimedRewardViewed(amount: Long, type: String?, status: String?) {
        val properties = HashMap<String, String>().apply {
            this[PropertyKey.SCREEN] = Event.REWARD_SCREEN
            this[PropertyKey.REWARD_AMOUNT] = TempCurrencyUtil.formatV2(amount)
            type?.let {
                this[PropertyKey.TYPE] = it
            }
            this[PropertyKey.STATUS] = status?.split("/")?.firstOrNull() ?: "N/A"
        }
        analyticsProvider.get().trackEvents(CLAIM_REWARD_DIALOG_OPENED, properties)
    }

    fun trackRewardDialogInteracted(
        item: String,
        amount: String? = null,
        type: String? = null,
        status: String? = null,
        interactionType: InteractionType = InteractionType.CLICK,
    ) {
        val properties = HashMap<String, String>().apply {
            this[PropertyKey.Item] = item
            amount?.let {
                this[PropertyKey.REWARD_AMOUNT] = it
            }
            type?.let {
                this[PropertyKey.TYPE] = it
            }
            this[PropertyKey.STATUS] = status?.split("/")?.firstOrNull() ?: "N/A"
        }
        analyticsProvider.get().trackObjectInteracted(CLAIM_REWARD_DIALOG, interactionType, properties)
    }

    fun trackClaimRewardState(
        state: String,
    ) {
        val type = when {
            state.contains("on_hold") -> "On Hold"
            state.contains("failed") -> "Failed"
            state.contains("processing") -> "Processing"
            state.contains("claimed") -> "Claimed"
            else -> "UnKnown"
        }
        val properties = mapOf(
            STATE to state,
            TYPE to type
        )
        analyticsProvider.get().trackEvents(CLAIM_REWARD_STATE, properties)
    }

    fun trackRewardPopUpUnscratched(
        screen: String,
        type: String,
        rewardId: String,
        source: String,
        referenceId: String,
        paymentId: String,
        accountId: String,
        collectionStatus: Boolean,
        createdBy: String,
    ) {
        val properties = mapOf(
            SCREEN to screen,
            TYPE to type,
            REWARD_ID to rewardId,
            SOURCE to source,
            REFERENCE_ID to referenceId,
            PAYMENT_ID to paymentId,
            ACCOUNT_ID to accountId,
            COLLECTION_ADOPTED to collectionStatus,
            CREATED_BY to createdBy,
        )

        analyticsProvider.get().trackEvents(REWARD_POP_UP_UNSCRATCHED, properties)
    }

    fun trackRewardPopUpDismissed(
        screen: String,
        type: String,
        rewardId: String,
        source: String,
        referenceId: String,
        paymentId: String,
        accountId: String,
        createdBy: String,
    ) {
        val properties = mapOf(
            SCREEN to screen,
            TYPE to type,
            REWARD_ID to rewardId,
            SOURCE to source,
            REFERENCE_ID to referenceId,
            PAYMENT_ID to paymentId,
            ACCOUNT_ID to accountId,
            CREATED_BY to createdBy,
        )

        analyticsProvider.get().trackEvents(REWARD_POP_UP_DISMISSED, properties)
    }

    fun trackRewardPopUpScratched(
        screen: String,
        type: String,
        rewardId: String,
        source: String,
        referenceId: String,
        paymentId: String,
        accountId: String,
        collectionStatus: Boolean,
        createdBy: String,
    ) {
        val properties = mapOf(
            SCREEN to screen,
            TYPE to type,
            REWARD_ID to rewardId,
            SOURCE to source,
            REFERENCE_ID to referenceId,
            PAYMENT_ID to paymentId,
            ACCOUNT_ID to accountId,
            COLLECTION_ADOPTED to collectionStatus,
            CREATED_BY to createdBy,
        )

        analyticsProvider.get().trackEvents(REWARD_POP_UP_SCRATCHED, properties)
    }

    fun trackRewardPopUpSeen(
        screen: String,
        type: String,
        rewardId: String,
        source: String,
        referenceId: String,
        paymentId: String,
        accountId: String,
        collectionStatus: Boolean,
        createdBy: String,
        amount: Long
    ) {
        val properties = mapOf(
            SCREEN to screen,
            TYPE to type,
            REWARD_ID to rewardId,
            SOURCE to source,
            REFERENCE_ID to referenceId,
            PAYMENT_ID to paymentId,
            ACCOUNT_ID to accountId,
            COLLECTION_ADOPTED to collectionStatus,
            CREATED_BY to createdBy,
            REWARD_AMOUNT to amount
        )

        analyticsProvider.get().trackEvents(REWARD_POP_UP_SEEN, properties)
    }

    fun trackButtonClick(
        screen: String,
        value: String,
        type: String,
        rewardId: String,
        source: String,
        referenceId: String,
        collectionStatus: Boolean,
    ) {
        val properties = mutableMapOf(
            SCREEN to screen,
            TYPE to type,
            VALUE to value,
            REWARD_ID to rewardId,
            SOURCE to source,
            REFERENCE_ID to referenceId,
            COLLECTION_ADOPTED to collectionStatus,
        )

        analyticsProvider.get().trackEvents(REWARD_CTA_CLICK, properties)
    }
}
