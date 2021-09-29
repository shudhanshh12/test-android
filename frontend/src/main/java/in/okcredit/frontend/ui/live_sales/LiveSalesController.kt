package `in`.okcredit.frontend.ui.live_sales

import `in`.okcredit.frontend.BuildConfig
import `in`.okcredit.frontend.ui.live_sales.views.TransactionView
import `in`.okcredit.frontend.ui.live_sales.views.pictureOnboardingView
import `in`.okcredit.frontend.ui.live_sales.views.transactionView
import `in`.okcredit.frontend.usecase.GetLiveSalesStatement
import `in`.okcredit.merchant.customer_ui.ui.customer.views.dateView
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.VisibilityState
import merchant.okcredit.accounting.model.Transaction
import merchant.okcredit.accounting.views.loaderPlaceholderView
import merechant.okcredit.resources.view.noInternetView
import org.joda.time.DateTime
import tech.okcredit.android.base.utils.DateTimeUtils
import java.util.*

class LiveSalesController constructor(
    private val liveSaleScreenFragment: LiveSalesFragment
) : AsyncEpoxyController() {

    private lateinit var state: LiveSalesContract.State
    private var visibleTransactions: ArrayList<Transaction> = arrayListOf()

    init {
        isDebugLoggingEnabled = BuildConfig.DEBUG
    }

    fun setState(state: LiveSalesContract.State) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {
        when {
            state.networkError -> noInternetView {
                id("noInternetView")
            }

            state.isLoading -> loaderPlaceholderView {
                id("loaderPlaceholderView")
            }

            state.transactions.isNullOrEmpty() -> {
                pictureOnboardingView {
                    id("pictureOnBoardingView")
                }
            }

            state.transactions.isNotEmpty() -> {

                state.transactions.map {
                    if (it.type == GetLiveSalesStatement.LiveSaleItemViewType.DATE_ITEM) {
                        val dateTimeString =
                            DateTimeUtils.formatTx(it.date, liveSaleScreenFragment.requireContext())
                        dateView {
                            id(it.date?.millis.toString())
                            date(dateTimeString)
                        }
                    } else if (it.type == GetLiveSalesStatement.LiveSaleItemViewType.TXN_ITEM) {
                        transactionView {
                            id(it.transaction?.id)
                            data(
                                TransactionView.TransactionViewDataModel(
                                    it.transaction!!,
                                )
                            )
                            listener(liveSaleScreenFragment)
                            onVisibilityChanged { model, _, percentVisibleHeight, _, _, _ ->
                                run {
                                    if (percentVisibleHeight > 0) {
                                        setVisibleTransactions(model.data().transaction, VisibilityState.VISIBLE)
                                    } else {
                                        setVisibleTransactions(model.data().transaction, VisibilityState.INVISIBLE)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setVisibleTransactions(transaction: Transaction, visibilityState: Int) {
        if (visibilityState == VisibilityState.VISIBLE) {
            if (!visibleTransactions.contains(transaction)) {
                visibleTransactions.add(transaction)
                liveSaleScreenFragment.onTopScrollItemChanged(getDateOfFirstVisibleTx())
            }
        } else if (visibilityState == VisibilityState.INVISIBLE) {
            if (visibleTransactions.contains(transaction)) {
                visibleTransactions.remove(transaction)
                liveSaleScreenFragment.onTopScrollItemChanged(getDateOfFirstVisibleTx())
            }
        }
    }

    private fun getDateOfFirstVisibleTx(): DateTime {
        var dateTime = DateTime(0)
        for (item in visibleTransactions) {
            if (dateTime.millis == 0L) {
                dateTime = item.createdAt
            } else if (dateTime.isAfter(item.createdAt)) {
                dateTime = item.createdAt
            }
        }
        return dateTime
    }
}
