package `in`.okcredit.collection_ui.ui.home.add

import `in`.okcredit.collection.contract.CollectionDestinationType
import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface AddMerchantDestinationContract {

    companion object {
        const val INVALID_ACCOUNT_NUMBER = 101
        const val INVALID_IFSC_CODE = 102
        const val INVALID_NAME = 103
        const val INVALID_ACCOUNT_NUMBER_AND_IFSC_CODE = 104
    }

    data class State(
        val isNetworkError: Boolean = false,
        val serverAPIError: Boolean = false,
        val adoptionMode: String = CollectionDestinationType.BANK.value,
        val confirmLoaderStatus: Boolean = false,
        val invalidPaymentAddressError: Boolean = false,
        val collectionMerchantProfile: CollectionMerchantProfile? = null,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val business: Business? = null,
        val showConfirmUI: Boolean = false,
        val showVerifyLoader: Boolean = false,
        val paymentAccountName: String? = null,
        val enteredAccountNumber: String = "",
        val enteredIfsc: String = "",
        val enteredUPI: String = "",
        val isValidIfsc: Boolean = false,
        val success: Boolean = false,
        val updateEta: Long = 0,
        val referredByMerchantId: String = "",

        // Activation
        val isMerchantComingFromRewardScreen: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class Success(val eta: Long) : PartialState()

        object ErrorState : PartialState()

        data class SetNetworkError(val isNetworkError: Boolean) : PartialState()

        data class SetAdoptionMode(val adoptionMode: String) : PartialState()

        data class UpdateMerchantLoaderStatus(val confirmLoaderStatus: Boolean) : PartialState()

        data class ShowVerifyLoader(val showVerifyLoader: Boolean) : PartialState()

        data class InvalidPaymentAddressError(val invalidPaymentAddressError: Boolean) : PartialState()

        data class ServerAPIError(val serverAPIError: Boolean) :
            PartialState()

        data class SetCollectionMerchantProfile(val collectionMerchantProfile: CollectionMerchantProfile) :
            PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        data class SetBusiness(val business: `in`.okcredit.merchant.contract.Business) : PartialState()

        data class ShowConfirmUI(val showConfirmUI: Boolean) : PartialState()

        data class SetPaymentAccountName(val showConfirmUI: Boolean, val paymentAccountName: String) : PartialState()

        data class EnteredAccountNumber(val enteredAccountNumber: String) : PartialState()

        data class EnteredIfsc(val enteredIfsc: String, val isValidIfsc: Boolean) : PartialState()

        data class EnteredUPI(val enteredUPI: String) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class SetUpiVpa(val upiVpa: String) : Intent()

        data class ConfirmBankAccount(val paymentAddress: String, val merchantId: String?) : Intent()

        data class SetAdoptionMode(val adoptionMode: String) : Intent()

        data class ShowConfirmUI(
            val showConfirmUI: Boolean,
            val paymentAddress: String,
            val paymentAddressType: String,
            val isUpdate: Boolean
        ) : Intent()

        data class EnteredAccountNumber(val enteredAccountNumber: String) : Intent()

        data class EnteredIfsc(val enteredIfsc: String) : Intent()

        data class EnteredUPI(val enteredUPI: String) : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {
        object GoToLogin : ViewEvents()

        object OnAccountAddedSuccessfully : ViewEvents()
    }
}
