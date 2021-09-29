package tech.okcredit.account_chat_sdk.use_cases

import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import io.reactivex.Observable
import org.joda.time.DateTime
import tech.okcredit.account_chat_contract.FIELDS
import tech.okcredit.account_chat_sdk.ChatProvider
import tech.okcredit.account_chat_sdk.models.Message
import tech.okcredit.account_chat_sdk.models.MetaInfo
import java.util.*
import javax.inject.Inject

class SendChatMessage @Inject constructor(private val getActiveBusiness: GetActiveBusiness) :
    UseCase<SendChatMessage.Request, Message> {

    data class Request(
        val message: String,
        val accountID: String?,
        val merchantId: String?,
        val role: String?,
        val receiverRole: String?,
        val accountName: String?
    )

    override fun execute(req: Request): Observable<Result<Message>> {
        return getActiveBusiness.execute().firstOrError().flatMapObservable { business ->
            Observable.create<Result<Message>> { emitter ->
                if (req.accountID == null) {
                    emitter.onNext(Result.Failure(NullPointerException("account id is null")))
                }
                ChatProvider.provideMessagesCollectionPath(business.id).whereEqualTo(FIELDS.ACCOUNT_ID, req.accountID)
                    .whereEqualTo(FIELDS.SENT_BY_ME, true)
                    .orderBy(FIELDS.APP_CREATE_TIME, Query.Direction.DESCENDING).limit(1).get(Source.CACHE)
                    .addOnSuccessListener {
                        var version: Long = 0
                        if (it.isEmpty.not()) {
                            version = (it.documents[0].toObject(Message::class.java) as Message).version + 1
                        }
                        val messageID = UUID.randomUUID().toString()
                        val currentTime = DateTime.now().millis.toString()
                        val friendlyMessage =
                            Message(
                                message_id = messageID,
                                account_id = req.accountID,
                                message = req.message,
                                sent_by_me = true,
                                status = "UNPROCESSED",
                                app_create_time = currentTime,
                                server_create_time = null,
                                first_delivered_time = null,
                                first_seen_time = null,
                                order_for_me = currentTime,
                                metaInfo = MetaInfo(
                                    req.accountName,
                                    req.receiverRole,
                                    business.name,
                                    false
                                ),
                                version = version
                            )
                        ChatProvider.provideMessagesCollectionPath(business.id).document(messageID).set(friendlyMessage)
                        emitter.onNext(Result.Success(friendlyMessage))
                    }
            }
        }
    }
}
