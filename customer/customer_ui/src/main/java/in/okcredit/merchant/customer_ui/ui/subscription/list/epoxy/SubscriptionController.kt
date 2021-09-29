package `in`.okcredit.merchant.customer_ui.ui.subscription.list.epoxy

import `in`.okcredit.merchant.customer_ui.ui.subscription.list.epoxy.view.subscriptionView
import com.airbnb.epoxy.TypedEpoxyController
import javax.inject.Inject

class SubscriptionController @Inject constructor() : TypedEpoxyController<List<SubscriptionItem>>() {

    private var listener: ((String) -> Unit)? = null

    override fun buildModels(data: List<SubscriptionItem>?) {
        data?.forEach {
            subscriptionView {
                id(it.id)
                subscriptionItem(it)
                itemClickListener { listener?.invoke(it) }
            }
        }
    }

    fun setItemClickListener(listener: ((String) -> Unit)?) {
        this.listener = listener
    }
}
