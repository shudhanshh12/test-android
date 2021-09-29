package `in`.okcredit.collection.contract

object CollectionConstants {
    const val INVALID_ACCOUNT_NUMBER = "invalid_account_number"
    const val INVALID_IFSC = "invalid_ifsc"
    const val INVALID_PAYMENT_ADDRESS = "invalid_payment_address"
    const val DESTINATION_NOT_SET = "destination_not_set"
    const val DUPLICATE_PAYOUT_REQ = "duplicate_payout_request"
    const val SUPPLIER_COLLECTION_REQUEST_CODE = 40001
    const val QR_SCANNER_REQUEST_CODE = 40002
    const val QR_SCANNER_REQUEST_CODE_BILLING = 400055

    const val DELETE_UPI_FLAG = 101
    const val UPDATE_UPI_FLAG = 102
    const val UPDATE_BANK_ACCOUNT_FLAG = 103
    const val DELETE_BANK_ACCOUNT_FLAG = 104
    const val UPDATE_PAYMENT_ADDRESS = 105

    const val OKCREDIT_TERMS_URL = "https://www.okcredit.in/terms"
    const val UPI_ID = "upi_id"
    const val METHOD = "Method"
}

enum class ScreenNames(val value: String) {
    AccountScreen("Account Screen"),
    CustomerScreen("Customer Screen"),
    TxnDetailsScreen("Txn Details Screen"),
    RewardsScreen("Rewards Screen"),
    MerchantScreen("Merchant Screen"),
    SecurityScreen("Security Screen"),
    CustomerProfile("Customer Profile"),
    Collection("Collection Screen")
}
