package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views

import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.ReminderItemView.ReminderItemViewListener
import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.TypedEpoxyController
import dagger.Lazy
import javax.inject.Inject

class ReminderController @Inject constructor(
    private val listener: Lazy<ReminderItemViewListener>,
) : TypedEpoxyController<List<ReminderProfile>>(
    EpoxyAsyncUtil.getAsyncBackgroundHandler(),
    EpoxyAsyncUtil.getAsyncBackgroundHandler()
) {
    override fun buildModels(data: List<ReminderProfile>?) {
        data?.forEachIndexed { index, reminderProfile ->
            reminderItemView {
                id("reminderItemView$modelCountBuiltSoFar")
                reminderProfile(reminderProfile)
                listener(listener.get())
                isLastReminderProfile(index + 1 == data.size)
            }
        }
    }
}
