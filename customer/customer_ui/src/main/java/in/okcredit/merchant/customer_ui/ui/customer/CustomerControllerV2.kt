package `in`.okcredit.merchant.customer_ui.ui.customer

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.merchant.customer_ui.ui.customer.views.*
import `in`.okcredit.shared.performance.PerformanceTracker
import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.TypedEpoxyController
import dagger.Lazy
import merchant.okcredit.accounting.analytics.AccountingEventTracker
import merchant.okcredit.accounting.views.LoadMoreView
import merchant.okcredit.accounting.views.loadMoreView
import merchant.okcredit.accounting.views.loaderPlaceholderView

class CustomerControllerV2(
    private var tracker: Lazy<Tracker>,
    private var performanceTracker: Lazy<PerformanceTracker>,
    private var accountingEventTracker: Lazy<AccountingEventTracker>,
) : TypedEpoxyController<List<CustomerScreenItem>>(
    EpoxyAsyncUtil.getAsyncBackgroundHandler(),
    EpoxyAsyncUtil.getAsyncBackgroundHandler()
) {

    private var customerControllerListener: CustomerControllerListener? = null

    fun addListener(customerControllerListener: CustomerControllerListener) {
        this.customerControllerListener = customerControllerListener
    }

    fun removeListener() {
        customerControllerListener = null
    }

    override fun buildModels(data: List<CustomerScreenItem>?) {
        data?.forEach { item ->
            when (item) {
                is CustomerScreenItem.DateItem -> renderDateView(item)
                is CustomerScreenItem.DeletedTransaction -> renderDeletedTransaction(item)
                is CustomerScreenItem.EmptyPlaceHolder -> renderEmptyPlaceHolder(item)
                CustomerScreenItem.LoadingItem -> renderLoadingItem()
                is CustomerScreenItem.RequestActionItem -> renderMerchantInfoCard(item)
                is CustomerScreenItem.ProcessingTransaction -> renderProcessingTransaction(item)
                is CustomerScreenItem.TransactionItem -> renderTransactionItem(item)
                CustomerScreenItem.LoadMoreItem -> renderLoadMoreItem()
                is CustomerScreenItem.InfoNudgeItem -> renderInfoNudgeItem(item)
                is CustomerScreenItem.AcknowledgeActionItem -> renderAcknowledgeActionItem(item)
            }
        }
    }

    private fun renderAcknowledgeActionItem(item: CustomerScreenItem.AcknowledgeActionItem) {
        acknowledgeActionView {
            id("acknowledgeActionView${item.type}")
            acknowledgeActionItem(item)
            listener(customerControllerListener)
        }
    }

    private fun renderInfoNudgeItem(item: CustomerScreenItem.InfoNudgeItem) {
        infoNudgeView {
            id("infoNudgeView${item.type}")
            infoNudgeItem(item)
            listener(customerControllerListener)
        }
    }

    private fun renderLoadMoreItem() {
        loadMoreView {
            id("loadMoreView")
            listener(customerControllerListener)
        }
    }

    private fun renderTransactionItem(transaction: CustomerScreenItem.TransactionItem) {
        transactionView {
            id(transaction.id)
            tracker(tracker.get())
            data(transaction)
            performanceTracker(performanceTracker.get())
            accountingTracker(accountingEventTracker.get())
            listener(customerControllerListener)
        }
    }

    private fun renderProcessingTransaction(item: CustomerScreenItem.ProcessingTransaction) {
        processingTransactionView {
            id(item.id)
            performanceTracker(performanceTracker.get())
            processingTransaction(item)
            tracker(accountingEventTracker.get())
            listener(customerControllerListener)
        }
    }

    private fun renderMerchantInfoCard(item: CustomerScreenItem.RequestActionItem) {
        requestActionView {
            id("requestMerchantInfoView${item.type}")
            requestActionItem(item)
            listener(customerControllerListener)
        }
    }

    private fun renderLoadingItem() {
        loaderPlaceholderView {
            id("loaderPlaceholderView")
        }
    }

    private fun renderEmptyPlaceHolder(item: CustomerScreenItem.EmptyPlaceHolder) {
        emptyCustomerPlaceholderView {
            id("emptyCustomerPlaceholderView")
        }
    }

    private fun renderDeletedTransaction(deletedTransaction: CustomerScreenItem.DeletedTransaction) {
        deleteTransactionView {
            id(deletedTransaction.id)
            transaction(deletedTransaction)
            performanceTracker(performanceTracker.get())
            tracker(accountingEventTracker.get())
            listener(customerControllerListener)
        }
    }

    private fun renderDateView(dateItem: CustomerScreenItem.DateItem) {
        dateView {
            id("dateView${dateItem.date}")
            date(dateItem.date)
        }
    }

    interface CustomerControllerListener :
        LoadMoreView.Listener,
        DeleteTransactionView.Listener,
        TransactionView.Listener,
        InfoNudgeView.Listener,
        ProcessingTransactionView.Listener,
        RequestActionView.RequestActionListener,
        AcknowledgeActionView.AcknowledgeActionListener
}
