package `in`.okcredit.merchant.customer_ui.ui.staff_link.edit

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.customer_ui.ui.staff_link.edit.StaffLinkEditDetailsContract.*
import `in`.okcredit.shared.base.Reducer

object StaffLinkEditDetailReducer : Reducer<State, PartialState> {
    override fun reduce(current: State, partial: PartialState): State {
        return when (partial) {
            PartialState.NoChange -> current
            is PartialState.SetCustomers -> setStateForCustomers(current, partial.list)
            is PartialState.SetLinkDetails -> current.copy(
                loading = false,
                link = partial.details.link,
                linkId = partial.details.linkId!!,
                linkCreateTime = partial.details.createTime,
            )
            is PartialState.SetLoading -> current.copy(loading = partial.loading)
            is PartialState.CustomersDeleted -> removeDeletedCustomers(current, partial.list.toSet())
        }
    }

    private fun removeDeletedCustomers(
        current: State,
        customerIds: Set<String>,
    ): State {
        val customerList = mutableListOf<StaffLinkEditDetailsCustomerItem>()
        var totalDue = 0L
        var customerCountWithPaymentDue = 0
        current.customerList.forEach {
            if (!customerIds.contains(it.id)) {
                if (it.balance < 0) {
                    totalDue += it.balance
                    customerCountWithPaymentDue++
                }
                customerList.add(it)
            }
        }
        return current.copy(
            customerList = customerList,
            customerCountWithBalanceDue = customerCountWithPaymentDue,
            totalDue = if (totalDue < 0) totalDue else 0
        )
    }

    private fun setStateForCustomers(current: State, customerList: List<Customer>): State {
        val list = mutableListOf<StaffLinkEditDetailsCustomerItem>()
        var totalDue = 0L
        var customerCountWithPaymentDue = 0
        val sortedList = customerList.sortedByDescending { it.lastActivity?.millis ?: 0L }
        sortedList.forEach { customer ->
            if (customer.balanceV2 < 0) {
                totalDue += customer.balanceV2
                customerCountWithPaymentDue++
            }
            val showPaymentReceived =
                if (customer.balanceV2 >= 0 && current.linkCreateTime != null && current.linkCreateTime > 0 && customer.lastPayment != null) {
                    customer.lastPayment!!.millis > current.linkCreateTime
                } else {
                    false
                }
            list.add(
                StaffLinkEditDetailsCustomerItem(
                    id = customer.id,
                    name = customer.description,
                    profilePic = customer.profileImage,
                    mobile = customer.mobile,
                    balance = customer.balanceV2,
                    address = customer.address,
                    showPaymentReceived = showPaymentReceived,
                    lasPayment = if (showPaymentReceived) customer.lastPayment else null
                )
            )
        }

        return current.copy(
            loading = false,
            customerList = list,
            customerCountWithBalanceDue = customerCountWithPaymentDue,
            totalDue = if (totalDue < 0) totalDue else 0
        )
    }
}
