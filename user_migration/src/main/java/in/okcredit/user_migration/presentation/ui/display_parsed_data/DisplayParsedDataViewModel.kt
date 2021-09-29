package `in`.okcredit.user_migration.presentation.ui.display_parsed_data

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.user_migration.R
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.DisplayParsedDataContract.*
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.models.ParsedDataModels
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.usecase.CreateCustomerAndTransaction
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.usecase.GetControllerDataModels
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.usecase.GetPdfFilePath
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.usecase.UpdateParsedDataModels
import dagger.Lazy
import io.reactivex.Observable
import org.joda.time.DateTime
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.user_migration.contract.models.create_customer_transaction.Customers
import tech.okcredit.user_migration.contract.models.create_customer_transaction.Transactions
import javax.inject.Inject

class DisplayParsedDataViewModel @Inject constructor(
    initialState: State,
    private val getPdfFilePath: Lazy<GetPdfFilePath>,
    private val createCustomerAndTransaction: Lazy<CreateCustomerAndTransaction>,
    private val displayParsedDataScreenArgs: Lazy<DisplayParsedDataFragmentArgs>,
    private val getFieldedDataModels: Lazy<GetControllerDataModels>,
    private val getUpdatedParsedDataModels: Lazy<UpdateParsedDataModels>
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    private var renderControllerModels = mutableListOf<ParsedDataModels>()

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            loadParsedFileCustomers(),
            openPdfFile(),
            handleSubmitButtonClick(),
            updateControllerModels(),
            goToEditDetailsDialog(),
            createCustomer()
        )
    }

    private fun loadParsedFileCustomers(): Observable<UiState.Partial<State>> {
        return intent<Intent.Load>()
            .switchMap {
                getFieldedDataModels.get().execute(
                    remoteUrls = displayParsedDataScreenArgs.get().listOfFiles.list,
                    localUrls = displayParsedDataScreenArgs.get().listOfFileNames.list
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.ShowFileParserProgress
                    is Result.Success -> {
                        renderControllerModels.clear()
                        renderControllerModels.addAll(it.value.models)
                        PartialState.SetParsedData(it.value.models, it.value.totalCustomerCount)
                    }
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> emitViewEvent(
                                ViewEvent.ShowError(R.string.please_check_network_setting)
                            )
                            else -> emitViewEvent(
                                ViewEvent.ShowError(
                                    R.string.err_default
                                )
                            )
                        }
                        PartialState.NoChange
                    }
                }
            }
    }

    private fun openPdfFile(): Observable<UiState.Partial<State>> {
        return intent<Intent.OpenPdfFile>()
            .switchMap {
                wrap(getPdfFilePath.get().execute(it.fileName))
            }.map {
                when (it) {
                    is Result.Success -> {
                        if (it.value.found) {
                            emitViewEvent(ViewEvent.OpenPdfFile(it.value.file!!))
                        } else {
                            emitViewEvent(ViewEvent.ShowError(R.string.pdf_not_found))
                        }
                        PartialState.NoChange
                    }
                    else -> PartialState.NoChange
                }
            }
    }

    private fun updateControllerModels(): Observable<UiState.Partial<State>> {
        return intent<Intent.UpdateCustomer>()
            .switchMap {
                wrap(
                    getUpdatedParsedDataModels.get()
                        .execute(updateEntries = it.customer, oldParsedDataModels = renderControllerModels)
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.ShowProgress
                    is Result.Success -> {
                        renderControllerModels.clear()
                        renderControllerModels.addAll(it.value)
                        PartialState.UpdateModels(it.value)
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun getListOfCustomer(allModels: List<ParsedDataModels>): List<Customers> {
        return allModels.mapNotNull { it as? ParsedDataModels.CustomerModel }
            .filter { it.customer.isCheckedBoxChecked }
            .map {
                Customers(
                    name = it.customer.name,
                    mobile = it.customer.phone,
                    transactions = listOf(
                        Transactions(
                            type = it.customer.type,
                            amount = it.customer.amount,
                            billDate = it.customer.dueDate ?: DateTime(DateTimeUtils.currentDateTime())
                        )
                    )
                )
            }
    }

    private fun handleSubmitButtonClick() = intent<Intent.Submit>()
        .map {
            PartialState.HandleSubmitClicks
        }

    private fun createCustomer(): Observable<UiState.Partial<State>> {
        return intent<Intent.CreateCustomer>()
            .switchMap {
                createCustomerAndTransaction.get().execute(it.customerList)
            }.map {
                when (it) {
                    is Result.Progress -> PartialState.ShowCreateCustomerProgress(true)
                    is Result.Success -> {
                        emitViewEvent(ViewEvent.GoToHome)
                        PartialState.NoChange
                    }
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> {
                                emitViewEvent(ViewEvent.ShowError(R.string.please_check_network_setting))
                                PartialState.ShowCreateCustomerProgress(false)
                            }
                            else -> {
                                emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                                PartialState.ShowCreateCustomerProgress(false)
                            }
                        }
                    }
                }
            }
    }

    private fun goToEditDetailsDialog(): Observable<UiState.Partial<State>>? {
        return intent<Intent.EditDetails>().map {
            emitViewEvent(ViewEvent.GoToEditDetailsDialog(it.customer))
            PartialState.NoChange
        }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is PartialState.NetworkError -> currentState.copy(
                networkError = true,
                parserError = false,
                fileParserLoading = false
            )
            is PartialState.ParserError -> currentState.copy(
                networkError = false,
                parserError = true,
                fileParserLoading = false
            )
            is PartialState.ShowFileParserProgress -> currentState.copy(
                networkError = false,
                parserError = false,
                fileParserLoading = true,
                createCustomerLoading = false
            )
            is PartialState.ShowCreateCustomerProgress -> currentState.copy(
                networkError = false,
                parserError = false,
                fileParserLoading = false,
                createCustomerLoading = partialState.show
            )
            is PartialState.SetParsedData -> currentState.copy(
                fileParserLoading = false,
                networkError = false,
                parserError = false,
                models = renderControllerModels,
                totalCustomerCount = partialState.customerCount
            )
            is PartialState.UpdateModels -> currentState.copy(
                createCustomerLoading = false,
                models = renderControllerModels
            )
            is PartialState.HandleSubmitClicks -> {
                val customerList = getListOfCustomer(currentState.models)
                if (customerList.isNullOrEmpty()) {
                    emitViewEvent(ViewEvent.ShowError(R.string.please_select_atleast_one_customer))
                } else {
                    pushIntent(Intent.CreateCustomer(customerList))
                }
                currentState
            }
            is PartialState.ShowProgress -> currentState.copy(
                createCustomerLoading = true
            )
            is PartialState.NoChange -> currentState
        }
    }
}
