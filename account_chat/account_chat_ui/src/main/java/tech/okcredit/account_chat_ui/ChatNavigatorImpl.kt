package tech.okcredit.account_chat_ui

import android.content.Context
import android.content.Intent
import tech.okcredit.account_chat_contract.ChatNavigator
import tech.okcredit.account_chat_ui.chat_activity.ChatActivity
import javax.inject.Inject

class ChatNavigatorImpl @Inject constructor() : ChatNavigator {
    override fun getChatIntent(
        context: Context,
        accountId: String,
        role: String,
        unreadMessageCount: String,
        firstUnseenMessageId: String?
    ): Intent {
        return ChatActivity.getIntent(
            context = context,
            accountId = accountId,
            role = role,
            unreadMessageCount = unreadMessageCount,
            firstUnseenMessageId = firstUnseenMessageId
        )
    }
}
