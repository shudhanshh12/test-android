package `in`.okcredit.collection_ui.ui.referral.education.di

import `in`.okcredit.collection_ui.ui.referral.TargetedReferralActivity
import `in`.okcredit.collection_ui.ui.referral.education.ReferralEducationContract
import `in`.okcredit.collection_ui.ui.referral.education.ReferralEducationFragment
import `in`.okcredit.collection_ui.ui.referral.education.ReferralEducationViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class ReferralEducationModule {

    companion object {

        @Provides
        fun initialState(fragment: ReferralEducationFragment): ReferralEducationContract.State {
            return ReferralEducationContract.State(
                customerIdFrmLedger = fragment.arguments?.getString(TargetedReferralActivity.EXTRA_CUSTOMER_ID_FROM_LEDGER)
            )
        }

        @Provides
        fun viewModel(
            fragment: ReferralEducationFragment,
            viewModelProvider: Provider<ReferralEducationViewModel>
        ): MviViewModel<ReferralEducationContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
