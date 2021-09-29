package `in`.okcredit.collection_ui.ui.benefits

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class CollectionBenefitsModule {

    companion object {
        @Provides
        fun initialState(activity: CollectionBenefitsActivity): CollectionsBenefitContract.State {
            val sendReminder = activity.intent.getBooleanExtra("send_reminder", false)
            val customerId = activity.intent.getStringExtra("customer_id")
            return CollectionsBenefitContract.State(sendReminder = sendReminder, customerId = customerId)
        }

        @Provides
        fun viewModel(
            activity: CollectionBenefitsActivity,
            viewModelProvider: Provider<CollectionsBenefitViewModel>
        ): MviViewModel<CollectionsBenefitContract.State> = activity.createViewModel(viewModelProvider)
    }
}
