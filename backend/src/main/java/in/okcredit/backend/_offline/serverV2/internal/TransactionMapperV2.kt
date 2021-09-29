package `in`.okcredit.backend._offline.serverV2.internal

import `in`.okcredit.backend._offline.database.internal.DbEntities
import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import tech.okcredit.base.exceptions.ExceptionUtils.Companion.logException

fun ApiMessagesV2.Transaction.toDomainModel(businessId: String): DbEntities.Transaction {
    val dbTransaction = DbEntities.Transaction()

    dbTransaction.id = id
    dbTransaction.type = type
    dbTransaction.customerId = account_id
    dbTransaction.collectionId = collection_id
    dbTransaction.amount = 0F
    dbTransaction.amountV2 = amount
    dbTransaction.receiptUrl = ConvertTransactionImageV2ListToTransactionImageV2String(this.images)
    dbTransaction.note = note
    dbTransaction.createdAt = DateTime((create_time ?: 0L) * 1000)
    dbTransaction.isOnboarding = false
    dbTransaction.isDeleted = deleted
    dbTransaction.deleteTime = DateTime((delete_time ?: 0) * 1000)
    dbTransaction.isDirty = false
    dbTransaction.billDate = DateTime((bill_date ?: 0) * 1000)
    dbTransaction.updatedAt = DateTime(update_time * 1000)
    dbTransaction.smsSent = alert_sent_by_creator
    dbTransaction.transactionState = transaction_state
    dbTransaction.createdByCustomer = creator_role == ApiMessagesV2.ROLE_BUYER
    dbTransaction.deletedByCustomer = deleter_role == ApiMessagesV2.ROLE_BUYER
    dbTransaction.transactionCategory = tx_category
    dbTransaction.businessId = businessId
    return dbTransaction
}

fun ConvertTransactionImageV2ListToTransactionImageV2String(transactionImages: List<ApiMessagesV2.TransactionImageV2>?): String? {
    val jsonArray = JSONArray()
    if (transactionImages != null) {
        for (transactionImage in transactionImages) {
            val imageJSON = JSONObject()
            try {
                imageJSON.put("id", transactionImage.id)
                imageJSON.put("request_id", transactionImage.request_id)
                imageJSON.put("transaction_id", transactionImage.transaction_id)
                imageJSON.put("url", transactionImage.url)
                imageJSON.put("create_time", transactionImage.create_time * 1000)
            } catch (e: JSONException) {
                logException("Error: ConvertTransactionImageV2 JSONException", JSONException(imageJSON.toString()))
            }
            jsonArray.put(imageJSON)
        }
        return jsonArray.toString()
    }
    return null
}
