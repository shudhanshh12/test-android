package `in`.okcredit.collection_ui.ui.home.add._di

import `in`.okcredit.analytics.Event
import `in`.okcredit.collection_ui.ui.home.add.AddMerchantDestinationContract
import `in`.okcredit.collection_ui.ui.home.add.AddMerchantDestinationDialog
import `in`.okcredit.collection_ui.ui.home.add.AddMerchantDestinationViewModel
import `in`.okcredit.collection_ui.ui.home.adoption.CollectionAdoptionV2Fragment
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.app_contract.AppConstants
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class AddMerchantDestinationModule {

    companion object {

        @Provides
        fun initialState(addMerchantDestinationDialog: AddMerchantDestinationDialog): AddMerchantDestinationContract.State =
            AddMerchantDestinationContract.State(
                isMerchantComingFromRewardScreen = addMerchantDestinationDialog.arguments?.getString(
                    AddMerchantDestinationDialog.ARG_SOURCE
                ) == Event.REWARD_SCREEN,
                referredByMerchantId = addMerchantDestinationDialog.arguments?.getString(
                    CollectionAdoptionV2Fragment.ARG_REFERRAL_MERCHANT_ID
                ) ?: "",
            )

        @Provides
        @ViewModelParam(AppConstants.ARG_PAYMENT_METHOD_TYPE)
        fun paymentMode(addMerchantDestinationDialog: AddMerchantDestinationDialog): String? {
            return addMerchantDestinationDialog.arguments?.getString(AppConstants.ARG_PAYMENT_METHOD_TYPE)
        }

        @Provides
        @ViewModelParam(AppConstants.ARG_IS_UPDATE_COLLECTION)
        fun isUpdateCollection(addMerchantDestinationDialog: AddMerchantDestinationDialog): Boolean {
            return addMerchantDestinationDialog.arguments?.getBoolean(AppConstants.ARG_IS_UPDATE_COLLECTION) ?: false
        }

        @Provides
        @ViewModelParam(AddMerchantDestinationDialog.ARG_ASYNC_REQUEST)
        fun asyncRequest(addMerchantDestinationDialog: AddMerchantDestinationDialog): Boolean {
            return addMerchantDestinationDialog.arguments?.getBoolean(AddMerchantDestinationDialog.ARG_ASYNC_REQUEST)
                ?: false
        }

        @Provides
        @ViewModelParam(AddMerchantDestinationDialog.ARG_SOURCE)
        fun source(addMerchantDestinationDialog: AddMerchantDestinationDialog): String {
            return addMerchantDestinationDialog.arguments?.getString(AddMerchantDestinationDialog.ARG_SOURCE)
                ?: AddMerchantDestinationDialog.DEFAULT_SOURCE
        }

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: AddMerchantDestinationDialog,
            viewModelProvider: Provider<AddMerchantDestinationViewModel>,
        ): MviViewModel<AddMerchantDestinationContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
