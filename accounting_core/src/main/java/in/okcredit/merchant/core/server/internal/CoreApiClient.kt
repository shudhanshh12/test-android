package `in`.okcredit.merchant.core.server.internal

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import `in`.okcredit.merchant.core.model.bulk_reminder.SetRemindersApiRequest
import `in`.okcredit.merchant.core.server.internal.bulk_search_transactions.BulkSearchTransactionsRequest
import `in`.okcredit.merchant.core.server.internal.bulk_search_transactions.BulkSearchTransactionsResponse
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddTransactionRequest
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddTransactionResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface CoreApiClient {

    // TODO Remove V1.0 from gradle
    @POST("customers/sync")
    suspend fun pushCustomerCommands(
        @Body requestTransactions: CoreApiMessages.PushCustomersCommandsRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): CoreApiMessages.PushCustomersCommandsResponse

    @POST("sync-transactions")
    fun pushTransactionCommands(
        @Body requestTransactions: CoreApiMessages.PushTransactionsCommandsRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<CoreApiMessages.PushTransactionsCommandsResponse?>>

    @POST("new/GetTransactions")
    fun getTransactions(
        @Body request: CoreApiMessages.GetTransactionsRequest,
        @Header("X-Source") source: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<CoreApiMessages.GetTransactionsResponse?>>

    @POST("new/GetTransactionFile")
    fun getTransactionFile(
        @Body req: CoreApiMessages.GetTransactionFileRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<CoreApiMessages.GetTransactionFileResponse?>>

    @GET("transaction/{tx_id}")
    fun getTransaction(
        @Path("tx_id") transactionId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<CoreApiMessages.Transaction>>

    @POST("recover-transactions/bulk-search-transactions")
    fun bulkSearchTransactions(
        @Body request: BulkSearchTransactionsRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<BulkSearchTransactionsResponse>>

    @POST("customer")
    fun addCustomer(
        @Body addCustomerRequest: CoreApiMessages.AddCustomerRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<CoreApiMessages.ApiCustomer?>>

    @GET("customer")
    fun listCustomers(
        @Query("mobile") mobile: String?,
        @Query("deleted") deleted: Boolean,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<List<CoreApiMessages.ApiCustomer>>>

    @GET("customer/{customer_id}")
    fun getCustomer(
        @Path("customer_id") customerId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<CoreApiMessages.ApiCustomer?>>

    @DELETE("customer/{customer_id}")
    fun deleteCustomer(
        @Path("customer_id") customerId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<Void>>

    @PUT("customer/{customer_id}")
    fun updateCustomer(
        @Path("customer_id") customerId: String,
        @Body request: CoreApiMessages.UpdateCustomerRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<CoreApiMessages.ApiCustomer?>>

    @POST("new/GetTxnAmountHistory")
    fun getTxnAmountHistory(
        @Body request: CoreApiMessages.GetTransactionAmountHistoryRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<CoreApiMessages.GetTransactionAmountHistoryResponse?>>

    @POST("customer/customerAndTransaction")
    fun quickAddTransaction(
        @Body request: QuickAddTransactionRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<QuickAddTransactionResponse>

    @GET("add-transaction/predicted-accounts")
    fun getSuggestedCustomerIdsForAddTransaction(
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<CoreApiMessages.SuggestedCustomerIdsForAddTransactionResponse>>

    @Headers("Content-Type: application/json", "Content-Encoding: gzip")
    @PUT("customers/reminders")
    suspend fun setCustomersLastReminderSendTime(
        @Body request: SetRemindersApiRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String
    )
}
