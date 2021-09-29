package `in`.okcredit.supplier.statement

import `in`.okcredit.supplier.statement.AccountStatementModel.*
import `in`.okcredit.supplier.statement.views.SupplierTransactionView
import `in`.okcredit.supplier.statement.views.supplierStatementSummaryView
import `in`.okcredit.supplier.statement.views.supplierTransactionView
import com.airbnb.epoxy.TypedEpoxyController
import merchant.okcredit.accounting.views.LoadMoreView
import merchant.okcredit.accounting.views.emptyPlaceholderView
import merchant.okcredit.accounting.views.loadMoreView
import merchant.okcredit.accounting.views.loaderPlaceholderView
import merechant.okcredit.resources.view.noInternetView
import javax.inject.Inject

class SupplierAccountStatementController @Inject constructor(
    private val transactionClickListener: SupplierTransactionView.Listener,
    private val loadMoreListener: LoadMoreView.Listener
) : TypedEpoxyController<List<AccountStatementModel>>() {

    override fun buildModels(data: List<AccountStatementModel>?) {
        data?.forEach {
            when (it) {
                is StatementSummary -> {
                    supplierStatementSummaryView {
                        id("totalBalanceView")
                        total(it.total)
                        paymentAmount(it.paymentAmount)
                        paymentCount(it.paymentCount)
                        creditCount(it.creditCount)
                        creditAmount(it.creditAmount)
                    }
                }

                is Transaction -> {
                    supplierTransactionView {
                        id("supplierTransactionView${it.wrapper.transaction.id}")
                        transaction(it.wrapper.transaction)
                        name(it.wrapper.supplierName)
                        listener(transactionClickListener)
                    }
                }

                NetworkError -> noInternetView {
                    id("loaderPlaceholderView")
                }

                Loading -> loaderPlaceholderView {
                    id("loaderPlaceholderView")
                }

                LoadMore -> loadMoreView {
                    id("loadMoreView")
                    listener(loadMoreListener)
                }

                Empty -> emptyPlaceholderView {
                    id("emptyPlaceholderView")
                }
            }
        }
    }
}
