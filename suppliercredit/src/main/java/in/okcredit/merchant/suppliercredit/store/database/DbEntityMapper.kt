package `in`.okcredit.merchant.suppliercredit.store.database

import com.google.common.base.Converter

object DbEntityMapper {

    fun SUPPLIER(businessId: String): Converter<`in`.okcredit.merchant.suppliercredit.Supplier, Supplier> =
        object : Converter<`in`.okcredit.merchant.suppliercredit.Supplier, Supplier>() {
            override fun doForward(supplier: `in`.okcredit.merchant.suppliercredit.Supplier): Supplier {
                return Supplier(
                    supplier.id,
                    supplier.registered,
                    supplier.deleted,
                    supplier.createTime,
                    supplier.txnStartTime,
                    supplier.name,
                    supplier.mobile,
                    supplier.address,
                    supplier.profileImage,
                    supplier.balance,
                    supplier.newActivityCount,
                    supplier.lastActivityTime,
                    supplier.lastViewTime,
                    supplier.txnAlertEnabled,
                    supplier.lang,
                    supplier.syncing,
                    supplier.lastActivityTime,
                    supplier.addTransactionRestricted,
                    supplier.state,
                    supplier.blockedBySupplier,
                    supplier.restrictContactSync,
                    businessId
                )
            }

            override fun doBackward(dbEntity: Supplier): `in`.okcredit.merchant.suppliercredit.Supplier {
                return `in`.okcredit.merchant.suppliercredit.Supplier(
                    dbEntity.id,
                    dbEntity.registered,
                    dbEntity.deleted,
                    dbEntity.createTime,
                    dbEntity.txnStartTime,
                    dbEntity.name,
                    dbEntity.mobile,
                    dbEntity.address,
                    dbEntity.profileImage,
                    dbEntity.balance,
                    dbEntity.newActivityCount,
                    dbEntity.lastActivityTime,
                    dbEntity.lastViewTime,
                    dbEntity.txnAlertEnabled,
                    dbEntity.lang,
                    dbEntity.syncing,
                    dbEntity.lastSyncTime,
                    dbEntity.addTransactionRestricted,
                    dbEntity.state,
                    dbEntity.blockedBySupplier,
                    dbEntity.restrictContactSync
                )
            }
        }

    fun TRANSACTION(businessId: String): Converter<`in`.okcredit.merchant.suppliercredit.Transaction, Transaction> =
        object : Converter<`in`.okcredit.merchant.suppliercredit.Transaction, Transaction>() {
            override fun doForward(merchant: `in`.okcredit.merchant.suppliercredit.Transaction): Transaction {
                return Transaction(
                    id = merchant.id,
                    supplierId = merchant.supplierId,
                    collectionId = merchant.collectionId,
                    payment = merchant.payment,
                    amount = merchant.amount,
                    note = merchant.note,
                    receiptUrl = merchant.receiptUrl,
                    billDate = merchant.billDate,
                    createTime = merchant.createTime,
                    createdBySupplier = merchant.createdBySupplier,
                    deleted = merchant.deleted,
                    deleteTime = merchant.deleteTime,
                    deletedBySupplier = merchant.deletedBySupplier,
                    updateTime = merchant.updateTime,
                    syncing = merchant.syncing,
                    lastSyncTime = merchant.lastSyncTime,
                    transactionState = merchant.transactionState,
                    txCategory = merchant.tx_category,
                    businessId = businessId
                )
            }

            override fun doBackward(dbEntity: Transaction): `in`.okcredit.merchant.suppliercredit.Transaction {
                return `in`.okcredit.merchant.suppliercredit.Transaction(
                    id = dbEntity.id,
                    supplierId = dbEntity.supplierId,
                    collectionId = dbEntity.collectionId,
                    payment = dbEntity.payment,
                    amount = dbEntity.amount,
                    note = dbEntity.note,
                    receiptUrl = dbEntity.receiptUrl,
                    billDate = dbEntity.billDate,
                    createTime = dbEntity.createTime,
                    createdBySupplier = dbEntity.createdBySupplier,
                    deleted = dbEntity.deleted,
                    deleteTime = dbEntity.deleteTime,
                    deletedBySupplier = dbEntity.deletedBySupplier,
                    updateTime = dbEntity.updateTime,
                    syncing = dbEntity.syncing,
                    lastSyncTime = dbEntity.lastSyncTime,
                    transactionState = dbEntity.transactionState,
                    tx_category = dbEntity.txCategory
                )
            }
        }

    fun SUPPLIER_WITH_TXN_INFO(businessId: String): Converter<`in`.okcredit.merchant.suppliercredit.Supplier, SupplierWithTransactionsInfo> =
        object : Converter<`in`.okcredit.merchant.suppliercredit.Supplier, SupplierWithTransactionsInfo>() {
            override fun doForward(supplier: `in`.okcredit.merchant.suppliercredit.Supplier): SupplierWithTransactionsInfo {
                return SupplierWithTransactionsInfo(
                    supplier.id,
                    supplier.registered,
                    supplier.deleted,
                    supplier.createTime,
                    supplier.txnStartTime,
                    supplier.name,
                    supplier.mobile,
                    supplier.address,
                    supplier.profileImage,
                    supplier.balance,
                    supplier.newActivityCount,
                    supplier.lastActivityTime,
                    supplier.lastViewTime,
                    supplier.txnAlertEnabled,
                    supplier.lang,
                    supplier.syncing,
                    supplier.lastActivityTime,
                    supplier.addTransactionRestricted,
                    supplier.state,
                    supplier.blockedBySupplier,
                    supplier.restrictContactSync,
                    businessId
                )
            }

            override fun doBackward(dbEntity: SupplierWithTransactionsInfo): `in`.okcredit.merchant.suppliercredit.Supplier {
                return `in`.okcredit.merchant.suppliercredit.Supplier(
                    dbEntity.id,
                    dbEntity.registered,
                    dbEntity.deleted,
                    dbEntity.createTime,
                    dbEntity.txnStartTime,
                    dbEntity.name,
                    dbEntity.mobile,
                    dbEntity.address,
                    dbEntity.profileImage,
                    dbEntity.balance,
                    dbEntity.newActivityCount,
                    dbEntity.lastActivityTime,
                    dbEntity.lastViewTime,
                    dbEntity.txnAlertEnabled,
                    dbEntity.lang,
                    dbEntity.syncing,
                    dbEntity.lastSyncTime,
                    dbEntity.addTransactionRestricted,
                    dbEntity.state,
                    dbEntity.blockedBySupplier,
                    dbEntity.restrictContactSync
                )
            }
        }
}
