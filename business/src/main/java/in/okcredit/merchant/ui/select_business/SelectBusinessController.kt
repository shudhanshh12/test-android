package `in`.okcredit.merchant.ui.select_business

import `in`.okcredit.merchant.ui.select_business.view.selectBusinessItemView
import com.airbnb.epoxy.AsyncEpoxyController

class SelectBusinessController(private val fragment: SelectBusinessFragment) : AsyncEpoxyController() {

    private var businessList: List<SelectBusinessContract.BusinessData> = listOf()

    fun setData(businessList: List<SelectBusinessContract.BusinessData>) {
        this.businessList = businessList
        requestModelBuild()
    }

    override fun buildModels() {
        businessList.forEach {
            selectBusinessItemView {
                id(it.business.id)
                businessData(it)
                listener(fragment)
            }
        }
    }
}
