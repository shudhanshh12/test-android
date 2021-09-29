package `in`.okcredit.merchant.customer_ui.ui.address

import `in`.okcredit.merchant.customer_ui.ui.address.UpdateCustomerAddressBottomSheet.Companion.ARG_CURRENT_ADDRESS
import `in`.okcredit.merchant.customer_ui.ui.address.UpdateCustomerAddressBottomSheet.Companion.ARG_CUSTOMER_ID
import `in`.okcredit.merchant.customer_ui.ui.address.UpdateCustomerAddressContract.State
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class UpdateCustomerAddressModule {

    companion object {

        @Provides
        fun initialState(fragment: UpdateCustomerAddressBottomSheet): State {
            val customerId = fragment.arguments?.getString(ARG_CUSTOMER_ID)
                ?: throw IllegalArgumentException("Customer id is required")
            val address = fragment.arguments?.getString(ARG_CURRENT_ADDRESS)
            return State(
                customerId = customerId,
                currentAddress = address,
            )
        }

        @Provides
        fun presenter(
            fragment: UpdateCustomerAddressBottomSheet,
            viewModelProvider: Provider<UpdateCustomerAddressViewModel>,
        ): MviViewModel<State> = fragment.createViewModel(viewModelProvider)
    }
}
