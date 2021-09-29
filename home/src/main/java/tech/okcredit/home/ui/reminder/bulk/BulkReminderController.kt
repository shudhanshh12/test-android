package tech.okcredit.home.ui.reminder.bulk

import com.airbnb.epoxy.TypedEpoxyController

class BulkReminderController : TypedEpoxyController<List<BulkReminderItem>>() {

    private var listener: ((String) -> Unit)? = null

    fun setListener(listener: ((String) -> Unit)?) {
        this.listener = listener
    }

    override fun buildModels(data: List<BulkReminderItem>?) {
        data?.forEach {
            bulkReminderCustomerView {
                id(it.customerId)
                data(it)
                clickListener {
                    listener?.invoke(it)
                }
            }
        }
    }
}
