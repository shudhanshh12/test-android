package `in`.okcredit.merchant.customer_ui.ui.subscription.add.frequency

import com.airbnb.epoxy.AsyncEpoxyController

class MonthController : AsyncEpoxyController() {

    private var listener: ((Int) -> Unit)? = null

    private var checkedDate = 0

    fun setListener(listener: ((Int) -> Unit)?) {
        this.listener = listener
    }

    fun setCheckedDate(date: Int) {
        this.checkedDate = date
        requestModelBuild()
    }

    override fun buildModels() {
        for (count in 1 until 29) {
            monthView {
                id(count)
                date(count)
                checked(count == checkedDate)
                clickListener {
                    listener?.invoke(it)
                }
            }
        }
    }
}
