package tech.okcredit.userSupport

enum class ContextualHelp(val value: List<String>) {

    CUSTOMER(arrayListOf("customer", "common_ledger", "transaction")),
    CUSTOMER_PROFILE(arrayListOf("customer")),
    TRANSACTION(arrayListOf("transaction")),
    SUPPLIER(arrayListOf(ContextualHelp.SUPPLIER_TYPE, "common_ledger", "customer", "transaction")),
    COMMON_LEDGER(arrayListOf("common_ledger")),
    SUPPLIER_PROFILE(arrayListOf("customer")),
    SUPPLIER_TRANSACTION(arrayListOf("transaction")),
    ACCOUNT(arrayListOf("account_statement", "account_security", "merchant_profile")),
    SECURITY(arrayListOf("account_security")),
    LANGUAGE(arrayListOf("language")),
    MERCHANT(arrayListOf("merchant_profile")),
    REWARD(arrayListOf("reward")),
    COLLECTION(arrayListOf("setup_collection")),
    SHARE_OKC(arrayListOf("share_okcredit")),
    COLLECTION_TARGETED_REFERRAL(arrayListOf("collection_targeted_referral"));

    companion object {
        const val SUPPLIER_TYPE = "supplier"
    }
}
