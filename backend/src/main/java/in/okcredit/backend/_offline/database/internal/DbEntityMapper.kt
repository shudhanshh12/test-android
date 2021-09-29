package `in`.okcredit.backend._offline.database.internal

import `in`.okcredit.backend._offline.model.DueInfo
import `in`.okcredit.backend._offline.model.TransactionImageAdapter
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.common.toTimestamp
import `in`.okcredit.merchant.core.model.Customer.State.Companion.getState
import com.google.common.base.Converter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import merchant.okcredit.accounting.model.Transaction
import merchant.okcredit.accounting.model.TransactionImage
import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import tech.okcredit.base.exceptions.ExceptionUtils.Companion.logException
import java.util.*
import `in`.okcredit.merchant.core.model.Customer as CoreCustomer

object DbEntityMapper {

    fun CUSTOMER(businessId: String): Converter<Customer, DbEntities.Customer> =
        object : Converter<Customer, DbEntities.Customer>() {
            override fun doForward(customer: Customer): DbEntities.Customer {
                val dbEntity = DbEntities.Customer()
                dbEntity.id = customer.id!!
                dbEntity.status = customer.status
                dbEntity.mobile = customer.mobile
                dbEntity.description = customer.description
                dbEntity.createdAt = customer.createdAt
                dbEntity.txnStartTime = customer.txnStartTime
                dbEntity.balanceV2 = customer.balanceV2
                dbEntity.transactionCount = customer.transactionCount
                dbEntity.lastActivity = customer.lastActivity
                dbEntity.lastPayment = customer.lastPayment
                dbEntity.accountUrl = customer.accountUrl
                dbEntity.profileImage = customer.profileImage
                dbEntity.address = customer.address
                dbEntity.email = customer.email
                dbEntity.lang = customer.lang
                dbEntity.txnAlertEnabled = customer.isTxnAlertEnabled()
                dbEntity.newActivityCount = customer.newActivityCount
                dbEntity.lastViewTime = customer.lastViewTime
                dbEntity.registered = customer.isRegistered()
                dbEntity.lastBillDate = customer.getLastBillDate()
                dbEntity.reminderMode = customer.reminderMode
                dbEntity.addTransactionRestricted = customer.isAddTransactionPermissionDenied()
                dbEntity.isLiveSales = customer.isLiveSales
                dbEntity.state =
                    if (customer.state === Customer.State.BLOCKED) Customer.State.BLOCKED.value else Customer.State.ACTIVE.value
                dbEntity.blockedByCustomer = customer.isBlockedByCustomer()
                dbEntity.restrictContactSync = customer.isRestrictContactSync()
                dbEntity.businessId = businessId
                return dbEntity
            }

            override fun doBackward(dbEntity: DbEntities.Customer): Customer {
                var balanceV2 = dbEntity.balanceV2
                if (dbEntity.balance != 0f && dbEntity.balanceV2 == 0L) {
                    balanceV2 = (dbEntity.balance * 100).toLong()
                }
                return Customer(
                    id = dbEntity.id,
                    status = dbEntity.status,
                    mobile = dbEntity.mobile,
                    description = dbEntity.description,
                    createdAt = dbEntity.createdAt,
                    txnStartTime = dbEntity.txnStartTime,
                    balanceV2 = balanceV2,
                    transactionCount = dbEntity.transactionCount,
                    lastActivity = dbEntity.lastActivity,
                    lastPayment = dbEntity.lastPayment,
                    accountUrl = dbEntity.accountUrl,
                    profileImage = dbEntity.profileImage,
                    address = dbEntity.address,
                    email = dbEntity.email,
                    newActivityCount = dbEntity.newActivityCount,
                    lastViewTime = dbEntity.lastViewTime,
                    registered = dbEntity.registered,
                    lastBillDate = dbEntity.lastBillDate,
                    txnAlertEnabled = dbEntity.txnAlertEnabled,
                    lang = dbEntity.lang,
                    reminderMode = dbEntity.reminderMode,
                    isLiveSales = dbEntity.isLiveSales,
                    addTransactionPermissionDenied = dbEntity.addTransactionRestricted,
                    state = if (dbEntity.state == Customer.State.BLOCKED.value) Customer.State.BLOCKED else Customer.State.ACTIVE,
                    blockedByCustomer = dbEntity.blockedByCustomer,
                    restrictContactSync = dbEntity.restrictContactSync,
                    lastReminderSendTime = dbEntity.lastReminderSendTime,
                )
            }
        }

    fun TRANSACTION(businessId: String): Converter<Transaction, DbEntities.Transaction> =
        object : Converter<Transaction, DbEntities.Transaction>() {
            override fun doForward(transaction: Transaction): DbEntities.Transaction {
                val dbEntity = DbEntities.Transaction()
                dbEntity.id = transaction.id
                dbEntity.type = transaction.type
                dbEntity.customerId = transaction.customerId
                dbEntity.collectionId = transaction.collectionId
                dbEntity.amountV2 = transaction.amountV2
                dbEntity.receiptUrl = convertTransactionImageListToTransactionImageString(transaction.receiptUrl)
                dbEntity.note = transaction.note
                dbEntity.createdAt = transaction.createdAt
                dbEntity.isOnboarding = transaction.isOnboarding
                dbEntity.isDeleted = transaction.isDeleted
                dbEntity.deleteTime = transaction.deleteTime
                dbEntity.isDirty = transaction.isDirty
                dbEntity.billDate = transaction.billDate
                dbEntity.smsSent = transaction.isSmsSent
                dbEntity.inputType = transaction.inputType
                dbEntity.voiceId = transaction.voiceId
                dbEntity.createdByCustomer = transaction.isCreatedByCustomer
                dbEntity.deletedByCustomer = transaction.isDeletedByCustomer
                dbEntity.transactionState = transaction.transactionState
                dbEntity.transactionCategory = transaction.transactionCategory
                dbEntity.amountUpdated = transaction.amountUpdated
                dbEntity.businessId = businessId
                return dbEntity
            }

            override fun doBackward(dbEntity: DbEntities.Transaction): Transaction {

                // billDate and updatedAt added on version 1.19.0. setting default value here
                var billDate = dbEntity.billDate
                var updatedAt = dbEntity.updatedAt
                if (billDate == null) {
                    billDate = dbEntity.createdAt
                }
                if (updatedAt == null) {
                    updatedAt = if (dbEntity.isDeleted && dbEntity.deleteTime != null) {
                        dbEntity.deleteTime
                    } else {
                        dbEntity.createdAt
                    }
                }
                var amountV2 = dbEntity.amountV2
                if (dbEntity.amount != 0f && dbEntity.amountV2 == 0L) {
                    amountV2 = (dbEntity.amount * 100).toLong()
                }
                return Transaction(
                    dbEntity.id,
                    dbEntity.type,
                    dbEntity.customerId,
                    dbEntity.collectionId,
                    amountV2,
                    convertTransactionImageStringToTransactionImageList(dbEntity.receiptUrl, dbEntity.id) ?: listOf(),
                    dbEntity.note,
                    dbEntity.createdAt,
                    dbEntity.isOnboarding,
                    dbEntity.isDeleted,
                    dbEntity.deleteTime,
                    dbEntity.isDirty,
                    billDate,
                    updatedAt,
                    dbEntity.smsSent,
                    dbEntity.createdByCustomer,
                    dbEntity.deletedByCustomer,
                    dbEntity.inputType, dbEntity.voiceId,
                    dbEntity.transactionState,
                    dbEntity.transactionCategory,
                    dbEntity.amountUpdated,
                    dbEntity.amountUpdatedAt
                )
            }
        }

    fun DUE_INFO(businessId: String): Converter<DbEntities.DueInfo, DueInfo> =
        object : Converter<DbEntities.DueInfo, DueInfo>() {
            override fun doForward(dueInfo: DbEntities.DueInfo): DueInfo {
                return DueInfo(
                    dueInfo.customerId,
                    dueInfo.is_due_active,
                    dueInfo.active_date,
                    dueInfo.is_custom_date_set,
                    dueInfo.is_auto_generated
                )
            }

            override fun doBackward(dueInfo: DueInfo): DbEntities.DueInfo {
                val dbDue = DbEntities.DueInfo()
                dbDue.is_custom_date_set = dueInfo.isCustomDateSet
                dbDue.is_due_active = dueInfo.isDueActive
                dbDue.active_date = dueInfo.activeDate
                dbDue.customerId = dueInfo.customerId
                dbDue.is_auto_generated = dueInfo.isAutoGenerated
                dbDue.businessId = businessId
                return dbDue
            }
        }

    fun DUE_INFO_API_AND_DB_CONVERTER(businessId: String): Converter<`in`.okcredit.backend._offline.server.internal.DueInfo, DbEntities.DueInfo> =
        object : Converter<`in`.okcredit.backend._offline.server.internal.DueInfo, DbEntities.DueInfo>() {
            override fun doForward(dueInfo: `in`.okcredit.backend._offline.server.internal.DueInfo?): DbEntities.DueInfo {
                val dbDue = DbEntities.DueInfo()
                dbDue.is_custom_date_set = dueInfo!!.isCustomDateSet
                dbDue.is_due_active = dueInfo.isDueActive()
                dbDue.active_date = dueInfo.getActiveDate()
                dbDue.customerId = dueInfo.getCustomerId()
                dbDue.is_auto_generated = dueInfo.isAutoGenerated
                dbDue.businessId = businessId
                return dbDue
            }

            override fun doBackward(dueInfo: DbEntities.DueInfo): `in`.okcredit.backend._offline.server.internal.DueInfo? {
                return null
            }
        }

    fun CustomerWithTransactionView(businessId: String): Converter<CustomerWithTransactionsInfo, Customer> =
        object : Converter<CustomerWithTransactionsInfo, Customer>() {
            override fun doForward(dbEntity: CustomerWithTransactionsInfo?): Customer {
                var balanceV2 = dbEntity!!.balanceV2
                if (dbEntity.balance != 0L && dbEntity.balanceV2 == 0L) {
                    balanceV2 = dbEntity.balance * 100
                }
                return Customer(
                    id = dbEntity.id,
                    status = dbEntity.status,
                    mobile = dbEntity.mobile,
                    description = dbEntity.description,
                    createdAt = dbEntity.createdAt,
                    txnStartTime = dbEntity.txnStartTime,
                    balanceV2 = balanceV2,
                    transactionCount = dbEntity.transactionCount,
                    lastActivity = dbEntity.lastActivity,
                    lastPayment = dbEntity.lastPayment,
                    accountUrl = dbEntity.accountUrl,
                    profileImage = dbEntity.profileImage,
                    address = dbEntity.address,
                    email = dbEntity.email,
                    newActivityCount = dbEntity.newActivityCount,
                    lastViewTime = dbEntity.lastViewTime,
                    registered = dbEntity.registered,
                    lastBillDate = dbEntity.lastBillDate,
                    txnAlertEnabled = dbEntity.txnAlertEnabled,
                    lang = dbEntity.lang,
                    reminderMode = dbEntity.reminderMode,
                    isLiveSales = dbEntity.isLiveSales,
                    addTransactionPermissionDenied = dbEntity.addTransactionRestricted,
                    state = if (dbEntity.state == Customer.State.BLOCKED.value) Customer.State.BLOCKED else Customer.State.ACTIVE,
                    blockedByCustomer = dbEntity.blockedByCustomer,
                    restrictContactSync = dbEntity.restrictContactSync,
                    dueActive = dbEntity.isDueActive,
                    dueInfo_activeDate = dbEntity.activeDate,
                    customDateSet = dbEntity.isCustomDateSet,
                    lastActivityMetaInfo = dbEntity.lastActivityMetaInfo,
                    lastAmount = dbEntity.lastAmount,
                    lastReminderSendTime = dbEntity.lastReminderSendTime ?: DateTime(0),
                )
            }

            override fun doBackward(customer: Customer): CustomerWithTransactionsInfo? {
                return null
            }
        }

    // todo review this by mohitesh (balance and toTimeStamp)
    val CustomerWithTransaction_CoreCustomer = object : Converter<CustomerWithTransactionsInfo, CoreCustomer>() {
        override fun doForward(dbEntity: CustomerWithTransactionsInfo?): CoreCustomer {
            if (dbEntity == null) {
                throw IllegalStateException("DB entity is null")
            }
            val state: Int =
                if (dbEntity.state == Customer.State.BLOCKED.value)
                    Customer.State.BLOCKED.value else Customer.State.ACTIVE.value
            return CoreCustomer(
                id = dbEntity.id,
                customerSyncStatus = Customer.CustomerSyncStatus.CLEAN.code,
                status = dbEntity.status,
                mobile = dbEntity.mobile,
                balance = dbEntity.balance,
                description = dbEntity.description,
                createdAt = dbEntity.createdAt.toTimeStamp(),
                txnStartTime = dbEntity.txnStartTime?.toTimestamp(),
                transactionCount = dbEntity.transactionCount,
                lastActivity = dbEntity.lastActivity?.toTimeStamp(),
                lastPayment = dbEntity.lastPayment?.toTimeStamp(),
                accountUrl = dbEntity.accountUrl,
                profileImage = dbEntity.profileImage,
                address = dbEntity.address,
                email = dbEntity.email,
                newActivityCount = dbEntity.newActivityCount,
                lastViewTime = dbEntity.lastViewTime?.toTimeStamp(),
                registered = dbEntity.registered,
                lastBillDate = dbEntity.lastBillDate?.toTimeStamp(),
                txnAlertEnabled = dbEntity.txnAlertEnabled,
                lang = dbEntity?.lang,
                reminderMode = dbEntity.reminderMode,
                isLiveSales = dbEntity.isLiveSales,
                addTransactionPermissionDenied = dbEntity.addTransactionRestricted,
                state = getState(state),
                blockedByCustomer = dbEntity.blockedByCustomer,
                restrictContactSync = dbEntity.restrictContactSync,
                lastActivityMetaInfo = dbEntity.lastActivityMetaInfo,
                lastAmount = dbEntity.lastAmount,
                lastReminderSendTime = (dbEntity.lastReminderSendTime ?: DateTime(0)).toTimeStamp()
            )
        }

        override fun doBackward(customer: CoreCustomer): CustomerWithTransactionsInfo? {
            return null
        }
    }

    fun DateTime.toTimeStamp() = Timestamp(this.millis * 1000)

    fun convertTransactionImageStringToTransactionImageList(
        receiptUrl: String?,
        txnID: String?,
    ): List<TransactionImage>? {
        return if (receiptUrl != null && !receiptUrl.isEmpty() && !receiptUrl.equals("[]", ignoreCase = true)) {
            if (receiptUrl.contains("request_id")) {
                val gson = Gson()
                val json = gson.toJson(receiptUrl)
                if (json.isEmpty()) {
                    return null
                }
                val type = object : TypeToken<ArrayList<TransactionImageAdapter>>() {}.type
                val transactionImages = ArrayList<TransactionImage>()
                val arrayList: ArrayList<TransactionImageAdapter> = gson.fromJson(receiptUrl, type)
                for (transactionImageAdapter in arrayList) {
                    transactionImages.add(
                        TransactionImage(
                            id = transactionImageAdapter.id,
                            request_id = transactionImageAdapter.request_id,
                            transaction_id = transactionImageAdapter.transaction_id,
                            url = transactionImageAdapter.url,
                            create_time = DateTime(transactionImageAdapter.create_time)
                        )
                    )
                }
                transactionImages.sortWith { o1, o2 -> o2.create_time.compareTo(o1.create_time) }
                transactionImages
            } else {
                val transactionImages = ArrayList<TransactionImage>()
                transactionImages.add(TransactionImage(txnID, txnID!!, txnID, receiptUrl, DateTime.now()))
                transactionImages
            }
        } else null
    }

    @JvmStatic
    fun convertTransactionImageListToTransactionImageString(transactionImages: List<TransactionImage>?): String? {
        val jsonArray = JSONArray()
        if (transactionImages != null) {
            for ((id, request_id, transaction_id, url, create_time) in transactionImages) {
                val imageJSON = JSONObject()
                try {
                    imageJSON.put("id", id)
                    imageJSON.put("request_id", request_id)
                    imageJSON.put("transaction_id", transaction_id)
                    imageJSON.put("url", url)
                    imageJSON.put("create_time", create_time.millis)
                } catch (e: JSONException) {
                    logException("Error: ConvertTransactionImage JSONException", e)
                }
                jsonArray.put(imageJSON)
            }
            return jsonArray.toString()
        }
        return null
    }
}
