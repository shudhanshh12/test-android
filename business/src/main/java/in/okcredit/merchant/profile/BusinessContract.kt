package `in`.okcredit.merchant.profile

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.BusinessType
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface BusinessContract {

    data class State(

        val loading: Boolean = true,

        val error: Boolean = false,

        val networkError: Boolean = false,

        val business: Business? = null,

        val referralId: String? = null,

        val businessTypes: List<BusinessType> = arrayListOf(),

        val contextualHelpIds: List<String> = emptyList(),

        val canShowMultipleAccountEntry: Boolean = false,
    ) : UiState

    sealed class Intent : UserIntent {

        object Load : Intent()

        data class UpdateBusiness(val business: BusinessType) : Intent()

        data class UpdateProfileImage(val profileImage: Pair<Boolean, String>) : Intent()

        object GoToCategoryScreen : Intent()

        data class ShowProfileBottomSheet(val showFullScreenImage: Boolean) : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object HideLoading : PartialState()

        data class ShowBusiness(val business: Business) : PartialState()

        data class SetReferralData(val referralId: String) : PartialState()

        data class SetBusinessTypes(val businessTypes: List<BusinessType>) : PartialState()

        object SetNetworkError : PartialState()

        object ErrorState : PartialState()

        object NoChange : PartialState()

        data class SetContextualHelpIds(val helpIds: List<String>) : PartialState()

        data class ShowMultipleAccountsEntry(val canShow: Boolean) : PartialState()
    }

    sealed class ViewEvent : BaseViewEvent

    interface Navigator {

        fun gotoLogin()
        fun gotoSetupProfile()
        fun shareBusinessCard()
        fun showProfileImageBottomSheet(showFullScreenImage: Boolean)
        fun showLocationDialog()
        fun goToCategoryScreen()
        fun openBusinessTypeBottomSheet()
    }
}
