package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2._di

import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Activity
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2ViewModel
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.BulkReminderTab.BulkReminderTabListener
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.TopBanner
import `in`.okcredit.shared.base.MviViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class BulkReminderV2Module {

    @Binds
    abstract fun activity(activity: BulkReminderV2Activity): AppCompatActivity

    @Binds
    @Reusable
    abstract fun bulkReminderTabListener(activity: BulkReminderV2Activity): BulkReminderTabListener

    @Binds
    @Reusable
    abstract fun topBannerListener(activity: BulkReminderV2Activity): TopBanner.TopBannerListener

    companion object {

        @Provides
        fun initialState(): BulkReminderV2Contract.State = BulkReminderV2Contract.State()

        @Provides
        fun viewModel(
            activity: BulkReminderV2Activity,
            viewModelProvider: Provider<BulkReminderV2ViewModel>,
        ): MviViewModel<BulkReminderV2Contract.State> = activity.createViewModel(viewModelProvider)
    }
}
