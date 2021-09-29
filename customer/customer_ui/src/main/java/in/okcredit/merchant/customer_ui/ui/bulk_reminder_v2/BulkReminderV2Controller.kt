package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2

import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.BulkReminderEpoxyModel
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.BulkReminderEpoxyModel.*
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile.ReminderType.PENDING_REMINDER
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.BulkReminderTab.BulkReminderTabListener
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.TopBanner.TopBannerListener
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.bulkReminderTab
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.congratulationBanner
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.topBanner
import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.TypedEpoxyController
import dagger.Lazy
import javax.inject.Inject

class BulkReminderV2Controller @Inject constructor(
    private val bulkReminderTabListener: Lazy<BulkReminderTabListener>,
    private val topBannerListener: Lazy<TopBannerListener>
) : TypedEpoxyController<List<BulkReminderEpoxyModel>>(
    EpoxyAsyncUtil.getAsyncBackgroundHandler(),
    EpoxyAsyncUtil.getAsyncBackgroundHandler()
) {
    override fun buildModels(data: List<BulkReminderEpoxyModel>?) {
        data?.forEach {
            when (it) {
                is TopBanner -> renderTopBanner(it)
                is ReminderTab -> renderReminderTab(it)
                is CongratulationBanner -> renderCongratulationBanner()
            }
        }
    }

    private fun renderCongratulationBanner() {
        congratulationBanner {
            id("congratulationBanner")
        }
    }

    private fun renderReminderTab(tab: ReminderTab) {
        if (tab.reminderTabType == PENDING_REMINDER) {
            bulkReminderTab {
                id("pendingReminderTab$modelCountBuiltSoFar")
                reminderTab(tab)
                listener(bulkReminderTabListener.get())
            }
        } else {
            bulkReminderTab {
                id("todaysReminderTab$modelCountBuiltSoFar")
                reminderTab(tab)
                listener(bulkReminderTabListener.get())
            }
        }
    }

    private fun renderTopBanner(topBanner: TopBanner) {
        topBanner {
            id("topBanner")
            topBanner(topBanner)
            listener(topBannerListener.get())
        }
    }
}
