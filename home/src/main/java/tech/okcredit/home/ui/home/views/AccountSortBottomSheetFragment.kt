package tech.okcredit.home.ui.home.views

import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.bottom_sheet_fragment_account_sort.*
import tech.okcredit.home.R
import tech.okcredit.home.ui.homesearch.HomeConstants
import tech.okcredit.home.ui.homesearch.Sort
import kotlin.apply

class AccountSortBottomSheetFragment : ExpandedBottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_fragment_account_sort, container, false)
    }

    internal fun handleUserExit() {
        parentFragment().cancelSort(Sort, "Outer Sreen")
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        handleUserExit()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tempFilter = mutableListOf<String>().apply {
            addAll(Sort.sortfilter)
        }
        var tempSortBy = Sort.sortBy

        toggleFilterVisibility()

        for (i in tempFilter) {
            when (i) {
                HomeConstants.SORT_FILTER_UPCOMING_DUE -> {
                    makeButtonActive(upcoming_due)
                }
                HomeConstants.SORT_FILTER_DUE_CROSSED -> {
                    makeButtonActive(due_crossed)
                }
                HomeConstants.SORT_FILTER_DUE_TODAY -> {
                    makeButtonActive(due_today)
                }
            }
        }
        when (tempSortBy) {
            HomeConstants.SORT_BY_LATEST -> {
                rdg_latest.isChecked = true
            }
            HomeConstants.SORT_BY_NAME -> {
                rdg_name.isChecked = true
            }
            HomeConstants.SORT_BY_AMOUNT -> {
                rdg_amount.isChecked = true
            }
        }

        due_today.setOnClickListener {

            if (tempFilter.contains(HomeConstants.SORT_FILTER_DUE_TODAY)) {
                tempFilter.remove(HomeConstants.SORT_FILTER_DUE_TODAY)
                makeButtonInActive(due_today)
            } else {
                tempFilter.add(HomeConstants.SORT_FILTER_DUE_TODAY)
                makeButtonActive(due_today)
            }
        }
        due_crossed.setOnClickListener {

            if (tempFilter.contains(HomeConstants.SORT_FILTER_DUE_CROSSED)) {
                tempFilter.remove(HomeConstants.SORT_FILTER_DUE_CROSSED)
                makeButtonInActive(due_crossed)
            } else {
                tempFilter.add(HomeConstants.SORT_FILTER_DUE_CROSSED)
                makeButtonActive(due_crossed)
            }
        }
        upcoming_due.setOnClickListener {

            if (tempFilter.contains(HomeConstants.SORT_FILTER_UPCOMING_DUE)) {

                tempFilter.remove(HomeConstants.SORT_FILTER_UPCOMING_DUE)
                makeButtonInActive(upcoming_due)
            } else {
                tempFilter.add(HomeConstants.SORT_FILTER_UPCOMING_DUE)
                makeButtonActive(upcoming_due)
            }
        }
        rdg.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(arg0: RadioGroup?, id: Int) {
                Sort.isDefaultSortByApplied = false
                when (id) {

                    R.id.rdg_latest -> {
                        tempSortBy = HomeConstants.SORT_BY_LATEST
                    }
                    R.id.rdg_amount -> {
                        tempSortBy = HomeConstants.SORT_BY_AMOUNT
                    }
                    R.id.rdg_name -> {
                        tempSortBy = HomeConstants.SORT_BY_NAME
                    }
                }
            }
        })

        apply.setOnClickListener {
            val isFilterApplied = Sort.sortfilter.isNotEmpty()
            Sort.sortfilter.clear()
            Sort.sortfilter.addAll(tempFilter)
            tempFilter.clear()
            Sort.sortApplied = true
            Sort.sortBy = tempSortBy
            parentFragment().applySort(Sort, isFilterApplied)
        }
        clear.setOnClickListener {
            Sort.sortApplied = false
            Sort.reset()
            tempFilter.clear()
            tempSortBy = Sort.sortByDefault
            reset()
            parentFragment().clearFilter("filter")
        }
        cancel.setOnClickListener {
            tempFilter.clear()
            tempSortBy = Sort.sortByDefault
            parentFragment().cancelSort(Sort, "Button")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d: Dialog = super.onCreateDialog(savedInstanceState)
        d.setOnShowListener { dialog ->
            val d: BottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheet: FrameLayout =
                d.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behaviour = BottomSheetBehavior.from(bottomSheet)
            behaviour.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        handleUserExit()
                        dismiss()
                    }
                }
            })
        }
        return d
    }

    private fun toggleFilterVisibility() {
        sort_only.visibility = View.VISIBLE
        sort_filter_contianer.visibility = View.VISIBLE
        sort_by.visibility = View.VISIBLE
    }

    fun reset() {
        rdg_latest?.isChecked = true
        makeButtonInActive(upcoming_due)
        makeButtonInActive(due_crossed)
        makeButtonInActive(due_today)
    }

    private fun makeButtonInActive(dueToday: MaterialButton?) {
        dueToday?.icon = null
        dueToday?.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
        dueToday?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        dueToday?.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey900))
    }

    private fun makeButtonActive(dueToday: MaterialButton?) {
        dueToday?.icon = context?.getDrawable(R.drawable.ic_check_green)
        dueToday?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green_lite))
        dueToday?.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green_lite))
        dueToday?.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_primary))
    }

    fun parentFragment(): AccountSortListener {

        if (parentFragment is AccountSortListener) {
            return parentFragment as AccountSortListener
        }
        throw IllegalStateException("No right parent")
    }

    /**
     *
     */
    interface AccountSortListener {
        fun applySort(sort: Sort, isFilterApplied: Boolean)
        fun cancelSort(sort: Sort, source: String)
        fun clearFilter(source: String)
    }

    companion object {

        val TAG: String? = AccountSortBottomSheetFragment::class.java.simpleName

        fun newInstance(): AccountSortBottomSheetFragment {
            return AccountSortBottomSheetFragment()
        }
    }
}
