package `in`.okcredit.sales_sdk.server

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_sdk.models.Models
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface ApiClient {

    @GET("sales/{sale_id}")
    fun getSale(
        @Path("sale_id") id: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<Models.SaleItemResponse>>

    @GET("sales")
    fun getSales(
        @Query("merchant_id") merchantId: String,
        @Query("from_date") fromDate: Long? = null,
        @Query("end_date") endDate: Long? = null,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<Models.SalesListResponse>>

    @POST("sales")
    fun submitSale(
        @Body sale: Models.SaleRequestModel,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<Models.AddSaleResponse>>

    @DELETE("sales/{sale_id}")
    fun deleteSale(
        @Path("sale_id") id: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Completable

    @GET("inventory")
    fun getBillItems(
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<BillModel.BillItemListResponse>>

    @POST("inventory")
    fun addBillItem(
        @Body addBillItemRequest: BillModel.AddBillItemRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<BillModel.BillItemResponse>>

    @PUT("inventory/{id}")
    fun updateBillItem(
        @Path("id") id: String,
        @Body updateBillItemRequest: BillModel.UpdateBillItemRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<BillModel.BillItemResponse>>

    @PUT("sales/{id}")
    fun updateSaleItem(
        @Path("id") id: String,
        @Body updateSaleItemRequest: Models.UpdateSaleItemRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<Models.SaleItemResponse>>
}
