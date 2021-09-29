package `in`.okcredit.backend._offline.server.internal

import `in`.okcredit.backend._offline.serverV2.internal.ApiMessagesV2
import `in`.okcredit.backend.contract.Version
import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddTransactionRequest
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface ApiClient {
    // get latest app version info
    @GET("version")
    fun getLatestVersion(@Query("lang") lang: String?): Single<Response<Version>>

    // check if mobile is registered
    @POST("check")
    fun checkMobileStatus(@Body req: CheckMobileStatusRequest): Single<Response<Void>>

    // link device with active merchant
    @POST("devices/{device_id}/link")
    fun linkDevice(@Path("device_id") deviceId: String?): Single<Response<Void>>

    // list all customers/serch customer by mobile
    @GET("customer")
    fun listCustomers(
        @Query("mobile") mobile: String?,
        @Query("deleted") deleted: Boolean,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<List<Customer>>>

    // get customer by id
    @GET("customer/{customer_id}")
    fun getCustomer(
        @Path("customer_id") customerId: String?,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<Customer>>

    // add customer
    @POST("customer")
    fun addCustomer(
        @Body request: AddCustomerRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<Customer>>

    // delete customer
    @DELETE("customer/{customer_id}")
    fun deleteCustomer(
        @Path("customer_id") customerId: String?,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<Void>>

    // update customer
    @PUT("customer/{customer_id}")
    fun updateCustomer(
        @Path("customer_id") customerId: String,
        @Body request: UpdateCustomerRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<Customer>>

    // get transaction by id
    @GET("transaction/{tx_id}")
    fun getTransaction(
        @Path("tx_id") txnId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<Transaction>>

    // delete transaction by id
    @DELETE("transaction/{tx_id}")
    fun deleteTransaction(
        @Path("tx_id") txnId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<Void>>

    @POST("transaction")
    fun addTransaction2(
        @Body req: AddTransactionRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<Transaction>>

    // update active merchant profile
    @POST("feedback")
    fun submitFeedback(
        @Body req: FeedbackRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<Void>>

    @POST("new/ListDuesInfo")
    fun getDueInfo(
        @Body listDuesInfoRequest: ListDuesInfoRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<ListDuesInfoResponse>>

    @POST("new/GetDueInfo")
    fun getParticularCustomerDueInfo(
        @Body getDueInfoRequest: GetDueInfoRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<GetDueInfoResponse>>

    @DELETE("new/transaction-images/{img_id}")
    fun deleteTransactionImage(
        @Path("img_id") imageId: String?,
        @Query("transaction_id") txnId: String?,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Completable

    @POST("new/GetTransactions")
    fun getTransactions(
        @Body request: ApiMessagesV2.GetTransactionsRequest,
        @Header("X-Source") source: String?,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<ApiMessagesV2.GetTransactionsResponse>>

    @POST("new/GetTransactionFile")
    fun getTransactionFile(
        @Body req: ApiMessagesV2.GetTransactionFileRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<ApiMessagesV2.GetTransactionFileResponse>>

    @PATCH("new/transactions/{txn_id}")
    fun updateTransactionNote(
        @Path("txn_id") txId: String?,
        @Body patchTransactionRequest: ApiMessagesV2.PatchTransactionRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Completable

    @POST("new/transaction-images")
    fun createTransactionImage(
        @Body createTransactionImageRequest: CreateTransactionImageRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Completable

    @POST("detect-intent")
    fun postVoiceData(
        @Body voiceInputBody: VoiceInputBody,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<VoiceInputResponseBody>>

    @POST("new/MigrateAccount")
    fun migrate(
        @Body migrationBody: MigrationBody,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<MigrateAccountResponse>>

    @POST("citadel/v1/AddDiscount")
    fun createDiscount(
        @Body addDiscountRequest: AddDiscountRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Completable

    @POST("citadel/v1/DeleteDiscount")
    fun deleteDiscount(
        @Body deleteDiscountRequest: DeleteDiscountRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Completable

    @POST("new/SyncContact")
    fun syncUpdatedAccounts(
        @Body syncContactRequest: SyncContactRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Completable

    @POST("customer/customerAndTransaction")
    fun quickAddTransaction(
        @Body request: QuickAddTransactionRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<QuickAddTransactionResponse>

    @POST("recover-transactions/check-status")
    fun checkActionableStatus(
        @Body request: CheckActionableStatusRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<CheckActionableStatusResponse>

    @PUT("recover-transactions/update-status/{action_id}")
    fun updateActionableStatus(
        @Path("action_id") actionId: String?,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Completable

    @GET("feature/GetBuyerTxnAlertFeature")
    fun getAllAccountsBuyerTxnAlertConfig(
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<AllAccountsBuyerTxnAlertConfigResponse>>

    @POST("feature/UpdateBuyerTxnAlertFeature")
    fun updateFeatureValueRequest(
        @Body updateFeatureValueRequest: UpdateFeatureValueRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Completable
}
