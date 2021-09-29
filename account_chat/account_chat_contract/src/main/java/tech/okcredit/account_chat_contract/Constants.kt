package tech.okcredit.account_chat_contract

object FEATURE {
    const val FEATURE_ACCOUNT_CHATS = "accounts_chat"
    const val CHAT_TOOTTIP = "chat_tooltip"
}

object COLLECTIONS {
    const val MERCHANTS = "merchants"
    const val MESSAGES = "messages"
}

object CHAT_INTENT_EXTRAS {
    const val FIRST_UNSEEN_MESSAGE_ID = "first_unseen_message_id"
    const val UNREAD_MESSAGE_COUNT = "unread_message_count"
    const val ROLE = "account_role"
    const val ACCOUNT_ID = "account_id"
    const val MESSAGE_ID = FIELDS.MESSAGE_ID
}

object FIELDS {
    const val METAINFO_SENT_SOUND_PLAYED = "metaInfo.sentSoundPlayed"
    const val STATUS = "status"
    const val SERVER_CREATE_TIME = "server_create_time"
    const val METAINFO_SENDER_NAME = "metaInfo.senderName"
    const val METAINFO_RECEIVER_ROLE = "metaInfo.receiverRole"
    const val METAINFO_RECEIVER_NAME = "metaInfo.receiverName"
    const val MESSAGE = "message"
    const val MESSAGE_ID = "message_id"
    const val APP_CREATE_TIME = "app_create_time"
    const val VERSION = "version"
    const val FIRST_DELIVERED_TIME = "first_delivered_time"
    const val ACCOUNT_ID = CHAT_INTENT_EXTRAS.ACCOUNT_ID
    const val SENT_BY_ME = "sent_by_me"
    const val FIRST_SEEN_TIME = "first_seen_time"
    const val ORDER_FOR_ME = "order_for_me"
}

object STRING_CONSTANTS {
    const val OFFLINE = "offline"
    const val ONLINE = "online"
    const val SELLER = "SELLER"
    const val BUYER = "BUYER"
    const val CUSTOMER = "Customer"
    const val SUPPLIER = "Supplier"
}

object NOTIFICATION_CONSTANTS {
    const val BASE_URL = "okcredit://merchant/v1/chat/"
    const val SINGLE_SLASH = "/"
    const val TRUE = "true"
    const val CHAT = "Chat"
    const val IN_APP = "Supplier"
}

object RDB {
    const val CONNECTED_REF_PATH = ".info/connected"
}
