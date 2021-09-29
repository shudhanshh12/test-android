package tech.okcredit.account_chat_sdk

interface IChatListner {
    fun executeMessages(businessId: String)
    fun removeListener()
}
