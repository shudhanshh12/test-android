package `in`.okcredit.collection_ui.ui.defaulters

import com.airbnb.epoxy.AsyncEpoxyController

class DefaulterController(
    private val activity: DefaulterListActivity
) : AsyncEpoxyController() {

    private var state = DefaulterListContract.State()

    fun setState(state: DefaulterListContract.State) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {
        state.defaulterList?.map { defaulter ->
            defaulterView {
                id("defaulter_${defaulter.customer.id}")
                defaulter(defaulter)
                listener(activity)
            }
        }
    }
}
