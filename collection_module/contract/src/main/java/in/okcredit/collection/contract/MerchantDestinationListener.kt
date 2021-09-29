package `in`.okcredit.collection.contract

interface MerchantDestinationListener {

    fun onAccountAddedSuccessfully(eta: Long)

    fun onCancelled()
}
