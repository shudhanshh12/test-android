package `in`.okcredit.collection_ui.ui.qr_scanner

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class QrScannerModule {

    companion object {

        @Provides
        fun initialState(): QRScannerContract.State = QRScannerContract.State()

        @Provides
        fun viewModel(
            activity: QrScannerActivity,
            viewModelProvider: Provider<QrScannerViewModel>
        ): MviViewModel<QRScannerContract.State> = activity.createViewModel(viewModelProvider)
    }
}
