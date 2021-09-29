package tech.okcredit.home.ui.home.supplier

import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import merchant.okcredit.accounting.contract.HomeSortType
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.home.R
import tech.okcredit.home.databinding.SupplierSortFragmentBinding
import tech.okcredit.home.ui.home.supplier.SupplierSortContract.Intent
import tech.okcredit.home.ui.home.supplier.SupplierSortContract.State
import tech.okcredit.home.ui.home.supplier.SupplierSortContract.ViewEvent

class SupplierSortFragment : BaseBottomSheetWithViewEvents<State, ViewEvent, Intent>("SupplierSortFragment") {

    private val binding: SupplierSortFragmentBinding by viewLifecycleScoped(SupplierSortFragmentBinding::bind)

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.supplier_sort_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply.setOnClickListener {
            when (binding.rdg.checkedRadioButtonId) {
                R.id.rdg_latest -> pushIntent(Intent.SelectSortType(HomeSortType.ACTIVITY))
                R.id.rdg_amount -> pushIntent(Intent.SelectSortType(HomeSortType.AMOUNT))
                R.id.rdg_name -> pushIntent(Intent.SelectSortType(HomeSortType.NAME))
            }
        }
        binding.clear.setOnClickListener {
            pushIntent(Intent.SelectSortType(HomeSortType.ACTIVITY))
        }
        binding.cancelSupplierSort.setOnClickListener { dismiss() }
    }

    interface SupplierSortListener {
        fun applySupplierSort()
    }

    companion object {

        const val TAG: String = "SupplierSortFragment"

        fun newInstance() = SupplierSortFragment()
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: State) {
        when (state.sortType) {
            HomeSortType.NONE -> {
                // DO Nothing
            }
            HomeSortType.NAME -> binding.rdgName.isChecked = true
            HomeSortType.AMOUNT -> binding.rdgAmount.isChecked = true
            HomeSortType.ACTIVITY -> binding.rdgLatest.isChecked = true
        }
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.ApplySort -> {
                (parentFragment as? SupplierSortListener)?.applySupplierSort()
                dismiss()
            }
        }
    }
}
