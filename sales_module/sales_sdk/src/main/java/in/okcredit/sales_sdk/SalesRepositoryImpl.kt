package `in`.okcredit.sales_sdk

import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.sales_sdk.server.SalesRemoteSource
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class SalesRepositoryImpl @Inject constructor(
    private val salesRemoteSource: SalesRemoteSource,
) : SalesRepository {
    override fun getSale(saleId: String, businessId: String): Single<Models.SaleItemResponse> {
        return salesRemoteSource.getSale(saleId, businessId)
    }

    override fun updateSale(
        saleId: String,
        updateSaleItemRequest: Models.UpdateSaleItemRequest,
        businessId: String,
    ): Single<Models.SaleItemResponse> {
        return salesRemoteSource.updateSale(saleId, updateSaleItemRequest, businessId)
    }

    override fun getSales(
        startTime: Long?,
        endTime: Long?,
        businessId: String,
    ): Single<Models.SalesListResponse> {
        return salesRemoteSource.getSales(startTime, endTime, businessId)
    }

    override fun submitSale(
        expenseRequestModel: Models.SaleRequestModel,
        businessId: String,
    ): Single<Models.AddSaleResponse> {
        return salesRemoteSource.submit(expenseRequestModel, businessId)
    }

    override fun deleteSale(id: String, businessId: String): Completable {
        return salesRemoteSource.deleteSale(id, businessId)
    }

    override fun getBillItems(businessId: String): Single<BillModel.BillItemListResponse> {
        return salesRemoteSource.getBillItems(businessId)
    }

    override fun addBillItem(
        addBillItemRequest: BillModel.AddBillItemRequest,
        businessId: String,
    ): Single<BillModel.BillItemResponse> {
        return salesRemoteSource.addBillItem(addBillItemRequest, businessId)
    }

    override fun updateBillItem(
        billId: String,
        updateBillItemRequest: BillModel.UpdateBillItemRequest,
        businessId: String,
    ): Single<BillModel.BillItemResponse> {
        return salesRemoteSource.updateBillItem(billId, updateBillItemRequest, businessId)
    }
}
