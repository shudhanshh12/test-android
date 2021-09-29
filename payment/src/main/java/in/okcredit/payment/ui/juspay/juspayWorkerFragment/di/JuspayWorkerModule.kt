package `in`.okcredit.payment.ui.juspay.juspayWorkerFragment.di

import `in`.okcredit.payment.ui.juspay.HyperServiceHolder
import `in`.okcredit.payment.ui.juspay.juspayWorkerFragment.JuspayWorkerContract
import `in`.okcredit.payment.ui.juspay.juspayWorkerFragment.JuspayWorkerFragment
import `in`.okcredit.payment.ui.juspay.juspayWorkerFragment.JuspayWorkerViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class JuspayWorkerModule {

    companion object {

        @Provides
        fun initialStateWorker(): JuspayWorkerContract.State = JuspayWorkerContract.State()

        @Provides
        fun hyperServiceHolder(): HyperServiceHolder = HyperServiceHolder()

        @Provides
        fun workerPresenter(
            fragment: JuspayWorkerFragment,
            viewModelProvider: Provider<JuspayWorkerViewModel>
        ): MviViewModel<JuspayWorkerContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
