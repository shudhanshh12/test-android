package tech.okcredit.home.ui.homesearch.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.homesearch_filterview.view.*
import tech.okcredit.home.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class FilterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.homesearch_filterview, this, true)
    }

    @ModelProp
    fun setSortProp(filterCount: Int) {
        tv_filter_count.text = filterCount.toString()
    }

    interface ClearFilterListener {
        fun onFilterCleared(source: String)
    }

    @CallbackProp
    fun setListener(listener: ClearFilterListener?) {
        clear_filter.setOnClickListener {
            listener?.onFilterCleared("search")
        }
    }
}
