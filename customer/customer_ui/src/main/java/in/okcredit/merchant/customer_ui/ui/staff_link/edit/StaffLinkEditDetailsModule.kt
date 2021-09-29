package `in`.okcredit.merchant.customer_ui.ui.staff_link.edit

import `in`.okcredit.merchant.customer_ui.ui.staff_link.edit.StaffLinkEditDetailsContract.*
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import java.lang.IllegalArgumentException
import javax.inject.Provider

@Module
abstract class StaffLinkEditDetailsModule {

    companion object {

        @Provides
        fun initialState(fragment: StaffLinkEditDetailsFragment): State {
            val linkId = fragment.arguments?.getString(StaffLinkEditDetailsContract.ARG_LINK_ID)
                ?: throw IllegalArgumentException("link id expected")
            val link = fragment.arguments?.getString(StaffLinkEditDetailsContract.ARG_LINK)
            val linkCreateTime = fragment.arguments?.getLong(StaffLinkEditDetailsContract.ARG_LINK_CREATE_TIME)
            return State(
                linkId = linkId,
                link = link,
                linkCreateTime = linkCreateTime
            )
        }

        @Provides
        @ViewModelParam("selected_customers")
        fun customerIds(fragment: StaffLinkEditDetailsFragment): List<String>? {
            return fragment.arguments?.getStringArrayList(StaffLinkEditDetailsContract.ARG_SELECTED_CUSTOMERS)
        }

        @Provides
        fun presenter(
            fragment: StaffLinkEditDetailsFragment,
            viewModelProvider: Provider<StaffLinkEditDetailsViewModel>,
        ): MviViewModel<State> = fragment.createViewModel(viewModelProvider)
    }
}
