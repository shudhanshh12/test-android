package `in`.okcredit.user_migration.presentation.ui.display_parsed_data.di_

import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.DisplayParsedDataContract
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.DisplayParsedDataFragment
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.DisplayParsedDataFragmentArgs
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.DisplayParsedDataViewModel
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.views.ItemViewCustomerList
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.views.ItemViewFileName
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class DisplayParsedDataModule {

    @Binds
    abstract fun eventListenerFileContent(fragment: DisplayParsedDataFragment): ItemViewFileName.ItemViewFileListener

    @Binds
    abstract fun eventListenerCustomer(fragment: DisplayParsedDataFragment): ItemViewCustomerList.ItemViewCustomerListener

    companion object {

        @Provides
        fun initialState(): DisplayParsedDataContract.State = DisplayParsedDataContract.State()

        @Provides
        fun arguments(fragment: DisplayParsedDataFragment) =
            DisplayParsedDataFragmentArgs.fromBundle(fragment.requireArguments())

        @Provides
        fun viewModel(
            fragment: DisplayParsedDataFragment,
            viewModelProvider: Provider<DisplayParsedDataViewModel>
        ): MviViewModel<DisplayParsedDataContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
