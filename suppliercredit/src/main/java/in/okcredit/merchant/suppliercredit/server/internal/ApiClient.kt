package `in`.okcredit.merchant.suppliercredit.server.internal

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import io.reactivex.Completable
import io.reactivex.Single
import org.joda.time.DateTime
import retrofit2.Response
import retrofit2.http.*

interface ApiClient {

    // add Supplier
    @POST("sc/suppliers")
    fun addSupplier(
        @Body request: ApiMessages.SupplierRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.Supplier>>

    @POST("sc/suppliers")
    fun reactiveSupplier(
        @Body request: ApiMessages.SupplierRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.Supplier>>

    // get Suppliers
    @GET("sc/suppliers")
    fun getSuppliers(
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.SuppliersResponse>>

    // get Supplier
    @GET("sc/suppliers/{supplier_id}")
    fun getSupplier(
        @Path("supplier_id") supplierId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.Supplier>>

    // update Supplier
    @PATCH("sc/suppliers/{supplier_id}")
    fun updateSupplier(
        @Path("supplier_id") supplierId: String,
        @Body req: ApiMessages.UpdateSupplierRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.Supplier>>

    // delete Supplier
    @DELETE("sc/suppliers/{supplier_id}")
    fun deleteSupplier(
        @Path("supplier_id") supplierId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<Void>>

    // find Supplier by mobile
    @GET("sc/suppliers:find?mobile={mobile}")
    fun getSupplierByMobile(
        @Path("mobile") mobile: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.Supplier>>

    // batch add suppliers
    @GET("sc/suppliers:batch")
    fun batchAddSuppliers(
        @Body request: List<ApiMessages.SupplierRequest>,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Completable

    // add TRANSACTION
    @POST("sc/suppliers/{supplier_id}/transactions")
    fun addTransaction(
        @Path("supplier_id") supplierId: String,
        @Body request: ApiMessages.AddTransactionRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.Transaction>>

    // get TRANSACTION
    @GET("sc/transactions/{transaction_id}")
    fun getTransaction(
        @Path("transaction_id") transactionId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.Transaction>>

    // get Transactions
    @GET("sc/transactions")
    fun getTransactions(
        @Query("start_time") startTime: DateTime? = null,
        @Query("end_time") endTime: DateTime? = null,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.TransactionsResponse>>
//    fun getTransactions(): Single<Response<ApiMessages.TransactionsResponse>>

    // get Transactions of supplier
    @GET("sc/suppliers/{supplier_id}/transactions")
    fun getTransactionsOfSupplier(
        @Path("supplier_id") supplierId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.TransactionsResponse>>

    @DELETE("sc/transactions/{transaction_id}")
    fun deleteTransaction(
        @Path("transaction_id") transactionId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<Void>>

    // get Supplier Credit Enabled CustomerIds
    @POST("customer/feature/find")
    fun getSupplierEnabledCustomerIds(
        @Body request: ApiMessages.FeatureFindRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.FeatureFindResponse>>

    @GET("single-list/GetCommonLedgerEnabled")
    fun getSingleListSupplierEnabledCustomerIds(
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.FeatureFindResponse>>

    @GET("feature/ReminderNotifications")
    fun getNotificationReminders(
        @Query("start_time") startTime: Long,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.NotificationRemindersResponse>>

    @POST("feature/CreateReminderNotification")
    fun createNotificationReminder(
        @Body request: ApiMessages.CreateNotificationReminderRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.CreateNotificationReminderResponse>>

    @PATCH("feature/ReminderNotifications")
    fun updateNotificationReminders(
        @Body request: ApiMessages.UpdateNotificationReminder,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.UpdateNotificationReminderActionResponse>>
}
