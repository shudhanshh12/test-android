package `in`.okcredit.frontend.ui.migrate_to_supplier

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface MoveToSupplierContract {
    data class State(
        val isLoading: Boolean = false,
        val error: Boolean = false,
        val networkError: Boolean = false,
        val customer: Customer? = null,
        val canShowLoaderScreen: Boolean = false,
        val canShowFailureScreen: Boolean = false,
        val canShowSuccessfulScreen: Boolean = false,
        val progressValue: Int = 0,
        val migratedAccountId: String? = null,
        val canShowAccountMigrationError: Boolean = false,
        val errorMessage: String? = "",
        val commonLedger: Boolean = false,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object ErrorState : PartialState()

        object NoChange : PartialState()

        data class SetCustomer(val customer: Customer?) : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        data class SetSupplierCreditEnabledStatus(val commonLedger: Boolean) : PartialState()

        data class SetMigrationStates(
            val canShowLoaderScreen: Boolean,
            val canShowSuccessfulScreen: Boolean,
            val canShowFailureScreen: Boolean,
            val migragtedAccountId: String?,
            val errorMessage: String?,
        ) : PartialState()

        data class SetLoaderProgess(val progressValue: Int) : PartialState()

        data class AccountMigrationPermittedError(val canShowAccountMigrationError: Boolean, val errorMessage: String) :
            PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        object ShowConfirmDialog : Intent()

        object Migrate : Intent()
    }

    interface Navigator {

        fun gotoLogin()

        fun gotoHomeScreen()

        fun showConfirmDialog()
    }
}
