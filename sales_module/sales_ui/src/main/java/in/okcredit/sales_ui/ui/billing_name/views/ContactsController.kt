package `in`.okcredit.sales_ui.ui.billing_name.views

import `in`.okcredit.sales_ui.ui.billing_name.BillingNameBottomSheetDialog
import `in`.okcredit.sales_ui.ui.billing_name.BillingNameContract
import android.annotation.SuppressLint
import com.airbnb.epoxy.AsyncEpoxyController

class ContactsController(private val fragment: BillingNameBottomSheetDialog) : AsyncEpoxyController() {

    private var state = BillingNameContract.State()

    fun setState(state: BillingNameContract.State) {
        this.state = state
        requestModelBuild()
    }

    @SuppressLint("DefaultLocale")
    override fun buildModels() {
        state.contacts?.let { contact ->
            contact.forEach {
                contactView {
                    id(it.mobile)
                    contact(it)
                    listener(fragment)
                }
            }
        }
    }
}
