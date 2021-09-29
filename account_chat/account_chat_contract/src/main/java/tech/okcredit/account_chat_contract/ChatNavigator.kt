package tech.okcredit.account_chat_contract

import android.content.Context
import android.content.Intent

interface ChatNavigator {
    fun getChatIntent(
        context: Context,
        accountId: String,
        role: String,
        unreadMessageCount: String,
        firstUnseenMessageId: String? = null
    ): Intent
}
