package tech.okcredit.bill_management_ui.selected_bills.selectedimage._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.bill_management_ui.selected_bills.selectedimage.SelectedImageContract
import tech.okcredit.bill_management_ui.selected_bills.selectedimage.SelectedImageFragment
import tech.okcredit.bill_management_ui.selected_bills.selectedimage.SelectedImageViewModel
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import tech.okcredit.camera_contract.CapturedImage
import javax.inject.Provider

@Module
abstract class SelectedImageModule {

    companion object {

        @Provides
        fun initialState(): SelectedImageContract.State = SelectedImageContract.State()

        @Provides
        fun viewModel(
            fragment: SelectedImageFragment,
            viewModelProvider: Provider<SelectedImageViewModel>
        ): MviViewModel<SelectedImageContract.State> = fragment.createViewModel(viewModelProvider)

        @Provides
        @ViewModelParam("images")
        fun images(fragment: SelectedImageFragment): ArrayList<CapturedImage> {
            return fragment.activity?.intent?.getSerializableExtra("addedImages") as ArrayList<CapturedImage>
        }

        @Provides
        @ViewModelParam(BILL_INTENT_EXTRAS.ACCOUNT_ID)
        fun getAccountId(chatFragment: SelectedImageFragment): String? {
            return chatFragment.activity?.intent?.getStringExtra(BILL_INTENT_EXTRAS.ACCOUNT_ID)
        }
    }
}
