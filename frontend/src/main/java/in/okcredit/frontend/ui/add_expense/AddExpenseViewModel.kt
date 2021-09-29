package `in`.okcredit.frontend.ui.add_expense

import `in`.okcredit.backend.contract.Features
import `in`.okcredit.frontend.ui.add_expense.AddExpenseContract.*
import `in`.okcredit.frontend.usecase.GetExpenses
import `in`.okcredit.frontend.usecase.GetUserExpenseTypes
import `in`.okcredit.frontend.usecase.SubmitExpenseUseCase
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class AddExpenseViewModel @Inject constructor(
    initialState: State,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val getUserExpenseTypes: GetUserExpenseTypes,
    private val submitExpense: SubmitExpenseUseCase,
    private val ab: AbRepository,
    private val getExpenses: GetExpenses
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    private val reload: PublishSubject<Unit> = PublishSubject.create()
    private var merchantId = ""

    data class AddExpense(val expense: String, val expenseType: String, val expenseDate: DateTime)

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(

            // hide network error when network becomes available
            intent<Intent.Load>()
                .switchMap { checkNetworkHealth.get().execute(Unit) }
                .switchMap { wrap(getActiveBusinessId.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            // network connected
                            merchantId = it.value
                            reload.onNext(Unit)
                            PartialState.ClearNetworkError
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> PartialState.SetNetworkError(true)
                                else -> PartialState.NoChange
                            }
                        }
                    }
                },

            intent<Intent.Load>()
                .switchMap { getUserExpenseTypes.execute(GetUserExpenseTypes.Request("")) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.UserExpenseTypes(it.value.expenseTypes)
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> PartialState.SetNetworkError(true)
                                else -> PartialState.NoChange
                            }
                        }
                    }
                },

            intent<Intent.Load>()
                .switchMap { UseCase.wrapObservable(ab.isFeatureEnabled(Features.EXPENSE_HIDE_DATE_TAG)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.HideDateTag(it.value)
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> PartialState.SetNetworkError(true)
                                else -> PartialState.NoChange
                            }
                        }
                    }
                },
            reload.switchMap { getExpenses.execute(GetExpenses.Request(merchantId)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.SetFirstTransaction(it.value.expenseList.isNullOrEmpty())
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> PartialState.NoChange
                                else -> PartialState.NoChange
                            }
                        }
                    }
                },

            intent<Intent.SubmitExpense>()
                .switchMap {
                    submitExpense.execute(
                        SubmitExpenseUseCase.Request(
                            merchantId,
                            it.expense.expense,
                            it.expense.expenseType,
                            it.expense.expenseDate
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.ShowLoading
                        is Result.Success -> {
                            emitViewEvent(ViewEvent.GoBack)
                            PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> PartialState.SetNetworkError(true)
                                else -> PartialState.HideLoading
                            }
                        }
                    }
                },
            intent<Intent.ShowDatePickerDialog>()
                .map {
                    emitViewEvent(ViewEvent.ShowDatePickerDialog)
                    PartialState.NoChange
                },

            intent<Intent.OnChangeDate>()
                .map {
                    PartialState.ChangeDate(it.date)
                },

            intent<Intent.ShowSuggestions>()
                .map {
                    PartialState.ShowSuggestions(it.list)
                },

            intent<Intent.ShowSubmitCTA>()
                .map {
                    PartialState.ShowSubmitCTA(it.canShow)
                },
            intent<Intent.ShowHandEducationIntent>()
                .map {
                    PartialState.ShowHandEducation(it.canShow)
                }

        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is PartialState.HideLoading -> currentState.copy(isLoading = false)
            is PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false
            )
            is PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is PartialState.SetLoaderStatus -> currentState.copy(isLoading = partialState.status)
            is PartialState.NoChange -> currentState
            is PartialState.SetMerchantExists -> currentState.copy(
                merchantAlreadyExistsError = true
            )
            is PartialState.UserExpenseTypes -> currentState.copy(
                userExpenseTypes = partialState.expenseTypes,
                suggestions = partialState.expenseTypes
            )
            is PartialState.ChangeDate -> currentState.copy(date = partialState.value)
            is PartialState.HideDateTag -> currentState.copy(hideDateTag = partialState.hide)
            is PartialState.ShowSuggestions -> currentState.copy(suggestions = partialState.list)
            is PartialState.ShowSubmitCTA -> currentState.copy(showSubmitCTA = partialState.canShow)
            is PartialState.SetFirstTransaction -> currentState.copy(isFirstTransaction = partialState.isFirstTransaction)
            is PartialState.ShowHandEducation -> currentState.copy(canShowHandEducation = partialState.canShow)
        }
    }
}
