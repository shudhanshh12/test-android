package tech.okcredit.sdk

import `in`.okcredit.merchant.suppliercredit.utils.Utils
import com.google.common.base.Converter
import tech.okcredit.sdk.models.BillWithDocs
import tech.okcredit.sdk.store.database.DBBill
import tech.okcredit.sdk.store.database.DbBillDoc
import tech.okcredit.sdk.store.database.LocalBill
import tech.okcredit.sdk.store.database.LocalBillDoc
import tech.okcredit.sdk.store.database.TxnType

object DbEntityMapper {

    fun getBillsWithDoc(businessId: String): Converter<BillWithDocs, LocalBill> =
        object : Converter<BillWithDocs, LocalBill>() {
            override fun doForward(billWithDocs: BillWithDocs): LocalBill {
                return LocalBill(
                    id = billWithDocs.dbBill.id,
                    localBillDocList = Utils.mapList(billWithDocs.billDocList, getDocs(businessId)),
                    createdAt = billWithDocs.dbBill.createdAt,
                    note = billWithDocs.dbBill.note,
                    createdByMe = billWithDocs.dbBill.createdByMe,
                    amount = billWithDocs.dbBill.amount,
                    billDate = billWithDocs.dbBill.billDate,
                    txnType = when (billWithDocs.dbBill.txnType) {
                        1 -> TxnType.CREDIT
                        2 -> TxnType.PAYMENT
                        else -> TxnType.UNKNOWN
                    },
                    deleted = billWithDocs.dbBill.deleted,
                    transactionId = billWithDocs.dbBill.transactionId,
                    updatedAt = billWithDocs.dbBill.updatedAt,
                    deletedAt = billWithDocs.dbBill.deletedAt,
                )
            }

            override fun doBackward(b: LocalBill): BillWithDocs {
                TODO("Not yet implemented")
            }
        }

    fun getBills(businessId: String): Converter<LocalBill, DBBill> = object : Converter<LocalBill, DBBill>() {
        override fun doForward(localBill: LocalBill): DBBill {
            return DBBill(
                id = localBill.id,
                transactionId = localBill.transactionId,
                createdByMe = localBill.createdByMe,
                createdAt = localBill.createdAt,
                accountId = localBill.accountId,
                billDate = localBill.billDate,
                note = localBill.note,
                amount = localBill.amount,
                txnType = localBill.txnType?.type ?: 0,
                deleted = localBill.deleted,
                updatedAt = localBill.updatedAt,
                deletedAt = localBill.deletedAt,
                businessId = businessId
            )
        }

        override fun doBackward(b: DBBill): LocalBill {
            TODO("Not yet implemented")
        }
    }

    fun getDocs(businessId: String): Converter<DbBillDoc, LocalBillDoc> =
        object : Converter<DbBillDoc, LocalBillDoc>() {
            override fun doForward(billDoc: DbBillDoc): LocalBillDoc {
                return LocalBillDoc(
                    billDocId = billDoc.id,
                    url = billDoc.url,
                    billId = billDoc.billId,
                    createdAt = billDoc.createdAt,
                    updatedAt = billDoc.updatedAt,
                    deletedAt = billDoc.deletedAt,
                )
            }

            override fun doBackward(billDoc: LocalBillDoc): DbBillDoc {
                return DbBillDoc(
                    id = billDoc.billDocId,
                    createdAt = billDoc.createdAt,
                    url = billDoc.url,
                    billId = billDoc.billId,
                    updatedAt = billDoc.updatedAt,
                    deletedAt = billDoc.deletedAt,
                    businessId = businessId
                )
            }
        }
}
