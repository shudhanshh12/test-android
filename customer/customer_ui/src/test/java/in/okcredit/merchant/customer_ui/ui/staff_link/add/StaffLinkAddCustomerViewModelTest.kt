package `in`.okcredit.merchant.customer_ui.ui.staff_link.add

import `in`.okcredit.merchant.customer_ui.TestViewModel
import `in`.okcredit.merchant.customer_ui.ui.staff_link.add.StaffLinkAddCustomerContract.*
import io.mockk.mockk

class StaffLinkAddCustomerViewModelTest :
    TestViewModel<State, PartialState, ViewEvent>() {

    override fun createViewModel() = StaffLinkAddCustomerViewModel(
        preSelectedCustomerIds = emptySet(),
        initialState = State(),
        getCustomerWithPaymentDue = mockk(),
        getSpecificCustomerList = mockk(),
        createStaffCollectionLink = mockk(),
        editStaffCollectionLink = mockk(),
        staffLinkAddCustomerReducer = mockk(),
        staffLinkEventsTracker = mockk()
    )
}
