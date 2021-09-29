package `in`.okcredit.collection_ui.ui.passbook.payments

import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.DialogDateFilterListBinding
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

class DateFilterListDialog : ExpandedBottomSheetDialogFragment() {

    lateinit var binding: DialogDateFilterListBinding

    private var listener: Listener? = null
    private var filters: Filters = Filters.Today

    interface Listener {
        fun onFilterClicked(filters: Filters)
    }

    enum class Filters(val value: String) {
        Today("Today"), Last7Days("Last 7 Days"), Last30Days("Last 30 Days"), Last3Months("Last 3 Months"), Overall("Overall")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedCornerBottomSheet)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogDateFilterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvFilter.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFilter.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )
        render()
    }

    private fun prepData(filters: Filters): List<FilterItemView.FilterOption> {
        return listOf(
            FilterItemView.FilterOption(Filters.Today.value, filters == Filters.Today),
            FilterItemView.FilterOption(Filters.Last7Days.value, filters == Filters.Last7Days),
            FilterItemView.FilterOption(Filters.Last30Days.value, filters == Filters.Last30Days),
            FilterItemView.FilterOption(Filters.Last3Months.value, filters == Filters.Last3Months),
            FilterItemView.FilterOption(Filters.Overall.value, filters == Filters.Overall)
        )
    }

    fun render() {
        val list = prepData(filters)
        binding.rvFilter.withModels {
            list.forEachIndexed { index, filterOption ->
                filterItemView {
                    id(index)
                    data(filterOption)
                    listener(filterItemListener())
                }
            }
        }
    }

    private fun filterItemListener() = object : FilterItemView.Listener {
        override fun onClick(filter: String) {
            val filterSelected = when (filter) {
                Filters.Today.value -> Filters.Today
                Filters.Last7Days.value -> Filters.Last7Days
                Filters.Last30Days.value -> Filters.Last30Days
                Filters.Last3Months.value -> Filters.Last3Months
                Filters.Overall.value -> Filters.Overall
                else -> Filters.Today
            }
            setFilter(filterSelected)
            render()
            listener?.onFilterClicked(filterSelected)
            dismiss()
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun setFilter(filters: Filters) {
        this.filters = filters
    }

    companion object {
        const val TAG = "DateFilterListDialog"
    }
}
