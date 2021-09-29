package `in`.okcredit.collection_ui.ui.home

import `in`.okcredit.collection_ui.ui.home.adoption.CollectionAdoptionV2Fragment
import `in`.okcredit.collection_ui.ui.home.adoption.CollectionAdoptionV2Module
import `in`.okcredit.collection_ui.ui.home.merchant_qr.QRCodeModule
import `in`.okcredit.collection_ui.ui.home.merchant_qr.QrCodeFragment
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class CollectionsHomeActivityModule {

    @ContributesAndroidInjector(modules = [QRCodeModule::class])
    abstract fun qrCodeScreen(): QrCodeFragment

    @ContributesAndroidInjector(modules = [CollectionAdoptionV2Module::class])
    abstract fun collectionAdoptionScreen(): CollectionAdoptionV2Fragment

    companion object {
        @Provides
        fun initialState(): CollectionsHomeActivityContract.State = CollectionsHomeActivityContract.State()

        @Provides
        @ViewModelParam("referral_merchant_id")
        fun referralMerchantId(collectionAdoptionFragment: CollectionsHomeActivity): String? {
            return collectionAdoptionFragment.intent?.getStringExtra(CollectionAdoptionV2Fragment.ARG_REFERRAL_MERCHANT_ID)
        }

        @Provides
        fun viewModel(
            fragment: CollectionsHomeActivity,
            viewModelProvider: Provider<CollectionsHomeActivityViewModel>
        ): MviViewModel<CollectionsHomeActivityContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
