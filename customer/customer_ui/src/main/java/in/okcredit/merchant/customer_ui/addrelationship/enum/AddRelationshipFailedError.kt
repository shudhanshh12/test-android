package `in`.okcredit.merchant.customer_ui.addrelationship.enum

enum class AddRelationshipFailedError(val value: String) {
    DELETED_CUSTOMER_CYCLIC_ACCOUNT("Deleted Customer Cyclic Account"),
    DELETED_SUPPLIER_CYCLIC_ACCOUNT("Deleted Supplier Cyclic Account"),
    ACTIVE_CUSTOMER_CYCLIC_ACCOUNT("Active Customer Cyclic Account"),
    ACTIVE_SUPPLIER_CYCLIC_ACCOUNT("Active Supplier Cyclic Account"),
    MOBILE_CONFLICT_ACCOUNT_WITH_CUSTOMER("Mobile Conflict With Customer"),
    MOBILE_CONFLICT_ACCOUNT_WITH_SUPPLIER("Mobile Conflict With Supplier");
}
