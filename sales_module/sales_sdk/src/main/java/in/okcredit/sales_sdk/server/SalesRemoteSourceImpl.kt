package `in`.okcredit.sales_sdk.server

import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_sdk.models.Models
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError
import javax.inject.Inject

class SalesRemoteSourceImpl @Inject constructor(
    private val apiClient: Lazy<ApiClient>,
) : SalesRemoteSource {

    override fun getSale(saleId: String, businessId: String): Single<Models.SaleItemResponse> {
        return apiClient.get().getSale(saleId, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }
    }

    override fun updateSale(
        saleId: String,
        updateSaleItemRequest: Models.UpdateSaleItemRequest,
        businessId: String,
    ): Single<Models.SaleItemResponse> {
        return apiClient.get().updateSaleItem(saleId, updateSaleItemRequest, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }
    }

    override fun getSales(
        startTime: Long?,
        endTime: Long?,
        businessId: String,
    ): Single<Models.SalesListResponse> {
        return apiClient.get().getSales(businessId, startTime, endTime, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }
    }

    override fun submit(amountModel: Models.SaleRequestModel, businessId: String): Single<Models.AddSaleResponse> {
        return apiClient.get().submitSale(amountModel, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }
    }

    override fun deleteSale(id: String, businessId: String): Completable {
        return apiClient.get().deleteSale(id, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
    }

    override fun getBillItems(businessId: String): Single<BillModel.BillItemListResponse> {
        return apiClient.get().getBillItems(businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }
    }

    override fun addBillItem(
        addBillItemRequest: BillModel.AddBillItemRequest,
        businessId: String,
    ): Single<BillModel.BillItemResponse> {
        return apiClient.get().addBillItem(addBillItemRequest, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    val error = res.asError()
                    if (error.code == 403) {
                        throw Exception(error.code.toString())
                    }
                    throw res.asError()
                }
            }
    }

    override fun updateBillItem(
        billId: String,
        updateBillItemRequest: BillModel.UpdateBillItemRequest,
        businessId: String,
    ): Single<BillModel.BillItemResponse> {
        return apiClient.get().updateBillItem(billId, updateBillItemRequest, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    val error = res.asError()
                    if (error.code == 403) {
                        throw Exception(error.code.toString())
                    }
                    throw res.asError()
                }
            }
    }
}
