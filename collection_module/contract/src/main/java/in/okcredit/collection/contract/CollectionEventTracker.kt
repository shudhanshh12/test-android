package `in`.okcredit.collection.contract

interface CollectionEventTracker {
    companion object {
        const val KYC_VERIFIED = "kyc_verified"
        const val CUSTOMER_SCREEN = "Customer Screen"
        const val CUSTOMER = "Customer"
        const val HOME_PAGE = "Homepage"
    }

    fun setUserProperty(key: String, value: String)

    fun trackCollectionReferralGiftClicked(accountId: String, screen: String, status: String)

    fun trackCollectionReferralGiftShown(accountId: String, screen: String, type: String, status: String)
}
