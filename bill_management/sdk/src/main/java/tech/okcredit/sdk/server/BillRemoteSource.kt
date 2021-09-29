package tech.okcredit.sdk.server

import io.reactivex.Single

interface BillRemoteSource {

    fun getBills(
        request: BillApiMessages.ListBillsRequest,
        businessId: String,
    ): Single<BillApiMessages.ListBillsResponse>

    fun createBill(
        billOperationList: List<BillApiMessages.BillOperation>,
        id: String,
        businessId: String,
    ): Single<BillApiMessages.BillSyncResponse>

    fun getBills(startDate: Long, source: String, businessId: String): Single<BillApiMessages.ListBillsResponse>

    fun getTransactionFile(id: String, businessId: String): Single<BillApiMessages.GetBillFileResponse>

    fun deletedBill(
        billOperationList: BillApiMessages.BillSyncRequest,
        businessId: String,
    ): Single<BillApiMessages.BillSyncResponse?>

    fun uploadNewBillDocs(
        operationList: BillApiMessages.BillSyncRequest,
        businessId: String,
    ): Single<BillApiMessages.BillSyncResponse>

    fun updateNote(
        note: BillApiMessages.BillSyncRequest,
        businessId: String,
    ): Single<BillApiMessages.BillSyncResponse>

    fun deleteBillDoc(
        billSyncRequest: BillApiMessages.BillSyncRequest,
        businessId: String,
    ): Single<BillApiMessages.BillSyncResponse>
}
