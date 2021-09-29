package `in`.okcredit.merchant.customer_ui.ui.staff_link.add

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class StaffLinkAddCustomerModule {

    companion object {

        @Provides
        fun initialState(fragment: StaffLinkAddCustomerFragment): StaffLinkAddCustomerContract.State {
            val linkId = fragment.arguments?.getString(StaffLinkAddCustomerContract.ARG_LINK_ID)
            val link = fragment.arguments?.getString(StaffLinkAddCustomerContract.ARG_LINK)
            val linkCreateTime = fragment.arguments?.getLong(StaffLinkAddCustomerContract.ARG_LINK_CREATE_TIME, 0L) ?: 0L
            val preSelectedCustomerIds =
                fragment.arguments?.getStringArrayList(StaffLinkAddCustomerContract.ARG_SELECTED_CUSTOMERS)?.toSet()
            return StaffLinkAddCustomerContract.State(
                linkId = linkId,
                link = link,
                linkCreateTime = linkCreateTime,
                selectedCustomerIds = preSelectedCustomerIds ?: emptySet()
            )
        }

        @Provides
        @ViewModelParam("selected_customers")
        fun customerIds(fragment: StaffLinkAddCustomerFragment): Set<String>? {
            return fragment.arguments?.getStringArrayList(StaffLinkAddCustomerContract.ARG_SELECTED_CUSTOMERS)?.toSet()
        }

        @Provides
        fun viewModel(
            fragment: StaffLinkAddCustomerFragment,
            viewModelProvider: Provider<StaffLinkAddCustomerViewModel>,
        ): MviViewModel<StaffLinkAddCustomerContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
