package `in`.okcredit.collection_ui.ui.home.adoption

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class CollectionAdoptionV2Module {

    companion object {
        @Provides
        fun initialState(collectionAdoptionFragment: CollectionAdoptionV2Fragment): CollectionAdoptionV2Contract.State {
            return CollectionAdoptionV2Contract.State(
                referredByMerchantId = collectionAdoptionFragment.requireActivity().intent
                    .getStringExtra(CollectionAdoptionV2Fragment.ARG_REFERRAL_MERCHANT_ID) ?: ""
            )
        }

        @Provides
        fun viewModel(
            fragment: CollectionAdoptionV2Fragment,
            viewModelProvider: Provider<CollectionAdoptionV2ViewModel>
        ): MviViewModel<CollectionAdoptionV2Contract.State> = fragment.createViewModel(viewModelProvider)
    }
}
