package `in`.okcredit.collection_ui.ui.defaulters.model

import `in`.okcredit.backend.contract.Customer

data class Defaulter(
    val customer: Customer,
    val hasUnSyncTransactions: Boolean,
    val isSupplierRegistered: Boolean,
)
