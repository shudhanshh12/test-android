package tech.okcredit.bill_management_ui.billintroductionbottomsheet._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.bill_management_ui.billintroductionbottomsheet.BillIntroductionBottomSheetContract
import tech.okcredit.bill_management_ui.billintroductionbottomsheet.BillIntroductionBottomSheetScreen
import tech.okcredit.bill_management_ui.billintroductionbottomsheet.BillIntroductionBottomSheetViewModel
import javax.inject.Provider

@Module
abstract class BillIntroductionBottomSheetModule {

    companion object {

        @Provides
        fun initialState(): BillIntroductionBottomSheetContract.State = BillIntroductionBottomSheetContract.State()

        @Provides
        fun viewModel(
            fragment: BillIntroductionBottomSheetScreen,
            viewModelProvider: Provider<BillIntroductionBottomSheetViewModel>
        ): MviViewModel<BillIntroductionBottomSheetContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
