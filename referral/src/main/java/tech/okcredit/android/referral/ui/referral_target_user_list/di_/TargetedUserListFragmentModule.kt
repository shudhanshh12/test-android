package tech.okcredit.android.referral.ui.referral_target_user_list.di_

import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.android.referral.ui.referral_target_user_list.ReferralTargetedUsersListContract
import tech.okcredit.android.referral.ui.referral_target_user_list.ReferralTargetedUsersListFragment
import tech.okcredit.android.referral.ui.referral_target_user_list.ReferralTargetedUsersListViewModel
import tech.okcredit.android.referral.ui.referral_target_user_list.views.ConvertedTargetedUserItemView.ConvertedTargetedUserActionListener
import tech.okcredit.android.referral.ui.referral_target_user_list.views.UnconvertedTargetedUserItemView.UnconvertedTargetedUserActionListener
import javax.inject.Provider

@Module
abstract class TargetedUserListFragmentModule {

    @Binds
    abstract fun unconvertedListener(fragment: ReferralTargetedUsersListFragment): UnconvertedTargetedUserActionListener

    @Binds
    abstract fun convertListener(fragment: ReferralTargetedUsersListFragment): ConvertedTargetedUserActionListener

    companion object {

        @Provides
        fun initialState() = ReferralTargetedUsersListContract.State()

        @Provides
        fun viewModel(
            fragment: ReferralTargetedUsersListFragment,
            viewModelProvider: Provider<ReferralTargetedUsersListViewModel>
        ): MviViewModel<ReferralTargetedUsersListContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
