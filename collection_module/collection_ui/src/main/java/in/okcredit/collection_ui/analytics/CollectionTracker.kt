package `in`.okcredit.collection_ui.analytics

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.IAnalyticsProvider
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.collection.contract.CollectionDestinationType
import `in`.okcredit.collection.contract.CollectionEventTracker
import `in`.okcredit.collection_ui.analytics.CollectionTracker.CollectionPropertyKey.ACCOUNT_ID
import `in`.okcredit.collection_ui.analytics.CollectionTracker.CollectionPropertyKey.CAMPAIGN
import `in`.okcredit.collection_ui.analytics.CollectionTracker.CollectionPropertyKey.CAMPAIGN_SRC
import `in`.okcredit.collection_ui.analytics.CollectionTracker.CollectionPropertyKey.CONTEXT
import `in`.okcredit.collection_ui.analytics.CollectionTracker.CollectionPropertyKey.FLOW
import `in`.okcredit.collection_ui.analytics.CollectionTracker.CollectionPropertyKey.METHOD
import `in`.okcredit.collection_ui.analytics.CollectionTracker.CollectionPropertyKey.SCREEN
import `in`.okcredit.collection_ui.analytics.CollectionTracker.CollectionPropertyKey.SOURCE
import `in`.okcredit.collection_ui.analytics.CollectionTracker.CollectionPropertyKey.STATUS
import `in`.okcredit.collection_ui.analytics.CollectionTracker.CollectionPropertyKey.TYPE
import dagger.Lazy
import javax.inject.Inject

class CollectionTracker @Inject constructor(private val analyticsProvider: Lazy<IAnalyticsProvider>) :
    CollectionEventTracker {

    object CollectionEvent {
        const val VIEW_COLLECTION_ADOPTION_PAGE = "View Collection Page"
        const val PERMISSION_ACCEPT = "Grant Permission"
        const val PERMISSION_DENIED = "Deny Permission"
        const val STARTED_ADOPT_COLLECTION = "Started Adopt Collection"
        const val SELECTED_COLLECTION_ADAPTION_TYPE = "Selected Collection Adoption Type"
        const val COLLECTION_ADOPTION_COMPLETED = "Collection Adoption Completed"
        const val CHANGE_COLLECTION_METHOD = "Change Collection Method"
        const val ENTER_COLLECTION_DETAILS = "Enter Collection Details"
        const val CONFIRM_COLLECTION_DETAILS = "Confirm Collection Details"
        const val ENTERED_INVALID_COLLECTION_DETAILS = "Entered Invalid Collection Details"
        const val COLLECTION_DETAILS_COMPLETED = "Collection Details Completed"
        const val CHANGE_COLLECTION_DETAILS = "Change Collection Details"
        const val VIEW_CAMERA_PERMISSION = "View Camera Permission"
        const val VIEW_COLLECTION_MORE = "View Collection More"
        const val VIEW_COLLECTION_STATEMENT = "View Collection Statement"
        const val SHARE_QR = "Share QR"
        const val SAVE_QR = "Save QR"
        const val DELETE_COLLECTION = "Delete Collection"
        const val CONFIRM_DELETE_COLLECTION = "Confirm Delete Collection"
        const val CANCEL_DELETE_COLLECTION = "Cancel Delete Collection"
        const val COLLECTION_DELETED = "Collection Deleted"
        const val UPDATE_COLLECTION = "Update Collection"
        const val STARTED_UPDATE_COLLECTION = "Started Update Collection"
        const val COLLECTION_UPDATE_COMPLETED = "Collection Update Completed"
        const val CANCEL_UPDATE_COLLECTION = "Cancel Update Collection"
        const val VIEW_STORAGE_PERMISSION = "View Storage Permission"
        const val CLICKED_CAMERA = "Clicked Camera"
        const val CLICKED_GALLERY = "Clicked Gallery"
        const val CLICKED_TORCH = "Clicked Torch"
        const val VIEW_QR = "View QR"
        const val COLLECTION_QR_CLOSED = "col_qr_closed"
        const val KYC_BANNER_SHOWN = "KYC banner shown"
        const val START_KYC_CLICKED = "Start KYC clicked"
        const val KYC_HOME_PAGE = "KYC Homepage"
        const val KYC_HOME_PAGE_CLICK = "KYC Homepage click"
        const val SECURITY_PIN_SET = "Security Pin Set"
        const val SECURITY_PIN_CHANGED = "Security Pin Changed"
        const val COLLECTION_REFERRAL_GIFT_CLICKED = "collection_referral_gift_clicked"
        const val INVITE_NOW_COLLECTION_CLICKED = "invite_now_collections_clicked"
        const val SHARE_COLLECTION_INVITE = "share_collection_invite"
        const val REMIND_COLLECTION_REFERRAL = "remind_collection_referral"
        const val CONTEXTUAL_INFOGRAPHIC_SCROLL = "collection_adoption_infographic_scroll"
        const val CONTEXTUAL_INFOGRAPHIC_DISMISS = "collection_adoption_infographic_dismiss"
        const val COLLECTION_REFERRAL_GIFT_SHOWN = "collection_referral_gift_shown"
    }

    object CollectionPropertyKey {
        const val TYPE = "Type"
        const val SCREEN = "Screen"
        const val ACCOUNT_ID = "account_id"
        const val METHOD = "Method"
        const val FLOW = "Flow"
        const val ALWAYS = "Always"
        const val SOURCE = "Source"
        const val IS_TORCH_ON = "Is Torch On"
        const val CONTEXT = "context"
        const val CAMPAIGN = "Campaign"
        const val CAMPAIGN_SRC = "Campaign Source"
        const val STATUS = "Status"
    }

    object CollectionPropertyValue {
        const val MERCHANT = "Merchant"
        const val UPI = "Upi"
        const val BANK = "Bank"
        const val MOBILE = "Mobile"
        const val Typing = "Typing"
        const val ACCOUNT_NUMBER = "Account Number"
        const val IFSC = "IFSC"
        const val CAMERA = "Camera"
        const val GALLERY = "Gallery"
        const val ADOPT = "Adopt"
        const val UPDATE = "Update"
        const val COLLECTION_TARGETED_REFERRAL = "collection_targetted_referral"
    }

    object CollectionScreen {
        const val MERCHANT_DESTINATION_SCREEN = "Merchant Destination Screen"
        const val COLLECTION_ADOPTION_PAGE = "Collection Adoption Page"
        const val QR_SCANNER_SCREEN = "QR Scanner Screen"
        const val COLLECTION_ADOPTION_POPUP_SCREEN = "Merchant Collection popup Screen"
        const val INSIGHTS_RELATIONSHIP = "Insights relationship"
        const val MERCHANT_QR = "Merchant QR"
        const val INVITE_N_EARN_SCREEN = "Invite & Earn through Collections"
        const val REFERRAL_INVITE_LIST = "Collections Referrer List"
    }

    fun trackEvents(
        eventName: String,
        type: String? = null,
        screen: String? = null,
        relation: String? = null,
        source: String? = null,
        value: Boolean? = null,
        propertiesMap: PropertiesMap? = null,
        flow: String? = null,
    ) {

        val properties = propertiesMap?.map() ?: mutableMapOf()

        if (!type.isNullOrEmpty()) {
            properties[PropertyKey.TYPE] = type
        }

        if (!screen.isNullOrEmpty()) {
            properties[PropertyKey.SCREEN] = screen
        }

        if (!relation.isNullOrEmpty()) {
            properties[PropertyKey.RELATION] = relation
        }

        if (value != null) {
            properties[PropertyKey.VALUE] = value
        }
        if (flow != null) {
            properties[PropertyKey.FLOW] = flow
        }

        if (!source.isNullOrEmpty()) {
            properties[`in`.okcredit.analytics.PropertyKey.SOURCE] = source
        }

        analyticsProvider.get().trackEvents(eventName, properties)
    }

    fun trackCollectionDetailsCompleted(
        method: String,
        isUpdateCollection: Boolean,
        adoptionMode: String,
        source: String,
        campaign: String? = null,
        campaignSrc: String? = null,
    ) {
        val flow = if (isUpdateCollection) {
            CollectionPropertyValue.UPDATE
        } else {
            CollectionPropertyValue.ADOPT
        }

        val properties = mutableMapOf<String, Any>().apply {
            this[SCREEN] = CollectionScreen.COLLECTION_ADOPTION_POPUP_SCREEN
            this[SOURCE] = source
            this[FLOW] = flow
            this[TYPE] = adoptionMode
            this[METHOD] = method

            campaign?.let {
                this[CAMPAIGN] = campaign
            }
            campaignSrc?.let {
                this[CAMPAIGN_SRC] = campaignSrc
            }
        }
        analyticsProvider.get().trackEvents(CollectionEvent.COLLECTION_DETAILS_COMPLETED, properties)
    }

    fun trackEnterCollectionDetails(
        method: String,
        type: String,
        isUpdateCollection: Boolean,
        source: String,
        campaign: String? = null,
        campaignSrc: String? = null,
    ) {
        val flow = if (isUpdateCollection) {
            CollectionPropertyValue.UPDATE
        } else {
            CollectionPropertyValue.ADOPT
        }

        val properties = mutableMapOf<String, Any>().apply {
            this[SCREEN] = CollectionScreen.COLLECTION_ADOPTION_POPUP_SCREEN
            this[SOURCE] = source
            this[FLOW] = flow
            this[TYPE] = type
            this[METHOD] = method

            campaign?.let {
                this[CAMPAIGN] = campaign
            }
            campaignSrc?.let {
                this[CAMPAIGN_SRC] = campaignSrc
            }
        }

        analyticsProvider.get().trackEvents(CollectionEvent.ENTER_COLLECTION_DETAILS, properties)
    }

    fun trackConfirmCollectionDetails(
        type: String,
        isUpdateCollection: Boolean,
        campaign: String? = null,
        campaignSrc: String? = null,
    ) {
        val flow = if (isUpdateCollection) {
            CollectionPropertyValue.UPDATE
        } else {
            CollectionPropertyValue.ADOPT
        }

        val adoptionType = if (type == CollectionDestinationType.UPI.value) {
            CollectionPropertyValue.UPI
        } else {
            CollectionPropertyValue.BANK
        }

        val properties = mutableMapOf<String, Any>().apply {
            this[SCREEN] = CollectionScreen.COLLECTION_ADOPTION_POPUP_SCREEN
            this[FLOW] = flow
            this[TYPE] = adoptionType

            campaign?.let {
                this[CAMPAIGN] = campaign
            }
            campaignSrc?.let {
                this[CAMPAIGN_SRC] = campaignSrc
            }
        }

        analyticsProvider.get().trackEvents(CollectionEvent.CONFIRM_COLLECTION_DETAILS, properties)
    }

    fun trackEnteredInvalidCollectionDetails(
        isUpdateCollection: Boolean,
        paymentAddressType: String,
        campaign: String? = null,
        campaignSrc: String? = null,
    ) {
        val flow = if (isUpdateCollection) {
            CollectionPropertyValue.UPDATE
        } else {
            CollectionPropertyValue.ADOPT
        }
        val type = if (paymentAddressType == CollectionDestinationType.UPI.value) {
            CollectionPropertyValue.UPI
        } else {
            CollectionPropertyValue.BANK
        }

        val properties = mutableMapOf<String, Any>().apply {
            this[SCREEN] = CollectionScreen.COLLECTION_ADOPTION_POPUP_SCREEN
            this[TYPE] = type
            this[FLOW] = flow
            campaign?.let {
                this[CAMPAIGN] = campaign
            }
            campaignSrc?.let {
                this[CAMPAIGN_SRC] = campaignSrc
            }
        }

        analyticsProvider.get().trackEvents(CollectionEvent.ENTERED_INVALID_COLLECTION_DETAILS, properties)
    }

    fun trackViewQr(screen: String) {
        val properties = mutableMapOf<String, Any>().apply {
            this[PropertyKey.SCREEN] = screen
        }
        analyticsProvider.get().trackEvents(CollectionEvent.VIEW_QR, properties)
    }

    fun trackCallRelationShip(
        screen: String,
        relation: String,
        mobile: String? = null,
        accountId: String? = null,
        isBlocked: Boolean = false,
    ) {
        val properties = mutableMapOf<String, Any>().apply {
            this[PropertyKey.SCREEN] = screen
            this[PropertyKey.RELATION] = relation
            accountId?.let {
                if (accountId.isNotBlank()) this[PropertyKey.ACCOUNT_ID] = accountId
            }
            this[PropertyKey.BLOCKED] = isBlocked
        }

        analyticsProvider.get().trackEvents(Event.CALL_RELATIONSHIP, properties)
    }

    fun trackCollectionQRClosed(
        screen: String,
        relation: String,
        accountId: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>().apply {
            this[PropertyKey.SCREEN] = screen
            this[PropertyKey.RELATION] = relation
            accountId?.let {
                if (accountId.isNotBlank()) this[PropertyKey.ACCOUNT_ID] = accountId
            }
        }

        analyticsProvider.get().trackEvents(CollectionEvent.COLLECTION_QR_CLOSED, properties)
    }

    override fun setUserProperty(key: String, value: String) {
        val properties = mutableMapOf<String, Any>()
        properties[key] = value
        analyticsProvider.get().setUserProperty(properties)
    }

    fun trackStartedAdoptCollection(
        type: String,
        source: String,
        campaign: String? = null,
        campaignSrc: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>().apply {
            this[PropertyKey.SOURCE] = source
            this[PropertyKey.TYPE] = type
            campaign?.let {
                this[CAMPAIGN] = campaign
            }
            campaignSrc?.let {
                this[CAMPAIGN_SRC] = campaignSrc
            }
        }

        analyticsProvider.get().trackEvents(CollectionEvent.STARTED_ADOPT_COLLECTION, properties)
    }

    override fun trackCollectionReferralGiftClicked(accountId: String, screen: String, status: String) {
        val properties = mutableMapOf<String, Any>().apply {
            this[ACCOUNT_ID] = accountId
            this[SCREEN] = screen
            this[STATUS] = status
        }

        analyticsProvider.get().trackEvents(CollectionEvent.COLLECTION_REFERRAL_GIFT_CLICKED, properties)
    }

    fun trackInviteNowCollectionClicked(source: String, screen: String) {
        val properties = mutableMapOf<String, Any>().apply {
            this[PropertyKey.SOURCE] = source
            this[PropertyKey.SCREEN] = screen
        }

        analyticsProvider.get().trackEvents(CollectionEvent.INVITE_NOW_COLLECTION_CLICKED, properties)
    }

    fun trackShareCollectionInvite(accountId: String, source: String, screen: String) {
        val properties = mutableMapOf<String, Any>().apply {
            this[PropertyKey.SOURCE] = source
            this[PropertyKey.SCREEN] = screen
            this[PropertyKey.ACCOUNT_ID] = accountId
        }

        analyticsProvider.get().trackEvents(CollectionEvent.SHARE_COLLECTION_INVITE, properties)
    }

    fun trackRemindCollectionReferral(accountId: String, source: String, screen: String) {
        val properties = mutableMapOf<String, Any>().apply {
            this[PropertyKey.SOURCE] = source
            this[PropertyKey.SCREEN] = screen
            this[PropertyKey.ACCOUNT_ID] = accountId
        }

        analyticsProvider.get().trackEvents(CollectionEvent.REMIND_COLLECTION_REFERRAL, properties)
    }

    fun trackContextualInfoScroll(
        accountId: String,
        type: String,
        keyword: String,
    ) {
        val properties = mutableMapOf<String, Any>().apply {
            this[CONTEXT] = keyword
            this[TYPE] = type
            this[PropertyKey.ACCOUNT_ID] = accountId
        }

        analyticsProvider.get().trackEvents(CollectionEvent.CONTEXTUAL_INFOGRAPHIC_SCROLL, properties)
    }

    fun trackContextualInfoDismiss(
        accountId: String,
        type: String,
        keyword: String,
    ) {
        val properties = mutableMapOf<String, Any>().apply {
            this[CONTEXT] = keyword
            this[TYPE] = type
            this[PropertyKey.ACCOUNT_ID] = accountId
        }

        analyticsProvider.get().trackEvents(CollectionEvent.CONTEXTUAL_INFOGRAPHIC_DISMISS, properties)
    }

    override fun trackCollectionReferralGiftShown(accountId: String, screen: String, type: String, status: String) {
        val properties = mutableMapOf<String, Any>().apply {
            this[ACCOUNT_ID] = accountId
            this[SCREEN] = screen
            this[TYPE] = type
            this[STATUS] = status
        }

        analyticsProvider.get().trackEvents(CollectionEvent.COLLECTION_REFERRAL_GIFT_SHOWN, properties)
    }

    fun trackViewCollectionAdoptionPage(source: String, campaign: String? = null, campaignSrc: String? = null) {
        val properties = mutableMapOf<String, Any>().apply {
            this[SOURCE] = source
            campaign?.let {
                this[CAMPAIGN] = campaign
            }
            campaignSrc?.let {
                this[CAMPAIGN_SRC] = campaignSrc
            }
        }

        analyticsProvider.get().trackEvents(CollectionEvent.VIEW_COLLECTION_ADOPTION_PAGE, properties)
    }

    fun trackChangeCollectionMethod(
        screen: String,
        type: String,
        flow: String,
        campaign: String? = null,
        campaignSrc: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>().apply {
            this[SCREEN] = screen
            this[TYPE] = type
            this[FLOW] = flow

            campaign?.let {
                this[CAMPAIGN] = campaign
            }
            campaignSrc?.let {
                this[CAMPAIGN_SRC] = campaignSrc
            }
        }

        analyticsProvider.get().trackEvents(CollectionEvent.CHANGE_COLLECTION_METHOD, properties)
    }

    fun trackSelectedCollectionAdoptionType(
        type: String,
        source: String,
        method: String,
        flow: String,
        campaign: String? = null,
        campaignSrc: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>().apply {
            this[SOURCE] = source
            this[FLOW] = flow
            this[TYPE] = type
            this[METHOD] = method

            campaign?.let {
                this[CAMPAIGN] = campaign
            }
            campaignSrc?.let {
                this[CAMPAIGN_SRC] = campaignSrc
            }
        }

        analyticsProvider.get().trackEvents(CollectionEvent.SELECTED_COLLECTION_ADAPTION_TYPE, properties)
    }

    fun trackCollectionAdoptionCompleted(
        type: String,
        screen: String,
        campaign: String? = null,
        campaignSrc: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>().apply {
            this[SCREEN] = screen
            this[TYPE] = type

            campaign?.let {
                this[CAMPAIGN] = campaign
            }
            campaignSrc?.let {
                this[CAMPAIGN_SRC] = campaignSrc
            }
        }

        analyticsProvider.get().trackEvents(CollectionEvent.COLLECTION_ADOPTION_COMPLETED, properties)
    }

    fun trackCollectionAdoptionUpdated(
        type: String,
        screen: String,
        campaign: String? = null,
        campaignSrc: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>().apply {
            this[SCREEN] = screen
            this[TYPE] = type

            campaign?.let {
                this[CAMPAIGN] = campaign
            }
            campaignSrc?.let {
                this[CAMPAIGN_SRC] = campaignSrc
            }
        }

        analyticsProvider.get().trackEvents(CollectionEvent.COLLECTION_UPDATE_COMPLETED, properties)
    }
}
