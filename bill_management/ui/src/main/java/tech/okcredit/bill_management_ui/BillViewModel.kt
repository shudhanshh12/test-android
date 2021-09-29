package tech.okcredit.bill_management_ui

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.BillGlobalInfo
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.bill_management_ui.BillContract.*
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import tech.okcredit.bills.GetAndUpdateBillSeenFirstTime
import tech.okcredit.sdk.analytics.BillTracker
import tech.okcredit.sdk.models.SelectedDateMode
import tech.okcredit.sdk.usecase.GetAndUpdateLastSeenTime
import tech.okcredit.sdk.usecase.GetBillsDateMapForAccount
import tech.okcredit.use_case.GetAccountsTotalBills
import tech.okcredit.use_case.ScheduleBillSync
import javax.inject.Inject

class BillViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam(BILL_INTENT_EXTRAS.ROLE) val role: String,
    @ViewModelParam(BILL_INTENT_EXTRAS.ACCOUNT_ID) val accountId: String,
    @ViewModelParam(BILL_INTENT_EXTRAS.ACCOUNT_NAME) val accountName: String?,
    private val updateLastSeenTime: Lazy<GetAndUpdateLastSeenTime>,
    private val getBillsDateMapForAccount: Lazy<GetBillsDateMapForAccount>,
    private val getAndUpdateBillSeenFirstTime: Lazy<GetAndUpdateBillSeenFirstTime>,
    private val tracker: Lazy<BillTracker>,
    private val totalBills: Lazy<GetAccountsTotalBills>,
    private val scheduleBillSync: Lazy<ScheduleBillSync>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    var seenSubject = PublishSubject.create<Unit>()
    var eventSent = false

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(

            intent<Intent.Load>()
                .map {
                    PartialState.SetAccountDetails(accountName, accountId)
                },
            intent<Intent.Load>()
                .switchMap { wrap(updateLastSeenTime.get().execute(accountId)) }
                .map {
                    when (it) {
                        is Result.Success -> PartialState.UpdateTimeSet(it.value)
                        is Result.Failure -> PartialState.NoChange
                        is Result.Progress -> PartialState.NoChange
                    }
                },
            intent<Intent.Load>()
                .switchMap { wrap(totalBills.get().execute(accountId)) }
                .map {
                    when (it) {
                        is Result.Success -> {
                            BillGlobalInfo.totalAccountBills = it.value.totalCount
                            BillGlobalInfo.unseenAccountBills = it.value.unseenCount
                            if (eventSent.not()) {
                                eventSent = true
                                tracker.get().trackPageViewed()
                            }
                            PartialState.NoChange
                        }
                        is Result.Progress -> PartialState.NoChange
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            intent<Intent.Load>()
                .switchMap { wrap(scheduleBillSync.get().execute()) }
                .map {
                    PartialState.NoChange
                },
            intent<Intent.Load>()
                .switchMap {
                    getBillsDateMapForAccount.get().execute(
                        GetBillsDateMapForAccount.Request(
                            accountId,
                            selectedMode = SelectedDateMode.OVERALL,
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            seenSubject.onNext(Unit)
                            if (it.value.map.isNotEmpty()) {
                                PartialState.SetBillsAndMonths(
                                    it.value.map,
                                    role,
                                    it.value.map.keys.toList(),
                                    it.value.currentMonth,
                                    it.value.lastMonth,
                                    it.value.lastToLastMonth,
                                    it.value.selectedMode
                                )
                            } else {
                                PartialState.AreBillsPresent(false)
                            }
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> PartialState.NoChange
                                else -> PartialState.NoChange
                            }
                        }
                    }
                },
            intent<Intent.Load>()
                .switchMap { getAndUpdateBillSeenFirstTime.get().execute() }
                .map { isFirstTime ->
                    if (isFirstTime) {
                        emitViewEvent(ViewEvent.ShowBottomSheetTutorial)
                    }
                    PartialState.NoChange
                },
            intent<Intent.OnDateChange>()
                .switchMap { selectedDate ->
                    getBillsDateMapForAccount.get().execute(
                        GetBillsDateMapForAccount.Request(
                            accountId,
                            startDate = selectedDate.selectedDate.startDate,
                            endDate = selectedDate.selectedDate.endDate,
                            selectedMode = selectedDate.selectedDate.selectedMode
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.SetBills(it.value.map, role, it.value.selectedMode)
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> PartialState.NoChange
                                else -> PartialState.NoChange
                            }
                        }
                    }
                }
        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            PartialState.NoChange -> currentState
            is PartialState.AreBillsPresent -> currentState.copy(areBillsPresent = partialState.areBillsPresent)
            is PartialState.UpdateTimeSet -> currentState.copy(
                lastSeenTime = partialState.lastSeenTime
            )
            is PartialState.SetAccountDetails -> currentState.copy(
                accountName = partialState.accountName,
                accountId = partialState.accountID
            )
            is PartialState.SetBills -> currentState.copy(
                map = partialState.map,
                role = partialState.role,
                selectedMode = partialState.selectedMode
            )
            is PartialState.SetBillsAndMonths -> currentState.copy(
                map = partialState.map,
                role = partialState.role,
                monthsList = partialState.monthsList,
                areBillsPresent = true,
                current = partialState.current,
                last = partialState.last,
                lastToLast = partialState.lastToLast,
                selectedMode = partialState.selectedMode
            )
        }
    }
}
