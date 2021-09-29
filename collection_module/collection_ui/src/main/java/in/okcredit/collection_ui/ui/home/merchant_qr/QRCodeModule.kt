package `in`.okcredit.collection_ui.ui.home.merchant_qr

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class QRCodeModule {

    companion object {

        @Provides
        fun initialState(): QrCodeContract.State = QrCodeContract.State()

        @Provides
        fun viewModel(
            fragment: QrCodeFragment,
            viewModelProvider: Provider<QrCodeViewModel>
        ): MviViewModel<QrCodeContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
