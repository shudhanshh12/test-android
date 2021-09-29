package tech.okcredit.sdk.server

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface BillApiClient {

    @POST("ListBills")
    fun listBills(
        @Body listsBillRequests: BillApiMessages.ListBillsRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<BillApiMessages.ListBillsResponse>>

    @POST("SyncBills")
    fun postBills(
        @Body billSyncRequest: BillApiMessages.BillSyncRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<BillApiMessages.BillSyncResponse>>

    @POST("new/GetTransactionFile")
    fun getBillFile(
        @Body req: BillApiMessages.GetBillFileRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<BillApiMessages.GetBillFileResponse?>>

    @POST("SyncBills")
    fun deletedBill(
        @Body billOperationList: BillApiMessages.BillSyncRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<BillApiMessages.BillSyncResponse?>>
}
