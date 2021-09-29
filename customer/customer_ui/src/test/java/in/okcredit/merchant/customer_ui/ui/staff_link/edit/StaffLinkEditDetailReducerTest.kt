package `in`.okcredit.merchant.customer_ui.ui.staff_link.edit

import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.data.server.model.response.ActiveStaffLinkResponse
import org.junit.Test

class StaffLinkEditDetailReducerTest {

    @Test
    fun `Delete customer reduces total due, customer count and also updates the list`() {
        val existingList = listOf(
            StaffLinkEditDetailsCustomerItem(
                id = "id_1",
                name = "Test_1",
                profilePic = null,
                balance = -1000,
                address = null,
                mobile = null,
                lasPayment = null,
            ),
            StaffLinkEditDetailsCustomerItem(
                "id_2",
                "Test_2",
                null,
                -2000,
                null,
                null,
                lasPayment = null,
            ),
            StaffLinkEditDetailsCustomerItem(
                id = "id_3",
                name = "Test_3",
                profilePic = null,
                balance = -3000,
                address = null,
                mobile = null,
                lasPayment = null,
            ),
            StaffLinkEditDetailsCustomerItem(
                id = "id_4",
                name = "Test_4",
                profilePic = null,
                balance = -4000,
                address = null,
                mobile = null,
                lasPayment = null,
            ),
        )
        val currentState = StaffLinkEditDetailsContract.State(
            totalDue = -10_000,
            customerCountWithBalanceDue = 4,
            customerList = existingList,
        )
        val newState = StaffLinkEditDetailReducer.reduce(
            current = currentState,
            partial = StaffLinkEditDetailsContract.PartialState.CustomersDeleted(listOf("id_3", "id_4"))
        )

        assert(newState.totalDue == -3000L)
        assert(newState.customerCountWithBalanceDue == 2)
        assert(newState.customerList.find { it.id == "id_3" } == null)
        assert(newState.customerList.find { it.id == "id_4" } == null)
    }

    @Test
    fun `SetCustomers sets total due, customer count with payment due and updates the list`() {
        val newState = StaffLinkEditDetailReducer.reduce(
            current = StaffLinkEditDetailsContract.State(),
            partial = StaffLinkEditDetailsContract.PartialState.SetCustomers(
                listOf(
                    TestData.CUSTOMER_2,
                    TestData.CUSTOMER_3,
                    TestData.CUSTOMER_4
                )
            )
        )
        assert(!newState.loading)
        assert(newState.totalDue == -1000L)
        assert(newState.customerCountWithBalanceDue == 3)
        val list = listOf(
            StaffLinkEditDetailsCustomerItem(
                id = "CUSTOMER_2",
                name = "CUSTOMER_2",
                profilePic = null,
                balance = -200,
                address = null,
                mobile = "9999999999",
                lasPayment = null,
            ),
            StaffLinkEditDetailsCustomerItem(
                id = "CUSTOMER_3",
                name = "CUSTOMER_3",
                profilePic = null,
                balance = -400,
                address = null,
                mobile = "9999999999",
                lasPayment = null,
            ),
            StaffLinkEditDetailsCustomerItem(
                id = "CUSTOMER_4",
                name = "CUSTOMER_4",
                profilePic = null,
                balance = -400,
                address = null,
                mobile = "9999999999",
                lasPayment = null,
            ),
        )
        newState.customerList.forEachIndexed { index, customer ->
            assert(customer == list[index])
        }
    }

    @Test
    fun `SetLinkDetails sets the link in new state`() {
        val newState = StaffLinkEditDetailReducer.reduce(
            current = StaffLinkEditDetailsContract.State(),
            partial = StaffLinkEditDetailsContract.PartialState.SetLinkDetails(
                ActiveStaffLinkResponse(
                    accountIds = listOf(),
                    link = "active_link",
                    linkId = "link_id",
                    createTime = 10_000,
                )
            )
        )

        assert(!newState.loading)
        assert(newState.link == "active_link")
        assert(newState.linkCreateTime == 10_000L)
        assert(newState.linkId == "link_id")
    }
}
