package `in`.okcredit.collection.contract

interface CustomerOnlineEducationListener {

    fun skipAndSend(dontAskAgain: Boolean)

    fun setupNow(dontAskAgain: Boolean)

    fun onDismiss()
}
