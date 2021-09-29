package tech.okcredit.account_chat_sdk.models

data class Message(
    var message_id: String? = null,
    var account_id: String? = null,
    val message: String? = null,
    val sent_by_me: Boolean = false,
    val status: String? = null,
    val app_create_time: String? = null,
    val server_create_time: String? = null,
    val first_delivered_time: String? = null,
    val first_seen_time: String? = null,
    val order_for_me: String? = null,
    val metaInfo: MetaInfo? = null,
    val version: Long = 0
)

data class MetaInfo(
    val receiverName: String? = null,
    var receiverRole: String? = null,
    var senderName: String? = null,
    var sentSoundPlayed: Boolean = false
)
