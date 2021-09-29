package `in`.okcredit.shared.dialogs.bottomsheetloader._di

import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.shared.dialogs.bottomsheetloader.BottomSheetLoaderContract
import `in`.okcredit.shared.dialogs.bottomsheetloader.BottomSheetLoaderScreen
import `in`.okcredit.shared.dialogs.bottomsheetloader.BottomSheetLoaderViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class BottomSheetLoaderModule {

    companion object {

        @Provides
        fun initialState(): BottomSheetLoaderContract.State = BottomSheetLoaderContract.State()

        @Provides
        fun viewModel(
            fragment: BottomSheetLoaderScreen,
            viewModelProvider: Provider<BottomSheetLoaderViewModel>
        ): MviViewModel<BottomSheetLoaderContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
