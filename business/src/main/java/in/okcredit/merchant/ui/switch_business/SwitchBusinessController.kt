package `in`.okcredit.merchant.ui.switch_business

import `in`.okcredit.merchant.ui.switch_business.view.businessItemView
import com.airbnb.epoxy.AsyncEpoxyController
import javax.inject.Inject

class SwitchBusinessController @Inject constructor(
    private val fragment: SwitchBusinessDialog,
) : AsyncEpoxyController() {

    private lateinit var state: SwitchBusinessContract.State
    var source: String? = null

    fun setState(state: SwitchBusinessContract.State, source: String?) {
        this.state = state
        this.source = source
        requestModelBuild()
    }

    override fun buildModels() {
        state.businessModelList.forEach { businessModel ->
            businessItemView {
                id(businessModel.business.id)
                business(businessModel)
                source(source)
                listener(fragment)
            }
        }
    }
}
