package `in`.okcredit.merchant.customer_ui.addrelationship.ui.manually

import `in`.okcredit.merchant.customer_ui.addrelationship.enum.AddRelationshipFailedError
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import androidx.annotation.StringRes

interface AddRelationshipManuallyContract {
    data class State(
        val isLoading: Boolean = false,
        val relationshipType: Int? = null,
        val name: String = "",
        val mobile: String = "",
        val profile: String? = null,
        val enableConfirmCTA: Boolean = false,
        val source: String = "",
        val defaultMode: String = "",
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class ShowProgress(val show: Boolean) : PartialState()
        data class NameChanged(val relationshipName: String) : PartialState()
        data class MobileNumberChanged(val mobile: String) : PartialState()
        object NoChange : PartialState()
        data class SetProfileImage(val profileImage: String?) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object AddRelationship : Intent()

        object ConfirmButtonClicked : Intent()

        data class AddCustomer(
            val name: String,
            val mobile: String,
            val profileImage: String?,
        ) : Intent()

        data class AddSupplier(
            val name: String,
            val mobile: String,
            val profileImage: String?,
        ) : Intent()

        data class NameChanged(val relationshipName: String) : Intent()
        data class MobileChanged(val mobile: String) : Intent()
        data class SetProfileImage(val profileImage: String?) : Intent()

        object RelationshipAddedAfterOnboarding : Intent()
        object TrackAddCustomerSelectName : Intent()
        object TrackAddCustomerSelectMobile : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object SetResultRelationshipAddedSuccessfully : ViewEvent()

        data class GoToCustomerFragment(val customerId: String) : ViewEvent()

        data class GoToSupplierFragment(val supplierId: String) : ViewEvent()

        data class ShowError(@StringRes val message: Int) : ViewEvent()

        data class AddRelationshipFailed(
            val id: String,
            val name: String?,
            val mobile: String?,
            val profile: String?,
            val errorType: AddRelationshipFailedError,
            val exception: Throwable,
        ) : ViewEvent()
    }
}
