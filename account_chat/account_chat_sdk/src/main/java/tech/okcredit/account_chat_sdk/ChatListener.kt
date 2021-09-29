package tech.okcredit.account_chat_sdk

import android.content.Context
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import dagger.Lazy
import org.joda.time.DateTime
import tech.okcredit.account_chat_contract.FIELDS
import tech.okcredit.account_chat_contract.NOTIFICATION_CONSTANTS
import tech.okcredit.account_chat_contract.STRING_CONSTANTS
import tech.okcredit.account_chat_sdk.models.Message
import tech.okcredit.account_chat_sdk.utils.ChatUtils
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.NotificationData
import java.util.concurrent.Executors
import javax.inject.Inject

class ChatListener @Inject constructor(
    private var communicationApi: Lazy<CommunicationRepository>,
    private val context: Lazy<Context>,
) : IChatListner {
    private var snapShotlistener: ListenerRegistration? = null
    private var shownMessageIds = mutableListOf<String>()
    override fun executeMessages(businessId: String) {
        if (snapShotlistener != null) {
            return
        }
        val query =
            ChatProvider.provideMessagesCollectionPath(businessId)
                .orderBy(
                    FIELDS.ORDER_FOR_ME,
                    Query.Direction.ASCENDING
                )
        snapShotlistener =
            query.addSnapshotListener(
                Executors.newCachedThreadPool(),
                EventListener { querySnapshot, firebaseFirestoreException ->
                    querySnapshot?.let {
                        var timeHacker = 0
                        for (i in it.documentChanges) {
                            val message = i.document.toObject(Message::class.java)
                            if (message.first_delivered_time == null &&
                                !message.sent_by_me &&
                                it.metadata.isFromCache.not()
                            ) {
                                val currentTime = DateTime.now().millis
                                if (DateTimeUtils.isTimeWithinLast10Second(message.app_create_time)) {
                                    if (activeAccountId.isNullOrEmpty() ||
                                        message.account_id != activeAccountId
                                    ) {
                                        if (shownMessageIds.contains(message.message_id).not()) {
                                            message.message_id?.let {
                                                shownMessageIds.add(it)
                                                setData(message, businessId)
                                            }
                                        }
                                    }
                                }
                                i.document.reference.update(
                                    FIELDS.FIRST_DELIVERED_TIME, (currentTime + timeHacker).toString(),
                                    FIELDS.ORDER_FOR_ME, (currentTime + timeHacker).toString()
                                )
                                if (activeAccountId != null &&
                                    message.account_id == activeAccountId &&
                                    DateTimeUtils.isTimeWithinLast10Second(message.app_create_time)
                                ) {
                                    ChatUtils.playReceivedSound(context.get())
                                    ChatUtils.provideHapticFeedback(context.get())
                                }
                                timeHacker++
                            }
                        }
                    }
                    firebaseFirestoreException?.let {
                        RecordException.recordException(it)
                    }
                }
            )
    }

    companion object {

        private var activeAccountId: String? = null

        fun setActiveAccountId(account: String?) {
            activeAccountId = account
        }
    }

    override fun removeListener() {
        snapShotlistener?.remove()
        snapShotlistener = null
        ChatProvider.clearCache()
    }

    private fun setData(
        message: Message,
        businessId: String
    ) {
        val receiverRole = message.metaInfo?.receiverRole
        val accountId = message.account_id
        val customerId = if (message.metaInfo?.receiverRole == STRING_CONSTANTS.SELLER) message.account_id else null
        val supplierId = if (message.metaInfo?.receiverRole == STRING_CONSTANTS.BUYER) message.account_id else null
        val subCampingId =
            if (message.metaInfo?.receiverRole == STRING_CONSTANTS.SELLER) STRING_CONSTANTS.CUSTOMER else if (message.metaInfo?.receiverRole == STRING_CONSTANTS.BUYER) STRING_CONSTANTS.SUPPLIER else null
        if (!message.sent_by_me) {
            val notification = NotificationData(
                title = message.metaInfo?.senderName,
                content = message.message,
                primaryAction = NOTIFICATION_CONSTANTS.BASE_URL + accountId + NOTIFICATION_CONSTANTS.SINGLE_SLASH + receiverRole + NOTIFICATION_CONSTANTS.SINGLE_SLASH + message.message_id,
                visible = NOTIFICATION_CONSTANTS.TRUE,
                customerId = customerId,
                supplierId = supplierId,
                campaignId = NOTIFICATION_CONSTANTS.CHAT,
                subCampaignId = subCampingId,
                segment = NOTIFICATION_CONSTANTS.IN_APP,
                expireTime = Long.MAX_VALUE.toString(),
                businessId = businessId,
            )
//            val moshi: Moshi = Moshi.Builder().build()
//            val jsonAdapter: JsonAdapter<NotificationData> = moshi.adapter(NotificationData::class.java)
//
//            val json = jsonAdapter.toJson(notification)
            val jsonString = Gson().toJson(notification)
            communicationApi.get().scheduleProcessingOkCreditNotification(jsonString)
        }
    }
}
