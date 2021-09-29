package `in`.okcredit.merchant.customer_ui.ui.staff_link.edit

import com.airbnb.epoxy.TypedEpoxyController

class StaffLinkEditDetailsController : TypedEpoxyController<List<StaffLinkEditDetailsCustomerItem>>() {

    private var customerSelectionListener: StaffLinkEditDetailsCustomerView.SelectCustomerListener? = null

    fun setSelectCustomerListener(customerSelectionListener: StaffLinkEditDetailsCustomerView.SelectCustomerListener?) {
        this.customerSelectionListener = customerSelectionListener
    }

    override fun buildModels(data: List<StaffLinkEditDetailsCustomerItem>?) {
        data?.forEach {
            staffLinkEditDetailsCustomerView {
                id(it.id)
                customerItem(it)
                listener(customerSelectionListener)
            }
        }
    }
}
