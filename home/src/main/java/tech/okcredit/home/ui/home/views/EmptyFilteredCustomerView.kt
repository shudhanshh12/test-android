package tech.okcredit.home.ui.home.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.jakewharton.rxbinding3.view.clicks
import kotlinx.android.synthetic.main.homesearch_filterview.view.*
import tech.okcredit.home.R
import tech.okcredit.home.ui.homesearch.Sort
import java.util.concurrent.TimeUnit

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class EmptyFilteredCustomerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var sort: Sort

    init {
        LayoutInflater.from(context).inflate(R.layout.empty_filtered_customers, this, true)
    }

    interface Listener {
        fun onClearFilterCleared(sort: Sort, source: String)
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        clear_filter.clicks()
            .throttleFirst(300, TimeUnit.MILLISECONDS)
            .doOnNext {
                listener?.onClearFilterCleared(sort, "empty results")
            }
            .subscribe()
    }

    @ModelProp(ModelProp.Option.DoNotHash)
    fun setSortProp(sort: Sort) {
        this.sort = sort
    }
}
