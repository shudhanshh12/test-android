package `in`.okcredit.collection_ui.ui.referral.invite_list.di

import `in`.okcredit.collection_ui.ui.referral.TargetedReferralActivity
import `in`.okcredit.collection_ui.ui.referral.invite_list.ReferralInviteListContract
import `in`.okcredit.collection_ui.ui.referral.invite_list.ReferralInviteListFragment
import `in`.okcredit.collection_ui.ui.referral.invite_list.ReferralInviteListViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class ReferralInviteListModule {

    companion object {

        @Provides
        fun initialState(fragment: ReferralInviteListFragment): ReferralInviteListContract.State {
            return ReferralInviteListContract.State(
                customerIdFrmLedger = fragment.arguments?.getString(TargetedReferralActivity.EXTRA_CUSTOMER_ID_FROM_LEDGER)
            )
        }

        @Provides
        fun viewModel(
            fragment: ReferralInviteListFragment,
            viewModelProvider: Provider<ReferralInviteListViewModel>,
        ): MviViewModel<ReferralInviteListContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
