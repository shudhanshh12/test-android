package tech.okcredit.home.ui.customer_tab

import `in`.okcredit.dynamicview.data.model.Customization
import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner
import tech.okcredit.home.ui.customer_tab.CustomerTabContract.BulkReminderState

sealed class CustomerTabItem {

    data class HomeCustomerItem(
        val customerId: String,
        val name: String,
        val profileImage: String?,
        val balance: Long,
        val subtitle: String,
        val type: SubtitleType,
        val commonLedger: Boolean,
        val unreadCount: Int,
        val addTxnPermissionDenied: Boolean,
        val showReferralIcon: Boolean,
    ) : CustomerTabItem()

    data class ReferralTargetBannerItem(
        val fullView: Boolean,
        val referralTargetBanner: ReferralTargetBanner,
    ) : CustomerTabItem()

    data class DynamicViewItem(val bannerCustomization: Customization) : CustomerTabItem()

    object FilterItem : CustomerTabItem()

    object EmptyFilterItem : CustomerTabItem()

    object AppLockItem : CustomerTabItem()

    object AddCustomerTutorialItem : CustomerTabItem()

    object UserStoriesItem : CustomerTabItem()

    data class LiveSalesItem(val id: String, val balance: Long, val liveSalesTutorialVisibility: Boolean) :
        CustomerTabItem()

    data class BulkReminderBanner(
        val bulkReminderState: BulkReminderState,
    ) : CustomerTabItem()
}

enum class SubtitleType {
    CUSTOMER_ADDED,
    DUE_TODAY,
    DUE_DATE_PASSED,
    DUE_DATE_INCOMING,
    TRANSACTION_SYNC_DONE,
    TRANSACTION_SYNC_PENDING,
    IMMUTABLE_CUSTOMER,
    DIRTY_CUSTOMER,
    COLLECTION_TARGETED_REFERRAL,
    BULK_REMINDER_BANNER,
}
