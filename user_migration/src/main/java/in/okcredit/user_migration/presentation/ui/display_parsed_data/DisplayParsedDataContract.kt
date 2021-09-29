package `in`.okcredit.user_migration.presentation.ui.display_parsed_data

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.models.CustomerUiTemplate
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.models.ParsedDataModels
import tech.okcredit.user_migration.contract.models.create_customer_transaction.Customers
import java.io.File

interface DisplayParsedDataContract {
    data class State(
        val fileParserLoading: Boolean = true,
        val createCustomerLoading: Boolean = false,
        val networkError: Boolean = false,
        val parserError: Boolean = false,
        val models: List<ParsedDataModels> = emptyList(),
        val totalCustomerCount: Int = 0
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        object ShowFileParserProgress : PartialState()
        object HandleSubmitClicks : PartialState()
        object ShowProgress : PartialState()
        object NetworkError : PartialState()
        object ParserError : PartialState()
        data class ShowCreateCustomerProgress(val show: Boolean) : PartialState()
        data class UpdateModels(val models: List<ParsedDataModels>) : PartialState()
        data class SetParsedData(val models: List<ParsedDataModels>, val customerCount: Int) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class Submit(val parsedDataModels: List<ParsedDataModels>) : Intent()
        data class CreateCustomer(val customerList: List<Customers>) :
            Intent()

        data class OpenPdfFile(val fileName: String) : Intent()
        data class EditDetails(val customer: CustomerUiTemplate) : Intent()
        data class UpdateCustomer(val customer: CustomerUiTemplate) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class ShowError(val error: Int) : ViewEvent()
        data class OpenPdfFile(val file: File) : ViewEvent()
        object GoToHome : ViewEvent()
        data class GoToEditDetailsDialog(val customer: CustomerUiTemplate) : ViewEvent()
    }
}
