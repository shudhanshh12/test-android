package tech.okcredit.sdk

import com.google.common.base.Converter
import tech.okcredit.sdk.server.BillApiMessages
import tech.okcredit.sdk.store.database.LocalBill
import tech.okcredit.sdk.store.database.LocalBillDoc
import tech.okcredit.sdk.store.database.TxnType

object ApiEntityMapper {
    var BILLS: Converter<BillApiMessages.ServerBill, LocalBill> =
        object : Converter<BillApiMessages.ServerBill, LocalBill>() {
            override fun doForward(a: BillApiMessages.ServerBill): LocalBill {
                return LocalBill(
                    id = a.id,
                    transactionId = a.transaction_id,
                    accountId = a.account_id,
                    createdByMe = a.created_by_me,
                    localBillDocList = getLocalBillDocList(a),
                    createdAt = a.created_at_ms,
                    note = a.note,
                    amount = a.amount,
                    billDate = a.bill_date_ms,
                    txnType = when (a.transaction_type) {
                        1 -> TxnType.CREDIT
                        2 -> TxnType.PAYMENT
                        else -> TxnType.UNKNOWN
                    },
                    deleted = a.deleted,
                    updatedAt = a.updated_at_ms,
                )
            }

            override fun doBackward(b: LocalBill): BillApiMessages.ServerBill {
                TODO("Not yet implemented")
            }
        }

    internal fun getLocalBillDocList(a: BillApiMessages.ServerBill): List<LocalBillDoc> {
        val localBillList = mutableListOf<LocalBillDoc>()
        a.serverBillDocList?.forEach {
            localBillList.add(
                LocalBillDoc(
                    billDocId = it.id,
                    url = it.url,
                    createdAt = it.created_at_ms,
                    billId = a.id
                )
            )
        }
        return localBillList
    }

    var BILL_DOC: Converter<LocalBillDoc, BillApiMessages.ServerBillDoc> =
        object : Converter<LocalBillDoc, BillApiMessages.ServerBillDoc>() {
            override fun doForward(a: LocalBillDoc): BillApiMessages.ServerBillDoc {
                return BillApiMessages.ServerBillDoc(id = a.billDocId, url = a.url, created_at_ms = a.createdAt)
            }

            override fun doBackward(b: BillApiMessages.ServerBillDoc): LocalBillDoc {
                TODO("Not yet implemented")
            }
        }
}
