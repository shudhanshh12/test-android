package tech.okcredit.account_chat_sdk

import `in`.okcredit.analytics.AnalyticsProvider
import android.content.Context
import dagger.Lazy
import tech.okcredit.account_chat_contract.FIELDS
import tech.okcredit.account_chat_contract.STRING_CONSTANTS
import tech.okcredit.account_chat_sdk.models.Message
import tech.okcredit.android.base.extensions.isConnectedToInternet
import javax.inject.Inject

class AccountChatTracker @Inject constructor(
    private val analyticsProvider: AnalyticsProvider,
    private val context: Lazy<Context>
) {

    object Key {
        const val UNREAD_COUNT = "Unread count"
        const val ACCOUNT_ID = "account_id"
        const val ROLE = "role"
        const val TYPE = "Type"
        const val LISTENER = "Listener"
        const val SCREEN = "Screen"
        const val RELATION = "Relation"
    }

    object Event {
        const val CHAT_TOOLTIP_SHOWN = "Chat Tooltip Shown"
        const val MESSAGE_SEEN = "Message Seen"
        const val PAGE_VIEWED = "Page Viewed"
        const val MESSAGE_RECEIVED = "Message Received"
        const val MESSAGE_START = "Message Start"
        const val MESSAGE_SENT = "Message Sent"
        const val SHARED = "Shared"
    }

    object Values {
        const val ONLINE = "Online"
        const val CHAT_SCREEN = "Chat"
        const val SUPPLIER = "Supplier"
        const val CUSTOMER = "Customer"
        const val OFFLINE = "Offline"
    }

    private fun addChatSuperProperties(properties: MutableMap<String, Any>) {
        if (context.get().isConnectedToInternet()) {
            properties[Key.TYPE] = Values.ONLINE
        } else {
            properties[Key.TYPE] = Values.OFFLINE
        }
    }

    private fun addMessageProperties(message: Message, properties: MutableMap<String, Any>) {
        properties[FIELDS.ACCOUNT_ID] = message.account_id.toString()
        properties[FIELDS.APP_CREATE_TIME] = message.app_create_time.toString()
        properties[FIELDS.FIRST_DELIVERED_TIME] = message.first_delivered_time.toString()
        properties[FIELDS.FIRST_SEEN_TIME] = message.first_seen_time.toString()
        properties[FIELDS.MESSAGE] = message.message.toString()
        properties[FIELDS.METAINFO_RECEIVER_NAME] = message.metaInfo?.receiverName.toString()
        properties[FIELDS.METAINFO_RECEIVER_ROLE] = message.metaInfo?.receiverRole.toString()
        properties[FIELDS.METAINFO_SENDER_NAME] = message.metaInfo?.senderName.toString()
        properties[FIELDS.ORDER_FOR_ME] = message.order_for_me.toString()
        properties[FIELDS.SENT_BY_ME] = message.sent_by_me.toString()
        properties[FIELDS.SERVER_CREATE_TIME] = message.server_create_time.toString()
        properties[FIELDS.STATUS] = message.status.toString()
        properties[FIELDS.VERSION] = message.version.toString()
        properties[FIELDS.MESSAGE_ID] = message.message_id.toString()
    }

    fun trackMessageStart(accountId: String?, role: String?, receiverRole: String?, chatScreen: String) {
        val properties = mutableMapOf<String, Any>().apply {
            this[Key.ACCOUNT_ID] = accountId ?: ""
            if (!role.isNullOrBlank()) this[Key.ROLE] = role
        }
        properties[Key.SCREEN] = Values.CHAT_SCREEN
        addChatSuperProperties(properties)
        if (properties.containsKey(Key.ROLE)) {
            if (properties[Key.ROLE] == STRING_CONSTANTS.SELLER)
                properties[Key.RELATION] = Values.CUSTOMER
            if (properties[Key.ROLE] == STRING_CONSTANTS.BUYER)
                properties[Key.RELATION] = Values.SUPPLIER
        }
        analyticsProvider.trackEvents(Event.MESSAGE_START, properties)
    }

    fun trackInviteClicked(
        accountId: String?,
        screen: String,
        role: String?
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!accountId.isNullOrBlank()) properties.put(Key.ACCOUNT_ID, accountId)
        if (!role.isNullOrBlank()) properties.put(Key.ROLE, role)
        properties.put(Key.SCREEN, screen)
        addChatSuperProperties(properties)
        if (properties.containsKey(Key.ROLE)) {
            if (properties[Key.ROLE] == STRING_CONSTANTS.SELLER)
                properties[Key.RELATION] = Values.CUSTOMER
            if (properties[Key.ROLE] == STRING_CONSTANTS.BUYER)
                properties[Key.RELATION] = Values.SUPPLIER
        }
        analyticsProvider.trackEvents(Event.SHARED, properties)
    }

    fun trackPageViewed(chatScreen: String, accountId: String?, unreadMesssageCount: String?, role: String?) {
        val properties = mutableMapOf<String, Any>()
        properties[Key.SCREEN] = Values.CHAT_SCREEN
        if (!role.isNullOrBlank()) properties.put(Key.ROLE, role)
        if (!accountId.isNullOrBlank()) properties.put(Key.ACCOUNT_ID, accountId)
        if (!unreadMesssageCount.isNullOrBlank()) properties.put(Key.UNREAD_COUNT, unreadMesssageCount)
        addChatSuperProperties(properties)
        if (properties.containsKey(Key.ROLE)) {
            if (properties[Key.ROLE] == STRING_CONSTANTS.SELLER)
                properties[Key.RELATION] = Values.CUSTOMER
            if (properties[Key.ROLE] == STRING_CONSTANTS.BUYER)
                properties[Key.RELATION] = Values.SUPPLIER
        }
        analyticsProvider.trackEvents(Event.PAGE_VIEWED, properties)
    }

    fun trackMessageSent(accountId: String?, role: String?, message: Message) {
        val properties = mutableMapOf<String, Any>().apply {
            this[Key.ACCOUNT_ID] = accountId ?: ""
        }
        properties[Key.SCREEN] = Values.CHAT_SCREEN
        if (!role.isNullOrBlank()) properties.put(Key.ROLE, role)
        addMessageProperties(message, properties)
        addChatSuperProperties(properties)
        if (properties.containsKey(Key.ROLE)) {
            if (properties[Key.ROLE] == STRING_CONSTANTS.SELLER)
                properties[Key.RELATION] = Values.SUPPLIER
            if (properties[Key.ROLE] == STRING_CONSTANTS.BUYER)
                properties[Key.RELATION] = Values.CUSTOMER
        }
        analyticsProvider.trackEvents(Event.MESSAGE_SENT, properties)
    }

    fun trackTipShown(accountId: String?, screen: String, role: String?) {
        val properties = mutableMapOf<String, Any>()
        if (!accountId.isNullOrBlank()) properties.put(Key.ACCOUNT_ID, accountId)
        if (!role.isNullOrBlank()) properties.put(Key.ROLE, role)
        properties.put(Key.SCREEN, screen)
        addChatSuperProperties(properties)
        if (properties.containsKey(Key.ROLE)) {
            if (properties[Key.ROLE] == STRING_CONSTANTS.SELLER)
                properties[Key.RELATION] = Values.CUSTOMER
            if (properties[Key.ROLE] == STRING_CONSTANTS.BUYER)
                properties[Key.RELATION] = Values.SUPPLIER
        }
        analyticsProvider.trackEvents(Event.CHAT_TOOLTIP_SHOWN, properties)
    }
}
