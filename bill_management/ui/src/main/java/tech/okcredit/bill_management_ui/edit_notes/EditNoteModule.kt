package tech.okcredit.bill_management_ui.edit_notes

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import javax.inject.Provider

@Module
abstract class EditNoteModule {

    companion object {

        @Provides
        fun initialState(): EditNoteContract.State = EditNoteContract.State()

        @Provides
        @ViewModelParam(BILL_INTENT_EXTRAS.NOTE)
        fun billNote(addNumberDialogFragment: EditNoteFragment): String {
            return addNumberDialogFragment.arguments?.getString(BILL_INTENT_EXTRAS.NOTE)
                ?: throw Exception("note shouldn't be null")
        }

        @Provides
        @ViewModelParam(BILL_INTENT_EXTRAS.BILL_ID)
        fun billId(addNumberDialogFragment: EditNoteFragment): String {
            return addNumberDialogFragment.arguments?.getString(BILL_INTENT_EXTRAS.BILL_ID)
                ?: throw Exception("bill id  shouldn't be null")
        }

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: EditNoteFragment,
            viewModelProvider: Provider<EditNoteViewModel>
        ): MviViewModel<EditNoteContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
