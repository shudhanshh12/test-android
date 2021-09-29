package `in`.okcredit.merchant.customer_ui.ui.address

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.backend._offline.usecase.UpdateCustomer
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker
import `in`.okcredit.merchant.customer_ui.ui.address.UpdateCustomerAddressContract.*
import `in`.okcredit.merchant.customer_ui.ui.staff_link.StaffLinkEventsTracker
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class UpdateCustomerAddressViewModel @Inject constructor(
    initialState: State,
    private val updateCustomer: Lazy<UpdateCustomer>,
    private val customerEventTracker: Lazy<CustomerEventTracker>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            observeAddressChange(),
            observeSubmitTapped(),
        )
    }

    private fun observeAddressChange() = intent<Intent.AddressChanged>()
        .map {
            if (getCurrentState().currentAddress.isNullOrEmpty() && it.address.isNotEmpty()) {
                customerEventTracker.get().trackSelectProfile(
                    screen = StaffLinkEventsTracker.Screen.COLLECTIONS_LIST_FOR_STAFF,
                    relation = CustomerEventTracker.RELATION_CUSTOMER,
                    field = CustomerEventTracker.ADDRESS,
                    accountId = getCurrentState().customerId,
                )
            }
            PartialState.AddressChanged(it.address)
        }

    private fun observeSubmitTapped() = intent<Intent.SubmitTapped>()
        .switchMap {
            wrap(
                updateCustomer.get().executeUpdateAddress(
                    getCurrentState().customerId,
                    getCurrentState().currentAddress ?: ""
                )
            )
        }
        .map {
            when (it) {
                is Result.Success -> {
                    customerEventTracker.get().trackUpdateProfile(
                        screen = StaffLinkEventsTracker.Screen.COLLECTIONS_LIST_FOR_STAFF,
                        relation = CustomerEventTracker.RELATION_CUSTOMER,
                        field = CustomerEventTracker.ADDRESS,
                        accountId = getCurrentState().customerId,
                    )
                    emitViewEvent(ViewEvent.AddressUpdated)
                    PartialState.SetLoading(false)
                }
                is Result.Failure -> {
                    customerEventTracker.get().trackUpdateProfileFailed(
                        screen = StaffLinkEventsTracker.Screen.COLLECTIONS_LIST_FOR_STAFF,
                        relation = PropertyValue.CUSTOMER,
                        field = CustomerEventTracker.ADDRESS,
                        accountId = getCurrentState().customerId,
                        reason = it.error.message,
                    )
                    PartialState.SetLoading(false)
                }
                else -> {
                    PartialState.SetLoading(true)
                }
            }
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            PartialState.NoChange -> currentState
            is PartialState.SetLoading -> currentState.copy(loading = partialState.loading)
            is PartialState.AddressChanged -> currentState.copy(currentAddress = partialState.address)
        }
    }
}
