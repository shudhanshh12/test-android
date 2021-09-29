package `in`.okcredit.supplier.statement

import `in`.okcredit.supplier.usecase.SupplierTransactionWrapper

sealed class AccountStatementModel {

    data class StatementSummary(
        val total: Long,
        val paymentAmount: Long,
        val paymentCount: Int,
        val creditCount: Int,
        val creditAmount: Long,
    ) : AccountStatementModel()

    data class Transaction(val wrapper: SupplierTransactionWrapper) : AccountStatementModel()

    object NetworkError : AccountStatementModel()

    object Loading : AccountStatementModel()

    object LoadMore : AccountStatementModel()

    object Empty : AccountStatementModel()
}

fun SupplierTransactionWrapper.toTransactionStatementModel() = AccountStatementModel.Transaction(this)
