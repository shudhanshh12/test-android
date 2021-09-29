package tech.okcredit.home.widgets.filter_option

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import tech.okcredit.home.databinding.HomeFilterContainerBinding

/**
 * A composite view for home screen filter view
 **/

class FilterContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    val binding: HomeFilterContainerBinding = HomeFilterContainerBinding.inflate(LayoutInflater.from(context), this)

    fun setFilterCount(count: Int) {
        if (count == 0) {
            binding.filterCount.visibility = View.GONE
            binding.filterOverflow.visibility = View.VISIBLE
        } else {
            binding.filterCount.text = count.toString()
            binding.filterCount.visibility = View.VISIBLE
            binding.filterOverflow.visibility = View.GONE
        }
    }
}
