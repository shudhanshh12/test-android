package `in`.okcredit.merchant.customer_ui.ui.staff_link.add

import `in`.okcredit.backend.contract.GetSpecificCustomerList
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.ui.staff_link.StaffLinkEventsTracker
import `in`.okcredit.merchant.customer_ui.ui.staff_link.add.StaffLinkAddCustomerContract.*
import `in`.okcredit.merchant.customer_ui.usecase.CreateStaffCollectionLink
import `in`.okcredit.merchant.customer_ui.usecase.EditStaffCollectionLink
import `in`.okcredit.merchant.customer_ui.usecase.GetCustomerWithPaymentDue
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import android.annotation.SuppressLint
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.android.base.extensions.ifLet
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
class StaffLinkAddCustomerViewModel @Inject constructor(
    @ViewModelParam("selected_customers") val preSelectedCustomerIds: Set<String>?,
    initialState: State,
    private val staffLinkAddCustomerReducer: Lazy<StaffLinkAddCustomerReducer>,
    private val getSpecificCustomerList: Lazy<GetSpecificCustomerList>,
    private val getCustomerWithPaymentDue: Lazy<GetCustomerWithPaymentDue>,
    private val createStaffCollectionLink: Lazy<CreateStaffCollectionLink>,
    private val editStaffCollectionLink: Lazy<EditStaffCollectionLink>,
    private val staffLinkEventsTracker: Lazy<StaffLinkEventsTracker>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            loadCustomerWithPaymentDue(),
            searchCustomers(),
            searchCustomerClicked(),
            dismissSearchCustomerClicked(),
            selectAllCustomers(),
            deselectAllCustomers(),
            customerTapped(),
            shareClicked(),
            addDetailsClicked(),
            loadCustomersFromIds(),
            goBackClicked(),
        )
    }

    private fun loadCustomerWithPaymentDue() = intent<Intent.Load>().map {
        pushIntent(Intent.SearchCustomer(""))
        PartialState.NoChange
    }

    private fun goBackClicked() = intent<Intent.GoBack>().map {
        staffLinkEventsTracker.get().tracCollectionListGoBack(
            linkId = getCurrentState().linkId,
            screen = if (getCurrentState().showEditableSearch) StaffLinkEventsTracker.Screen.SEARCH else StaffLinkEventsTracker.Screen.HOME
        )
        PartialState.NoChange
    }

    private fun loadCustomersFromIds() = intent<Intent.GetCustomersFromIds>()
        .switchMap { wrap(getSpecificCustomerList.get().execute(it.customerIds).firstOrError()) }
        .map {
            when (it) {
                is Result.Success -> {
                    PartialState.SetPreSelectedCustomer(it.value)
                }
                is Result.Failure -> {
                    if (isInternetIssue(it.error)) {
                        emitViewEvent(ViewEvent.ShowError(R.string.err_network))
                    } else {
                        emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                    }
                    PartialState.SetLoading(false)
                }
                else -> {
                    PartialState.SetLoading(true)
                }
            }
        }

    private fun searchCustomerClicked() = intent<Intent.SearchClicked>()
        .map {
            staffLinkEventsTracker.get().tracCollectionListSearch(
                linkId = getCurrentState().linkId,
                customerCount = getCurrentState().originalCustomerList.size,
                totalDue = getCurrentState().totalDue
            )
            PartialState.SearchClicked
        }

    private fun dismissSearchCustomerClicked() = intent<Intent.DismissSearch>()
        .map { PartialState.DismissSearch }

    private fun selectAllCustomers() = intent<Intent.SelectAllCustomers>()
        .map {
            staffLinkEventsTracker.get().tracCollectionListSelection(
                linkId = getCurrentState().linkId,
                screen = if (getCurrentState().showEditableSearch) StaffLinkEventsTracker.Screen.SEARCH else StaffLinkEventsTracker.Screen.HOME,
                customerCount = getCurrentState().originalCustomerList.size,
                totalDue = getCurrentState().totalDue,
                type = StaffLinkEventsTracker.Type.ALL,
                accountId = "",
                action = StaffLinkEventsTracker.Action.SELECT
            )
            val selectedCustomers = getCurrentState().filteredCustomerList.map { it.id }.toSet()
            PartialState.SelectCustomers(selectedCustomers)
        }

    private fun deselectAllCustomers() = intent<Intent.DeselectAllCustomers>()
        .map {
            staffLinkEventsTracker.get().tracCollectionListSelection(
                linkId = getCurrentState().linkId,
                screen = if (getCurrentState().showEditableSearch) StaffLinkEventsTracker.Screen.SEARCH else StaffLinkEventsTracker.Screen.HOME,
                customerCount = getCurrentState().originalCustomerList.size,
                totalDue = getCurrentState().totalDue,
                type = StaffLinkEventsTracker.Type.ALL,
                accountId = "",
                action = StaffLinkEventsTracker.Action.DESELECT
            )
            PartialState.SelectCustomers(emptySet())
        }

    private fun createOrEditStaffLink(): Observable<Result<CreateStaffCollectionLink.StaffLinkSummary>> {
        // check if linkId and link are already present in the state
        ifLet(getCurrentState().linkId, getCurrentState().link) { linkId, link ->
            // check if there is any change in the current and pre-selected customers
            if (getCurrentState().selectedCustomerIds == preSelectedCustomerIds) {
                return wrap(
                    Single.just(
                        CreateStaffCollectionLink.StaffLinkSummary(
                            linkId = linkId,
                            link = link,
                            customerIds = getCurrentState().selectedCustomerIds.toList(),
                            linkCreateTime = getCurrentState().linkCreateTime,
                        )
                    )
                )
            }
            // edit staff link details if there was a change in customer list
            return wrap {
                editStaffCollectionLink.get().execute(
                    linkId = linkId,
                    currentCustomerIds = preSelectedCustomerIds ?: emptySet(),
                    newCustomerIds = getCurrentState().selectedCustomerIds,
                )
                CreateStaffCollectionLink.StaffLinkSummary(
                    linkId = linkId,
                    link = link,
                    customerIds = getCurrentState().selectedCustomerIds.toList(),
                    linkCreateTime = getCurrentState().linkCreateTime,
                )
            }
        }
        // create a new link with selected customers
        return wrap {
            createStaffCollectionLink.get().execute(getCurrentState().selectedCustomerIds.toList())
        }
    }

    private fun addDetailsClicked() = intent<Intent.AddDetailsClicked>()
        .switchMap {
            createOrEditStaffLink()
        }
        .map {
            when (it) {
                is Result.Success -> {
                    staffLinkEventsTracker.get().tracCollectionListAddDetails(
                        linkId = it.value.linkId,
                        customerCount = it.value.customerIds.size,
                        totalDue = getCurrentState().totalDue,
                    )
                    emitViewEvent(ViewEvent.MoveToAddDetails(it.value))
                    PartialState.NoChange
                }
                is Result.Failure -> {
                    when {
                        isInternetIssue(it.error) -> {
                            emitViewEvent(ViewEvent.ShowError(R.string.err_network))
                        }
                        it.error is CreateStaffCollectionLink.CollectionNotActivatedError -> {
                            emitViewEvent(ViewEvent.ShowSetUpCollection)
                        }
                        else -> {
                            emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                        }
                    }
                    PartialState.NoChange
                }
                else -> {
                    PartialState.SetLoading(true)
                }
            }
        }

    private fun customerTapped() = intent<Intent.CustomerTapped>()
        .map {
            val selected = getCurrentState().selectedCustomerIds.contains(it.id)
            val selectedCustomerSet = mutableSetOf<String>()
            selectedCustomerSet.addAll(getCurrentState().selectedCustomerIds)
            staffLinkEventsTracker.get().tracCollectionListSelection(
                linkId = getCurrentState().linkId,
                screen = if (getCurrentState().showEditableSearch) StaffLinkEventsTracker.Screen.SEARCH else StaffLinkEventsTracker.Screen.HOME,
                customerCount = getCurrentState().originalCustomerList.size,
                totalDue = getCurrentState().totalDue,
                type = StaffLinkEventsTracker.Type.INDIVIDUAL,
                accountId = it.id,
                action = if (selected) StaffLinkEventsTracker.Action.DESELECT else StaffLinkEventsTracker.Action.SELECT
            )
            if (selected) {
                selectedCustomerSet.remove(it.id)
                PartialState.SelectCustomers(selectedCustomerSet)
            } else {
                selectedCustomerSet.add(it.id)
                PartialState.SelectCustomers(selectedCustomerSet)
            }
        }

    private fun shareClicked() = intent<Intent.ShareClicked>()
        .switchMap {
            createOrEditStaffLink()
        }
        .map {
            when (it) {
                is Result.Success -> {
                    staffLinkEventsTracker.get().tracCollectionListShareList(
                        linkId = it.value.linkId,
                        customerCount = it.value.customerIds.size,
                        totalDue = getCurrentState().totalDue,
                        screen = StaffLinkEventsTracker.Screen.HOME,
                    )
                    emitViewEvent(ViewEvent.ShareOnWhatsApp(it.value))
                    PartialState.NoChange
                }
                is Result.Failure -> {
                    when {
                        isInternetIssue(it.error) -> {
                            emitViewEvent(ViewEvent.ShowError(R.string.err_network))
                        }
                        it.error is CreateStaffCollectionLink.CollectionNotActivatedError -> {
                            emitViewEvent(ViewEvent.ShowSetUpCollection)
                        }
                        else -> {
                            emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                        }
                    }
                    PartialState.NoChange
                }
                else -> {
                    PartialState.SetLoading(true)
                }
            }
        }

    private fun searchCustomers() = intent<Intent.SearchCustomer>()
        .switchMap {
            if (getCurrentState().searchQuery.isEmpty() && it.query.trim().isNotEmpty()) {
                staffLinkEventsTracker.get().tracCollectionListSearchStart(
                    linkId = getCurrentState().linkId,
                    customerCount = getCurrentState().originalCustomerList.size,
                    totalDue = getCurrentState().totalDue,
                )
            }
            wrap(getCustomerWithPaymentDue.get().execute(it.query.trim()))
        }
        .map {
            when (it) {
                is Result.Success -> {
                    PartialState.SetCustomerList(it.value)
                }
                is Result.Failure -> {
                    if (isInternetIssue(it.error)) {
                        emitViewEvent(ViewEvent.ShowError(R.string.err_network))
                    } else {
                        emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                    }
                    PartialState.NoChange
                }
                else -> {
                    PartialState.NoChange
                }
            }
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return staffLinkAddCustomerReducer.get().reduce(currentState, partialState)
    }
}
