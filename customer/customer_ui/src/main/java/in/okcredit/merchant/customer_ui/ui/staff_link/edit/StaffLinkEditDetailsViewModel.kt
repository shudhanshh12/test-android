package `in`.okcredit.merchant.customer_ui.ui.staff_link.edit

import `in`.okcredit.backend.contract.GetSpecificCustomerList
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.data.server.model.request.EditAction
import `in`.okcredit.merchant.customer_ui.ui.staff_link.StaffLinkEventsTracker
import `in`.okcredit.merchant.customer_ui.ui.staff_link.edit.StaffLinkEditDetailsContract.*
import `in`.okcredit.merchant.customer_ui.usecase.DeleteCollectionStaffLink
import `in`.okcredit.merchant.customer_ui.usecase.EditStaffCollectionLink
import `in`.okcredit.merchant.customer_ui.usecase.GetActiveStaffLinkDetails
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class StaffLinkEditDetailsViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam("selected_customers") val customerIds: List<String>?,
    private val getActiveStaffLinkDetails: Lazy<GetActiveStaffLinkDetails>,
    private val getSpecificCustomerList: Lazy<GetSpecificCustomerList>,
    private val deleteCollectionStaffLink: Lazy<DeleteCollectionStaffLink>,
    private val editCollectionStaffLink: Lazy<EditStaffCollectionLink>,
    private val staffLinkEventsTracker: Lazy<StaffLinkEventsTracker>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            loadData(),
            loadLinkDetails(),
            loadCustomerList(),
            deleteLinkConfirmed(),
            deleteLinkClicked(),
            deleteCustomerClicked(),
            deleteCustomerConfirmed(),
            updateCustomerAddressClicked(),
            updateCustomerMobileClicked(),
            shareClicked(),
            addToListClicked(),
            deleteLinkCancelled(),
            deleteCustomerCancelled(),
            goBackClicked(),
        )
    }

    private fun loadData() = intent<Intent.Load>()
        .map {
            if (getCurrentState().linkId.isNotEmpty() && getCurrentState().link.isNullOrEmpty()) {
                pushIntent(Intent.LoadLinkDetails)
            }
            if (!customerIds.isNullOrEmpty()) {
                pushIntent(Intent.GetCustomersFromIds(customerIds))
            }
            PartialState.NoChange
        }

    private fun goBackClicked() = intent<Intent.GoBack>().map {
        staffLinkEventsTracker.get().tracCollectionListGoBack(
            linkId = getCurrentState().linkId,
            screen = StaffLinkEventsTracker.Screen.UPDATE_CUSTOMER_DETAILS
        )
        PartialState.NoChange
    }

    private fun deleteCustomerClicked() = intent<Intent.DeleteCustomerClicked>().map {
        staffLinkEventsTracker.get().tracCollectionListSelection(
            linkId = getCurrentState().linkId,
            screen = StaffLinkEventsTracker.Screen.UPDATE_CUSTOMER_DETAILS,
            customerCount = getCurrentState().customerList.size,
            totalDue = getCurrentState().totalDue,
            type = StaffLinkEventsTracker.Type.INDIVIDUAL,
            accountId = it.customerId,
            action = StaffLinkEventsTracker.Action.DESELECT
        )
        emitViewEvent(ViewEvent.ShowConfirmDelete(it.customerId))
        PartialState.NoChange
    }

    private fun deleteLinkCancelled() = intent<Intent.DeleteLinkCancelled>().map {
        staffLinkEventsTracker.get().tracCollectionListDeletePopUpAction(
            linkId = getCurrentState().linkId,
            customerCount = getCurrentState().customerList.size,
            totalDue = getCurrentState().totalDue,
            action = StaffLinkEventsTracker.Action.CANCEL,
            type = StaffLinkEventsTracker.Type.LIST
        )
        PartialState.NoChange
    }

    private fun deleteCustomerCancelled() = intent<Intent.DeleteCustomerCancelled>().map {
        staffLinkEventsTracker.get().tracCollectionListDeletePopUpAction(
            linkId = getCurrentState().linkId,
            customerCount = getCurrentState().customerList.size,
            totalDue = getCurrentState().totalDue,
            action = StaffLinkEventsTracker.Action.CANCEL,
            type = StaffLinkEventsTracker.Type.CUSTOMER
        )
        PartialState.NoChange
    }

    private fun deleteLinkClicked() = intent<Intent.DeleteLinkClicked>().map {
        staffLinkEventsTracker.get().tracCollectionListDeleteList(
            linkId = getCurrentState().linkId,
            customerCount = getCurrentState().customerList.size,
            totalDue = getCurrentState().totalDue,
        )
        emitViewEvent(ViewEvent.ShowConfirmDelete())
        PartialState.NoChange
    }

    private fun updateCustomerAddressClicked() = intent<Intent.UpdateCustomerAddressClicked>().map { intent ->
        val address = getCurrentState().customerList.firstOrNull { it.id == intent.customerId }?.address
        staffLinkEventsTracker.get().tracCollectionListAddCustomerDetails(
            linkId = getCurrentState().linkId,
            accountId = intent.customerId,
            customerCount = getCurrentState().customerList.size,
            totalDue = getCurrentState().totalDue,
            flow = if (address.isNullOrEmpty()) StaffLinkEventsTracker.Flow.NEW else StaffLinkEventsTracker.Flow.EDIT,
            type = StaffLinkEventsTracker.Type.ADDRESS
        )
        emitViewEvent(ViewEvent.ShowUpdateAddress(intent.customerId, address))
        PartialState.NoChange
    }

    private fun updateCustomerMobileClicked() = intent<Intent.UpdateCustomerMobileClicked>().map { intent ->
        val mobile = getCurrentState().customerList.firstOrNull { it.id == intent.customerId }?.mobile
        staffLinkEventsTracker.get().tracCollectionListAddCustomerDetails(
            linkId = getCurrentState().linkId,
            accountId = intent.customerId,
            customerCount = getCurrentState().customerList.size,
            totalDue = getCurrentState().totalDue,
            flow = if (mobile.isNullOrEmpty()) StaffLinkEventsTracker.Flow.NEW else StaffLinkEventsTracker.Flow.EDIT,
            type = StaffLinkEventsTracker.Type.MOBILE
        )
        emitViewEvent(ViewEvent.ShowUpdateMobile(intent.customerId, mobile))
        PartialState.NoChange
    }

    private fun loadLinkDetails() = intent<Intent.LoadLinkDetails>()
        .switchMap { wrap { getActiveStaffLinkDetails.get().execute() } }
        .map { result ->
            when (result) {
                is Result.Success -> {
                    if (customerIds.isNullOrEmpty()) {
                        result.value.accountIds?.let {
                            pushIntent(Intent.GetCustomersFromIds(it))
                        }
                    }
                    PartialState.SetLinkDetails(result.value)
                }
                is Result.Failure -> {
                    if (isInternetIssue(result.error)) {
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

    private fun loadCustomerList() = intent<Intent.GetCustomersFromIds>()
        .switchMap { wrap(getSpecificCustomerList.get().execute(it.customerIds)) }
        .map {
            when (it) {
                is Result.Success -> {
                    PartialState.SetCustomers(it.value)
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

    private fun deleteLinkConfirmed() = intent<Intent.DeleteLinkConfirm>()
        .switchMap { wrap { deleteCollectionStaffLink.get().execute() } }
        .map {
            when (it) {
                is Result.Success -> {
                    staffLinkEventsTracker.get().tracCollectionListDeletePopUpAction(
                        linkId = getCurrentState().linkId,
                        customerCount = getCurrentState().customerList.size,
                        totalDue = getCurrentState().totalDue,
                        action = StaffLinkEventsTracker.Action.DELETE,
                        type = StaffLinkEventsTracker.Type.LIST
                    )
                    emitViewEvent(ViewEvent.FinishScreen)
                    PartialState.NoChange
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

    private fun deleteCustomerConfirmed() = intent<Intent.DeleteCustomerConfirmed>()
        .switchMap {
            wrap {
                editCollectionStaffLink.get().execute(
                    linkId = getCurrentState().linkId,
                    customerIds = listOf(it.customerId),
                    action = EditAction.DELETE
                )
            }
        }
        .map {
            when (it) {
                is Result.Success -> {
                    staffLinkEventsTracker.get().tracCollectionListDeletePopUpAction(
                        linkId = getCurrentState().linkId,
                        customerCount = getCurrentState().customerList.size,
                        totalDue = getCurrentState().totalDue,
                        action = StaffLinkEventsTracker.Action.DELETE,
                        type = StaffLinkEventsTracker.Type.CUSTOMER
                    )
                    PartialState.CustomersDeleted(it.value)
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

    private fun shareClicked() = intent<Intent.ShareClicked>()
        .map {
            staffLinkEventsTracker.get().tracCollectionListShareList(
                linkId = getCurrentState().linkId,
                customerCount = getCurrentState().customerList.size,
                totalDue = getCurrentState().totalDue,
                screen = StaffLinkEventsTracker.Screen.UPDATE_CUSTOMER_DETAILS
            )
            getCurrentState().link?.let { it1 -> emitViewEvent(ViewEvent.ShareOnWhatsApp(it1)) }
            PartialState.NoChange
        }

    private fun addToListClicked() = intent<Intent.AddToListClicked>()
        .map {
            staffLinkEventsTracker.get().tracCollectionListAddCustomer(
                linkId = getCurrentState().linkId,
            )
            emitViewEvent(
                ViewEvent.GoToAddCustomer(
                    getCurrentState().linkId,
                    getCurrentState().link,
                    getCurrentState().linkCreateTime,
                    getCurrentState().customerList.map { it.id }
                )
            )
            PartialState.NoChange
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return StaffLinkEditDetailReducer.reduce(currentState, partialState)
    }
}
