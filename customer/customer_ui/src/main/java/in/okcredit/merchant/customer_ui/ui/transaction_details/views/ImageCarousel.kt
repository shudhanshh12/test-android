package `in`.okcredit.merchant.customer_ui.ui.transaction_details.views

import android.content.Context
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.ModelView

@ModelView(saveViewState = true, autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ImageCarousel(context: Context) : Carousel(context) {

    override fun getSnapHelperFactory(): Nothing? = null
}
