package `in`.okcredit.merchant.customer_ui.ui.staff_link.add

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.ui.staff_link.add.StaffLinkAddCustomerContract.PartialState
import `in`.okcredit.merchant.customer_ui.ui.staff_link.add.StaffLinkAddCustomerContract.State
import `in`.okcredit.merchant.customer_ui.usecase.GetCustomerWithPaymentDue
import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test
import tech.okcredit.android.base.extensions.getColorCompat

class StaffLinkAddCustomerReducerTest {

    private val context: Context = mockk()
    private val spannableStringBuilder: SpannableStringBuilder = mockk()

    private fun createReducer(preSelectedList: Set<String>) = StaffLinkAddCustomerReducer(
        { context },
        preSelectedList
    )

    @Test
    fun `SetCustomerList show select all customers when no preselected customers`() {
        every { context.getString(R.string.due) } returns ""
        every { context.getString(R.string.after_due_on) } returns "After "
        every { context.getString(R.string.t_003_staff_collection_amount_due_date) } returns "Due today"
        every { context.getString(R.string.since_due_on) } returns "Since today"
        every { context.getColorCompat(R.color.tx_credit) } returns Color.RED
        every { spannableStringBuilder.length } returns 4
        val reducer = createReducer(emptySet())
        val response = GetCustomerWithPaymentDue.CustomerSearchWrapper(
            "",
            listOf(TestData.CUSTOMER, TestData.CUSTOMER_2, TestData.CUSTOMER_3),
            listOf(TestData.CUSTOMER, TestData.CUSTOMER_2, TestData.CUSTOMER_3)
        )
        val newState = reducer.reduce(State(), PartialState.SetCustomerList(response))
        val expectedSelectedCustomers = setOf(
            TestData.CUSTOMER.id,
            TestData.CUSTOMER_2.id,
            TestData.CUSTOMER_3.id
        )
        Assert.assertTrue(
            newState.searchQuery == "" &&
                !newState.showEditableSearch &&
                newState.showTopSummaryCard &&
                newState.totalDue == 700L &&
                newState.showSelectAllHeader &&
                newState.selectedCustomerIds == expectedSelectedCustomers &&
                !newState.loading
        )
    }

    @Test
    fun `SetCustomerList sets the correct state for first time when preselected customers present`() {
        every { context.getString(R.string.after_due_on) } returns "After "
        every { context.getString(R.string.t_003_staff_collection_amount_due_date) } returns "Due today"
        every { context.getString(R.string.since_due_on) } returns "Since "
        every { context.getString(R.string.due) } returns "Due"
        every { context.getColorCompat(R.color.tx_credit) } returns Color.RED
        every { spannableStringBuilder.length } returns 4
        val reducer = createReducer(setOf(TestData.CUSTOMER.id, TestData.CUSTOMER_2.id))
        val response = GetCustomerWithPaymentDue.CustomerSearchWrapper(
            "",
            listOf(TestData.CUSTOMER, TestData.CUSTOMER_2, TestData.CUSTOMER_3),
            listOf(TestData.CUSTOMER, TestData.CUSTOMER_2, TestData.CUSTOMER_3)
        )
        val newState = reducer.reduce(
            State(selectedCustomerIds = setOf(TestData.CUSTOMER.id, TestData.CUSTOMER_2.id)),
            PartialState.SetCustomerList(response)
        )
        println(newState.selectedCustomerIds)
        Assert.assertTrue(
            newState.selectedCustomerIds == setOf(TestData.CUSTOMER.id, TestData.CUSTOMER_2.id)
        )
    }

    @Test
    fun `SearchClicked sets the correct state`() {
        val reducer = createReducer(emptySet())
        val newState = reducer.reduce(State(), PartialState.SearchClicked)
        Assert.assertTrue(
            !newState.showTopSummaryCard &&
                newState.showEditableSearch
        )
    }

    @Test
    fun `DismissSearch sets the correct state`() {
        val reducer = createReducer(emptySet())
        val newState = reducer.reduce(State(), PartialState.DismissSearch)
        Assert.assertTrue(
            newState.showTopSummaryCard &&
                newState.showSelectAllHeader &&
                !newState.showEditableSearch
        )
    }

    @Test
    fun `SelectCustomers sets the correct state`() {
        val reducer = createReducer(emptySet())
        val newState = reducer.reduce(State(), PartialState.SelectCustomers(setOf(TestData.CUSTOMER_2.id)))
        Assert.assertTrue(
            newState.selectedCustomerIds == setOf(TestData.CUSTOMER_2.id) &&
                newState.showBottomActions
        )
    }

    @Test
    fun `SelectCustomers sets the correct state when empty selected list sent`() {
        val reducer = createReducer(emptySet())
        val newState = reducer.reduce(State(), PartialState.SelectCustomers(emptySet()))
        Assert.assertTrue(
            newState.selectedCustomerIds.isEmpty() &&
                !newState.showBottomActions
        )
    }
}
