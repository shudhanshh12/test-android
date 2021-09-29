package `in`.okcredit.collection_ui.ui.passbook.payments

import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.DialogTransactionTypeFilterBinding
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class TransactionTypeFilterDialog : ExpandedBottomSheetDialogFragment() {

    interface Listener {
        fun onTransactionFilterClicked(filter: TransactionFilter)
    }

    private var listener: Listener? = null

    private val binding: DialogTransactionTypeFilterBinding by viewLifecycleScoped(
        DialogTransactionTypeFilterBinding::bind
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DialogTransactionTypeFilterBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val selectedFilter: TransactionFilter =
            (arguments?.getSerializable(ARG_SELECTED_FILTER) as TransactionFilter?) ?: TransactionFilter.ALL
        val list = listOf(
            TransactionFilterView.FilterOption(
                transactionFilter = TransactionFilter.CUSTOMER_COLLECTIONS,
                icon = R.drawable.ic_take,
                text = R.string.t_003_transaction_history_payments_received,
                isSelected = selectedFilter == TransactionFilter.CUSTOMER_COLLECTIONS,
            ),
            TransactionFilterView.FilterOption(
                transactionFilter = TransactionFilter.SUPPLIER_COLLECTIONS,
                icon = R.drawable.ic_give,
                text = R.string.t_003_transaction_history_payment_given,
                isSelected = selectedFilter == TransactionFilter.SUPPLIER_COLLECTIONS,
            ),
            TransactionFilterView.FilterOption(
                transactionFilter = TransactionFilter.ALL,
                icon = R.drawable.ic_all_transactions,
                text = R.string.t_003_transaction_history_all_transactions,
                isSelected = selectedFilter == TransactionFilter.ALL,
            )
        )
        binding.textClose.setOnClickListener { dismissAllowingStateLoss() }
        binding.rvFilter.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFilter.withModels {
            list.forEach {
                transactionFilterView {
                    id(it.toString())
                    data(it)
                    listener(object : TransactionFilterView.Listener {
                        override fun onClick(filter: TransactionFilter) {
                            this@TransactionTypeFilterDialog.listener?.onTransactionFilterClicked(filter)
                            dismissAllowingStateLoss()
                        }
                    })
                }
            }
        }
    }

    override fun onDestroyView() {
        listener = null
        super.onDestroyView()
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    companion object {
        fun getInstance(selectedFilter: TransactionFilter = TransactionFilter.ALL): TransactionTypeFilterDialog {
            return TransactionTypeFilterDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_SELECTED_FILTER, selectedFilter)
                }
            }
        }

        private const val ARG_SELECTED_FILTER = "selected_filter"
    }
}

enum class TransactionFilter {
    CUSTOMER_COLLECTIONS,
    SUPPLIER_COLLECTIONS,
    ALL
}
