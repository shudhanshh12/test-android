package `in`.okcredit.merchant.customer_ui.ui.staff_link.add

import com.airbnb.epoxy.TypedEpoxyController

class StaffLinkAddCustomerController : TypedEpoxyController<List<CustomerItem>>() {

    private var customerSelectionListener: StaffLinkAddCustomerView.SelectCustomerListener? = null

    fun setSelectCustomerListener(customerSelectionListener: StaffLinkAddCustomerView.SelectCustomerListener?) {
        this.customerSelectionListener = customerSelectionListener
    }

    override fun buildModels(data: List<CustomerItem>?) {
        data?.forEach {
            staffLinkAddCustomerView {
                id(it.id)
                customerItem(it)
                listener(customerSelectionListener)
            }
        }
    }
}
