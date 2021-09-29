package `in`.okcredit.frontend.ui.account_statement

import `in`.okcredit.frontend.ui.account_statement.views.transactionView
import com.airbnb.epoxy.AsyncEpoxyController
import merchant.okcredit.accounting.views.emptyPlaceholderView
import merchant.okcredit.accounting.views.loadMoreView
import merchant.okcredit.accounting.views.loaderPlaceholderView
import merchant.okcredit.accounting.views.totalBalanceView
import merechant.okcredit.resources.view.noInternetView
import javax.inject.Inject

class AccountStatementController @Inject
constructor(private val accountStatementFragment: AccountStatementFragment) : AsyncEpoxyController() {
    private lateinit var state: AccountStatementContract.State

    fun setState(state: AccountStatementContract.State) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {
        val onlineTxns =
            state.transactions.filter { transaction -> transaction.transaction.isOnlinePaymentTransaction }
        when {
            state.networkError -> noInternetView {
                id("loaderPlaceholderView")
            }

            state.isLoading -> loaderPlaceholderView {
                id("loaderPlaceholderView")
            }

            (state.transactions.isEmpty() || (state.isOnlineTransactionSelected && onlineTxns.isEmpty())) -> emptyPlaceholderView {
                id("emptyPlaceholderView")
            }

            state.transactions.isNotEmpty() -> {
                if (!state.isOnlineTransactionSelected || onlineTxns.isNotEmpty()) {
                    totalBalanceView {
                        id("totalBalanceView")
                        total(state.totalCreditAmount - state.totalPaymentAmount - state.totalDiscountAmount)
                        paymentAmount(state.totalPaymentAmount)
                        paymentCount(state.totalPaymentCount)
                        creditCount(state.totalCreditCount)
                        creditAmount(state.totalCreditAmount)
                        discountAmount(state.totalDiscountAmount)
                    }
                }

                for (transaction in state.transactions) {
                    if (state.isOnlineTransactionSelected.not() || transaction.transaction.isOnlinePaymentTransaction) {
                        transactionView {
                            id(transaction.transaction.toString())
                            transaction(transaction)
                            listener(accountStatementFragment)
                        }
                    }
                }

                if (state.isShowOld) {
                    loadMoreView {
                        id("loadMoreView")
                        listener(accountStatementFragment)
                    }
                }
            }
        }
    }
}
