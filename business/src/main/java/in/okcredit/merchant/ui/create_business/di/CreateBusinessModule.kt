package `in`.okcredit.merchant.ui.create_business.di

import `in`.okcredit.merchant.ui.create_business.CreateBusinessContract
import `in`.okcredit.merchant.ui.create_business.CreateBusinessDialog
import `in`.okcredit.merchant.ui.create_business.CreateBusinessViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class CreateBusinessModule {

    companion object {

        @Provides
        fun initialState(): CreateBusinessContract.State = CreateBusinessContract.State()

        @Provides
        fun viewModel(
            fragment: CreateBusinessDialog,
            provider: Provider<CreateBusinessViewModel>,
        ): MviViewModel<CreateBusinessContract.State> = fragment.createViewModel(provider)
    }
}
