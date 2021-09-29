package `in`.okcredit.sales_sdk.server

import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_sdk.models.Models
import io.reactivex.Completable
import io.reactivex.Single

interface SalesRemoteSource {

    fun getSale(saleId: String, businessId: String): Single<Models.SaleItemResponse>

    fun updateSale(saleId: String, updateSaleItemRequest: Models.UpdateSaleItemRequest, businessId: String): Single<Models.SaleItemResponse>

    fun getSales(startTime: Long? = null, endTime: Long? = null, businessId: String): Single<Models.SalesListResponse>

    fun submit(amountModel: Models.SaleRequestModel, businessId: String): Single<Models.AddSaleResponse>

    fun deleteSale(id: String, businessId: String): Completable

    fun getBillItems(businessId: String): Single<BillModel.BillItemListResponse>

    fun addBillItem(addBillItemRequest: BillModel.AddBillItemRequest, businessId: String): Single<BillModel.BillItemResponse>

    fun updateBillItem(
        billId: String,
        updateBillItemRequest: BillModel.UpdateBillItemRequest,
        businessId: String
    ): Single<BillModel.BillItemResponse>
}
