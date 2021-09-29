package `in`.okcredit.frontend.ui.supplier

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.ui.supplier.views.DeleteTransactionView
import `in`.okcredit.frontend.ui.supplier.views.SupplierProcessingTransactionView
import `in`.okcredit.frontend.ui.supplier.views.TransactionView
import `in`.okcredit.frontend.ui.supplier.views.deleteTransactionView
import `in`.okcredit.frontend.ui.supplier.views.supplierProcessingTransactionView
import `in`.okcredit.frontend.ui.supplier.views.transactionView
import `in`.okcredit.merchant.customer_ui.ui.customer.views.dateView
import `in`.okcredit.merchant.customer_ui.ui.customer.views.emptyPlaceholderView
import `in`.okcredit.shared.performance.PerformanceTracker
import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.TypedEpoxyController
import dagger.Lazy
import merchant.okcredit.accounting.analytics.AccountingEventTracker
import merchant.okcredit.accounting.views.LoadMoreView
import merchant.okcredit.accounting.views.loadMoreView
import merchant.okcredit.accounting.views.loaderPlaceholderView
import merechant.okcredit.resources.view.noInternetView

class SupplierControllerV2 constructor(
    private val tracker: Tracker,
    private var performanceTracker: Lazy<PerformanceTracker>,
    private var accountingEventTracker: Lazy<AccountingEventTracker>,
) : TypedEpoxyController<List<SupplierScreenItem>>(
    EpoxyAsyncUtil.getAsyncBackgroundHandler(),
    EpoxyAsyncUtil.getAsyncBackgroundHandler()
) {

    private var supplierControllerListener: SupplierControllerListener? = null

    fun addListener(customerControllerListener: SupplierControllerListener) {
        this.supplierControllerListener = customerControllerListener
    }

    fun removeListener() {
        supplierControllerListener = null
    }

    override fun buildModels(data: List<SupplierScreenItem>?) {
        data?.forEach { item ->
            when (item) {
                SupplierScreenItem.NetworkErrorItem -> renderNetworkErrorView()

                SupplierScreenItem.LoadingItem -> renderLoadingView()

                is SupplierScreenItem.EmptyPlaceHolder -> renderEmptyPlaceHolder(item)

                SupplierScreenItem.LoadMoreItem -> renderLoadMoreItem()

                is SupplierScreenItem.DateItem -> renderDateView(item)

                is SupplierScreenItem.TransactionItem -> renderTransactionItem(item)

                is SupplierScreenItem.DeletedTransaction -> renderDeletedTransaction(item)

                is SupplierScreenItem.ProcessingTransaction -> renderProcessingTransaction(item)
            }
        }
    }

    private fun renderNetworkErrorView() =
        noInternetView {
            id("noInternetView")
        }

    private fun renderLoadingView() =
        loaderPlaceholderView {
            id("loaderPlaceholderView")
        }

    private fun renderEmptyPlaceHolder(emptyPlaceHolder: SupplierScreenItem.EmptyPlaceHolder) =
        emptyPlaceholderView {
            id("emptyPlaceholderView")
            customerName(emptyPlaceHolder.supplierName)
        }

    private fun renderLoadMoreItem() {
        loadMoreView {
            id("loadMoreView")
            listener(supplierControllerListener)
        }
    }

    private fun renderDateView(dateItem: SupplierScreenItem.DateItem) {
        dateView {
            id("dateView${dateItem.date}")
            date(dateItem.date)
        }
    }

    private fun renderTransactionItem(transaction: SupplierScreenItem.TransactionItem) {
        transactionView {
            id(transaction.txnId)
            data(transaction)
            tracker(tracker)
            listener(supplierControllerListener)
        }
    }

    private fun renderProcessingTransaction(item: SupplierScreenItem.ProcessingTransaction) {
        supplierProcessingTransactionView {
            id(item.txnId)
            processingTransaction(item)
            tracker(accountingEventTracker.get())
            listener(supplierControllerListener)
        }
    }

    private fun renderDeletedTransaction(deletedTransaction: SupplierScreenItem.DeletedTransaction) {
        deleteTransactionView {
            id(deletedTransaction.id)
            transaction(deletedTransaction)
            performanceTracker(performanceTracker.get())
            tracker(accountingEventTracker.get())
            listener(supplierControllerListener)
        }
    }

    interface SupplierControllerListener :
        LoadMoreView.Listener,
        DeleteTransactionView.Listener,
        TransactionView.Listener,
        SupplierProcessingTransactionView.Listener
}
