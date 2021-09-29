package `in`.okcredit.frontend.ui.expense_manager

import `in`.okcredit.backend._offline.usecase.SubmitFeedbackImpl
import `in`.okcredit.backend.contract.Features
import `in`.okcredit.backend.contract.RxSharedPrefValues
import `in`.okcredit.expense.models.Models
import `in`.okcredit.frontend.usecase.DeleteExpense
import `in`.okcredit.frontend.usecase.ExpenseOnBoarding
import `in`.okcredit.frontend.usecase.FilterExpense
import `in`.okcredit.frontend.usecase.GetExpenses
import `in`.okcredit.frontend.utils.Utils
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
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ExpenseManagerViewModel @Inject constructor(
    initialState: ExpenseManagerContract.State,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val navigator: ExpenseManagerContract.Navigator,
    private val getExpenses: GetExpenses,
    private val deleteExpense: DeleteExpense,
    private val ab: AbRepository,
    private val rxPreference: DefaultPreferences,
    private val filterExpense: FilterExpense,
    private val expenseOnBoarding: ExpenseOnBoarding,
    private val submitFeedback: SubmitFeedbackImpl,
) : BaseViewModel<ExpenseManagerContract.State, ExpenseManagerContract.PartialState, ExpenseManagerContract.ViewEvent>(
    initialState
) {

    private val reload: PublishSubject<Unit> = PublishSubject.create()
    private val internetPinger: PublishSubject<Boolean> = PublishSubject.create()
    private var merchantId = ""
    private var addExpenseEducation = false
    private var allExpenses: List<Models.Expense> = listOf()

    override fun handle(): Observable<UiState.Partial<ExpenseManagerContract.State>> {
        return mergeArray(

            // hide network error when network becomes available
            intent<ExpenseManagerContract.Intent.Load>()
                .switchMap { checkNetworkHealth.get().execute(Unit) }
                .switchMap { wrap(getActiveBusinessId.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> ExpenseManagerContract.PartialState.ShowLoading
                        is Result.Success -> {
                            // network connected
                            merchantId = it.value
                            reload.onNext(Unit)
                            ExpenseManagerContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    ExpenseManagerContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> ExpenseManagerContract.PartialState.SetNetworkError(true)
                                else -> ExpenseManagerContract.PartialState.NoChange
                            }
                        }
                    }
                },
            intent<ExpenseManagerContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(ab.isFeatureEnabled(Features.EXPENSE_SHOW_ADD_EXPENSE)) }
                .map {
                    when (it) {
                        is Result.Progress -> ExpenseManagerContract.PartialState.NoChange
                        is Result.Success -> {
                            ExpenseManagerContract.PartialState.showAddExpense(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    ExpenseManagerContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> ExpenseManagerContract.PartialState.NoChange
                                else -> ExpenseManagerContract.PartialState.NoChange
                            }
                        }
                    }
                },
            intent<ExpenseManagerContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(ab.isFeatureEnabled(Features.EXPENSE_SUMMARY_VIEW_AB)) }
                .map {
                    when (it) {
                        is Result.Progress -> ExpenseManagerContract.PartialState.NoChange
                        is Result.Success -> {
                            ExpenseManagerContract.PartialState.showSummaryViewAb(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    ExpenseManagerContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> ExpenseManagerContract.PartialState.NoChange
                                else -> ExpenseManagerContract.PartialState.NoChange
                            }
                        }
                    }
                },
            intent<ExpenseManagerContract.Intent.Load>()
                .switchMap { expenseOnBoarding.execute(Unit) }
                .map {
                    when (it) {
                        is Result.Progress -> ExpenseManagerContract.PartialState.NoChange
                        is Result.Success -> {
                            ExpenseManagerContract.PartialState.SetOnBoardingVariant(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    ExpenseManagerContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> ExpenseManagerContract.PartialState.NoChange
                                else -> ExpenseManagerContract.PartialState.NoChange
                            }
                        }
                    }
                },
            intent<ExpenseManagerContract.Intent.GetAllExpenses>()
                .switchMap { getExpenses.execute(GetExpenses.Request(merchantId)) }
                .map {
                    when (it) {
                        is Result.Progress -> ExpenseManagerContract.PartialState.ShowLoading
                        is Result.Success -> {
                            ExpenseManagerContract.PartialState.SetAllExpenses(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    ExpenseManagerContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    internetPinger.onNext(true)
                                    ExpenseManagerContract.PartialState.SetNetworkError(true)
                                }
                                else -> ExpenseManagerContract.PartialState.NoChange
                            }
                        }
                    }
                },
            intent<ExpenseManagerContract.Intent.GetExpenses>()
                .delay(200, TimeUnit.MILLISECONDS)
                .switchMap {
                    filterExpense.execute(FilterExpense.Request(allExpenses, it.startDate, it.endDate))
                }
                .map {
                    when (it) {
                        is Result.Progress -> ExpenseManagerContract.PartialState.ShowLoading
                        is Result.Success -> {
                            ExpenseManagerContract.PartialState.SetExpenses(it.value)
                        }
                        is Result.Failure -> {
                            ExpenseManagerContract.PartialState.NoChange
                        }
                    }
                },
            intent<ExpenseManagerContract.Intent.DeleteExpense>()
                .switchMap { deleteExpense.execute(it.id) }
                .map {
                    when (it) {
                        is Result.Progress -> ExpenseManagerContract.PartialState.ShowLoading
                        is Result.Success -> {
                            reload.onNext(Unit)
                            ExpenseManagerContract.PartialState.ShowDeleteConfirmDialog(false)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    ExpenseManagerContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> ExpenseManagerContract.PartialState.SetNetworkError(true)
                                else -> ExpenseManagerContract.PartialState.NoChange
                            }
                        }
                    }
                },
            reload.switchMap { getExpenses.execute(GetExpenses.Request(merchantId)) }
                .map {
                    when (it) {
                        is Result.Progress -> ExpenseManagerContract.PartialState.ShowLoading
                        is Result.Success -> {
                            allExpenses = it.value.expenseList
                            navigator.reLoad()
                            val isNewUser = it.value.expenseList.isNullOrEmpty()
                            navigator.trackEventOnLoad(isNewUser, isNewUser || it.value.expenseList.size == 1)
                            ExpenseManagerContract.PartialState.SetNewUser(it.value.expenseList.isNullOrEmpty())
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    ExpenseManagerContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    ExpenseManagerContract.PartialState.SetNetworkError(true)
                                }
                                else -> ExpenseManagerContract.PartialState.NoChange
                            }
                        }
                    }
                },
            intent<ExpenseManagerContract.Intent.ChangeFilter>()
                .map {
                    when (it.filter) {
                        ExpenseManagerContract.Filter.ALL -> navigator.showAll()
                        ExpenseManagerContract.Filter.THIS_MONTH -> navigator.showThisMonth()
                        ExpenseManagerContract.Filter.TODAY -> navigator.showToday()
                        ExpenseManagerContract.Filter.LAST_MONTH -> navigator.showLastMonth()
                        else -> ExpenseManagerContract.PartialState.NoChange
                    }
                    ExpenseManagerContract.PartialState.ChangeFilter(it.filter)
                },
            intent<ExpenseManagerContract.Intent.SetDateRangeIntent>()
                .map {
                    navigator.showForSelectedRange(it.startDate, it.endDate)
                    ExpenseManagerContract.PartialState.ChangeFilter(ExpenseManagerContract.Filter.DATE_RANGE)
                },
            intent<ExpenseManagerContract.Intent.Load>()
                .switchMap {
                    UseCase.wrapSingle(
                        rxPreference.getBoolean(RxSharedPrefValues.SHOULD_SHOW_FIRST_ADD_EXPENSE_EDUCATION, Scope.Individual)
                            .asObservable().firstOrError()
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> ExpenseManagerContract.PartialState.NoChange
                        is Result.Success -> {
                            addExpenseEducation = it.value
                            ExpenseManagerContract.PartialState.SetFirstAddExpenseEducation(addExpenseEducation)
                        }
                        is Result.Failure -> ExpenseManagerContract.PartialState.NoChange
                    }
                },
            intent<ExpenseManagerContract.Intent.Load>()
                .switchMap {
                    UseCase.wrapSingle(
                        rxPreference.getString(ExpenseManagerContract.DEFAULT_EXPENSE_FILTER, Scope.Individual)
                            .asObservable().firstOrError()
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> ExpenseManagerContract.PartialState.NoChange
                        is Result.Success -> {
                            val filter = when (it.value) {
                                ExpenseManagerContract.Filter.ALL.name -> ExpenseManagerContract.Filter.ALL
                                ExpenseManagerContract.Filter.TODAY.name -> ExpenseManagerContract.Filter.TODAY
                                ExpenseManagerContract.Filter.THIS_MONTH.name -> ExpenseManagerContract.Filter.THIS_MONTH
                                ExpenseManagerContract.Filter.LAST_MONTH.name -> ExpenseManagerContract.Filter.LAST_MONTH
                                else -> ExpenseManagerContract.Filter.ALL
                            }
                            ExpenseManagerContract.PartialState.ChangeFilter(filter)
                        }
                        is Result.Failure -> ExpenseManagerContract.PartialState.NoChange
                    }
                },
            intent<ExpenseManagerContract.Intent.RxPreferenceBoolean>()
                .switchMap { wrap(rxCompletable { rxPreference.set(it.key, it.value, it.scope) }) }
                .map {
                    ExpenseManagerContract.PartialState.NoChange
                },
            intent<ExpenseManagerContract.Intent.RxPreferenceString>()
                .switchMap { wrap(rxCompletable { rxPreference.set(it.key, it.value, it.scope) }) }
                .map {
                    ExpenseManagerContract.PartialState.NoChange
                },
            intent<ExpenseManagerContract.Intent.SetFirstAddExpenseEducation>()
                .map {
                    ExpenseManagerContract.PartialState.SetFirstAddExpenseEducation(addExpenseEducation && it.canShow)
                },
            intent<ExpenseManagerContract.Intent.Retry>()
                .map {
                    reload.onNext(Unit)
                    ExpenseManagerContract.PartialState.NoChange
                },
            intent<ExpenseManagerContract.Intent.OnAddExpenseClicked>()
                .map {
                    navigator.goToAddExpenseScreen()
                    ExpenseManagerContract.PartialState.NoChange
                },
            intent<ExpenseManagerContract.Intent.ShowDeleteLayout>()
                .map {
                    ExpenseManagerContract.PartialState.ShowDeleteLayout(it.expense)
                },
            intent<ExpenseManagerContract.Intent.HideDeleteLayout>()
                .map {
                    ExpenseManagerContract.PartialState.HideDeleteLayout
                },
            intent<ExpenseManagerContract.Intent.ShowDeleteConfirmDialog>()
                .map {
                    ExpenseManagerContract.PartialState.ShowDeleteConfirmDialog(it.canShow)
                },
            intent<ExpenseManagerContract.Intent.SubmitFeedBack>()
                .switchMap {
                    UseCase.wrapCompletable(
                        submitFeedback.schedule(
                            it.msg,
                            7
                        )
                    ) // For the the expense feedback please maintain the rating as 7
                }.map {
                    ExpenseManagerContract.PartialState.NoChange
                },
            internetPinger.flatMap {
                Utils.isInternetAvailable().doOnComplete {
                }.repeatWhen {
                    return@repeatWhen it.delay(1, TimeUnit.SECONDS)
                }.takeUntil {
                    return@takeUntil it
                }.filter {
                    return@filter it
                }.map<ExpenseManagerContract.PartialState> {
                    navigator.reLoad()
                    ExpenseManagerContract.PartialState.SetNetworkError(
                        false
                    )
                }.startWith(ExpenseManagerContract.PartialState.SetNetworkError(true))
            }
        )
    }

    override fun reduce(
        currentState: ExpenseManagerContract.State,
        partialState: ExpenseManagerContract.PartialState,
    ): ExpenseManagerContract.State {
        return when (partialState) {
            is ExpenseManagerContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is ExpenseManagerContract.PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is ExpenseManagerContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is ExpenseManagerContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is ExpenseManagerContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false
            )
            is ExpenseManagerContract.PartialState.ClearNetworkError -> currentState.copy(
                networkError = false,
                isLoading = false
            )
            is ExpenseManagerContract.PartialState.SetLoaderStatus -> currentState.copy(isLoading = partialState.status)
            is ExpenseManagerContract.PartialState.NoChange -> currentState
            is ExpenseManagerContract.PartialState.SetAllExpenses -> currentState.copy(
                list = partialState.expenseResponse.expenseList,
                startDate = partialState.expenseResponse.startDate,
                endDate = partialState.expenseResponse.endDate,
                totalAmount = partialState.expenseResponse.totalAmount,
                isLoading = false,
                networkError = false
            )
            is ExpenseManagerContract.PartialState.showAddExpense -> currentState.copy(showAddexpense = partialState.canShow)
            is ExpenseManagerContract.PartialState.ChangeFilter -> currentState.copy(filter = partialState.filter)
            is ExpenseManagerContract.PartialState.showSummaryViewAb -> currentState.copy(isSummaryViewAbEnabled = partialState.showSummaryViewAb)
            is ExpenseManagerContract.PartialState.SetNewUser -> currentState.copy(isNewUser = partialState.isNewUser)
            is ExpenseManagerContract.PartialState.SetExpenses -> currentState.copy(
                list = partialState.expenseResponse.expenseList,
                totalAmount = partialState.expenseResponse.totalAmount,
                startDate = partialState.expenseResponse.startDate,
                endDate = partialState.expenseResponse.endDate,
                isLoading = false,
                networkError = false
            )
            is ExpenseManagerContract.PartialState.SetFirstAddExpenseEducation -> currentState.copy(
                canShowAddExpenseEducation = partialState.canShow
            )
            is ExpenseManagerContract.PartialState.ShowDeleteLayout -> currentState.copy(
                selectedExpense = partialState.expense,
                canShowDeleteLayout = true
            )
            ExpenseManagerContract.PartialState.HideDeleteLayout -> currentState.copy(canShowDeleteLayout = false)
            is ExpenseManagerContract.PartialState.ShowDeleteConfirmDialog -> currentState.copy(
                canShowDeleteConfirmDialog = partialState.canShow,
                canShowDeleteLayout = false
            )
            is ExpenseManagerContract.PartialState.SetOnBoardingVariant -> currentState.copy(onBoardingVariant = partialState.onBoardingVariant)
        }
    }
}
