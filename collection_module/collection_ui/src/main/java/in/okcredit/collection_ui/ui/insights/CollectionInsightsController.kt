package `in`.okcredit.collection_ui.ui.insights

import `in`.okcredit.collection_ui.ui.insights.views.DefaultersItemViewModel_
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.carousel
import javax.inject.Inject

class CollectionInsightsController @Inject constructor(
    private val collectionInsightsActivity: CollectionInsightsActivity,
) : AsyncEpoxyController() {

    private lateinit var state: CollectionInsightsContract.State

    fun setState(state: CollectionInsightsContract.State) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {
        carousel {
            id("carouselView")
            numViewsToShowOnScreen(3.2f)
            models(
                state.dueCustomers.mapIndexed { index, dueCustomer ->
                    DefaultersItemViewModel_()
                        .id(dueCustomer.id)
                        .image(dueCustomer)
                        .name(dueCustomer.description)
                        .amount(dueCustomer.balanceV2)
                        .clickListener(collectionInsightsActivity)
                }
            )
        }
    }
}
