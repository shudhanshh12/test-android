package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.bulk_reminder_tab

import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.BulkReminderTab
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.ReminderItemView
import `in`.okcredit.shared.base.IBaseLayoutViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module
abstract class BulkReminderTabModule {

    @Binds
    @Reusable
    abstract fun listener(BulkReminderTab: BulkReminderTab): ReminderItemView.ReminderItemViewListener

    companion object {

        @Provides
        fun initialState() = BulkReminderTabContract.State

        @Provides
        fun sendMessagePresenter(
            bulkReminderTabViewModel: BulkReminderTabViewModel,
        ): IBaseLayoutViewModel<BulkReminderTabContract.State> {
            return bulkReminderTabViewModel
        }
    }
}
