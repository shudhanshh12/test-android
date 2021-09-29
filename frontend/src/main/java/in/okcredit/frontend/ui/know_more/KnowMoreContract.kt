package `in`.okcredit.frontend.ui.know_more

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface KnowMoreContract {
    object Constants {
        const val Supplier = "supplier"
        const val Customer = "customer"
    }

    data class State(
        val isLoading: Boolean = false,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val merchantName: String = "",
        val customerName: String = "",
        val networkError: Boolean = false,
        val business: Business? = null,
        val merchantPic: String? = null,
        val customerPic: String? = null,
        val raiseConcernState: RasieConcernState = RasieConcernState.InActive,
        val accountType: String? = null,
        val accountID: String? = null,
        val commonLedgerSellerVideo: String? = null,
        val commonLedgerBuyerVideo: String? = null
    ) : UiState

    sealed class RasieConcernState {
        object Active : RasieConcernState()
        object InActive : RasieConcernState()
    }

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        data class SetLoaderStatus(val status: Boolean) : PartialState()

        object ClearNetworkError : PartialState()

        data class SetAccountTypeAndID(val id: String, val accountType: String) : PartialState()

        data class SetBusiness(val name: String, val profileImage: String?) : PartialState()

        data class SetCustomer(val name: String, val profileImage: String?) : PartialState()

        data class SetVideos(val commonLedgerBuyerVideo: String?, val commonLedgerSellerVideo: String?) : PartialState()
    }

    sealed class Intent : UserIntent {

        object Load : Intent()

        data class SubmitFeedback(val feedback: String, val rating: Int) : Intent()
    }

    interface Navigator {

        fun goBackAfterAnimation()

        fun gotoLogin()
    }
}
