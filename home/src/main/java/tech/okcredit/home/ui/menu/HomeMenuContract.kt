package tech.okcredit.home.ui.menu

import `in`.okcredit.dynamicview.data.model.Customization
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.home.models.KycMenuItem

interface HomeMenuContract {

    data class HomeMenuState(
        val business: Business? = null,
        val showNewLabel: Boolean = false,
        val showQuickAddCardItem: Boolean = false,
        val isFeedbackEnabled: Boolean = false,
        val customization: Customization? = null,
        val shouldShowCreditCardInfoForKyc: Boolean = false,
        val kycMenuItem: KycMenuItem = KycMenuItem.Unavailable,
        val canShowCreateBusiness: Boolean = false,
        val helpNumber: String = "",
        val showInventoryAndBilling: Boolean = false,
    ) : UiState {

        fun showCallCustomerCare() = helpNumber.isNotNullOrBlank()
    }

    sealed class HomeMenuPartialState : UiState.Partial<HomeMenuState> {

        object NoChange : HomeMenuPartialState()

        data class ProfileLoad(val business: Business) : HomeMenuPartialState()

        data class Customization(val target: `in`.okcredit.dynamicview.data.model.Customization?) :
            HomeMenuPartialState()

        data class UnclaimedRewardsCount(val count: Int) : HomeMenuPartialState()

        data class SetFeedbackEnabled(val isFeedbackEnabled: Boolean) : HomeMenuPartialState()

        data class SetShouldShowCreditCardInfoForKyc(val shouldShowCreditCardInfoForKyc: Boolean) :
            HomeMenuPartialState()

        data class SetKycMenuItem(val kycMenuItem: KycMenuItem) : HomeMenuPartialState()

        data class ShowCreateBusiness(val canShow: Boolean) : HomeMenuPartialState()

        data class ShowCallCustomerCare(val number: String) : HomeMenuPartialState()

        data class SetShowInventoryAndBilling(val shouldShow: Boolean) : HomeMenuPartialState()
    }

    sealed class HomeMenuIntent : UserIntent {
        data class SubmitFeedback(val feedback: String, val rating: Int) : HomeMenuIntent()

        object Load : HomeMenuIntent()

        object CollectionClicked : HomeMenuIntent()

        object ShowKycStatusDialog : HomeMenuIntent()

        object SettingsClicked : HomeMenuIntent()

        object CallHelp : HomeMenuIntent()
    }

    sealed class HomeMenuViewEvent : BaseViewEvent {

        object GoToCollectionScreen : HomeMenuViewEvent()

        object GoToSettingsScreen : HomeMenuViewEvent()

        object ShowKycStatusDialog : HomeMenuViewEvent()

        object CallHelp : HomeMenuViewEvent()
    }
}
