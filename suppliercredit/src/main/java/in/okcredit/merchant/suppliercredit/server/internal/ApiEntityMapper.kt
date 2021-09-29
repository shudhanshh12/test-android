package `in`.okcredit.merchant.suppliercredit.server.internal

import `in`.okcredit.merchant.suppliercredit.AccountMetaInfo
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.Transaction
import `in`.okcredit.merchant.suppliercredit.store.database.NotificationReminder
import `in`.okcredit.shared.utils.CommonUtils.currentDateTime
import com.google.common.base.Converter

object ApiEntityMapper {

    val ACCOUNT_META_INFO: Converter<ApiMessages.FeatureFindResponse, AccountMetaInfo> =
        object : Converter<ApiMessages.FeatureFindResponse, AccountMetaInfo>() {
            override fun doForward(apiEntity: ApiMessages.FeatureFindResponse): AccountMetaInfo {
                val list = apiEntity.results
                return AccountMetaInfo(
                    list.filter { featureFindResult -> featureFindResult.feature_enabled && featureFindResult.customer_id != null }
                        .map { featureFindResult -> featureFindResult.customer_id!! },
                    list.filter { featureFindResult -> featureFindResult.restrict_customer_txn_enabled && featureFindResult.customer_id != null }
                        .map { featureFindResult -> featureFindResult.customer_id!! },
                    list.filter { featureFindResult -> featureFindResult.add_transaction_restricted && featureFindResult.customer_id != null }
                        .map { featureFindResult -> featureFindResult.customer_id!! }
                )
            }

            override fun doBackward(accountMetaInfo: AccountMetaInfo): ApiMessages.FeatureFindResponse {
                throw RuntimeException("illegal operation: cannot convert accountMetaInfo domain entity to api entity")
            }
        }

    var SUPPLIER: Converter<ApiMessages.Supplier, Supplier> = object : Converter<ApiMessages.Supplier, Supplier>() {
        override fun doForward(apiEntity: ApiMessages.Supplier): Supplier {

            return Supplier(
                apiEntity.id,
                apiEntity.registered,
                apiEntity.deleted,
                apiEntity.create_time,
                apiEntity.txn_start_time,
                apiEntity.name,
                apiEntity.mobile,
                apiEntity.address,
                apiEntity.profile_image,
                apiEntity.balance,
                0,
                null,
                null,
                apiEntity.txn_alert_enabled,
                apiEntity.lang,
                true,
                null,
                apiEntity.add_transaction_restricted,
                apiEntity.state,
                apiEntity.blocked_by_supplier,
                apiEntity.restrict_contact_sync
            )
        }

        override fun doBackward(b: Supplier): ApiMessages.Supplier {
            throw RuntimeException("illegal operation: cannot convert Supplier domain entity to api entity")
        }
    }

    var TRANSACTION: Converter<ApiMessages.Transaction, Transaction> =
        object : Converter<ApiMessages.Transaction, Transaction>() {
            override fun doForward(apiEntity: ApiMessages.Transaction): Transaction {

                return Transaction(
                    id = apiEntity.id,
                    supplierId = apiEntity.supplier_id,
                    collectionId = apiEntity.collection_id,
                    payment = apiEntity.payment,
                    amount = apiEntity.amount,
                    note = apiEntity.note,
                    receiptUrl = apiEntity.receipt_url,
                    billDate = apiEntity.bill_date,
                    createTime = apiEntity.create_time,
                    createdBySupplier = apiEntity.created_by_supplier,
                    deleted = apiEntity.deleted,
                    deleteTime = apiEntity.delete_time,
                    deletedBySupplier = apiEntity.deleted_by_supplier,
                    updateTime = apiEntity.update_time,
                    syncing = true,
                    lastSyncTime = currentDateTime(),
                    transactionState = apiEntity.transaction_state,
                    tx_category = apiEntity.tx_category ?: -1
                )
            }

            override fun doBackward(b: Transaction): ApiMessages.Transaction {
                throw RuntimeException("illegal operation: cannot convert txn domain entity to api entity")
            }
        }

    enum class NotificationReminderStatus(val status: Int) {
        NO_ACTION(-1),
        DISMISSED(0),
        PAYNOW(1),
    }

    private fun ApiMessages.NotificationReminders.toEntityMapper(businessId: String): NotificationReminder =
        NotificationReminder(
            id = this.id,
            accountId = this.accountId,
            createdAt = this.createdAt,
            expiresAt = this.expiresAt,
            status = NotificationReminderStatus.NO_ACTION.status,
            businessId = businessId
        )

    fun List<ApiMessages.NotificationReminders>.toEntityMapper(businessId: String): List<NotificationReminder> =
        this.map { it.toEntityMapper(businessId) }
}
