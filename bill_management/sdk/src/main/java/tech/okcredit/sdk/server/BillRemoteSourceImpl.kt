package tech.okcredit.sdk.server

import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.base.error.Error
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError
import tech.okcredit.sdk.models.Ordering
import javax.inject.Inject

class BillRemoteSourceImpl @Inject constructor(
    private val apiClient: Lazy<BillApiClient>
) : BillRemoteSource {
    override fun getBills(
        request: BillApiMessages.ListBillsRequest,
        businessId: String,
    ): Single<BillApiMessages.ListBillsResponse> {
        return apiClient.get().listBills(request, businessId).subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw Error.parse(res)
                }
            }
    }

    override fun createBill(
        billOperationList: List<BillApiMessages.BillOperation>,
        id: String,
        businessId: String
    ): Single<BillApiMessages.BillSyncResponse> {
        return apiClient.get().postBills(BillApiMessages.BillSyncRequest(billOperationList), businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker()).map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw Error.parse(res)
                }
            }
    }

    override fun getBills(startDate: Long, source: String, businessId: String): Single<BillApiMessages.ListBillsResponse> {

        return apiClient.get().listBills(
            BillApiMessages.ListBillsRequest(
                start_time_ms = startDate,
                order_by = Ordering.UPDATE_TIME.label,
                exclude_deleted = false
            ),
            businessId
        )
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw Error.parse(res)
                }
            }
    }

    override fun getTransactionFile(id: String, businessId: String): Single<BillApiMessages.GetBillFileResponse> {
        return apiClient.get().getBillFile(BillApiMessages.GetBillFileRequest(id), businessId)
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

    override fun deletedBill(
        billSyncRequest: BillApiMessages.BillSyncRequest,
        businessId: String,
    ): Single<BillApiMessages.BillSyncResponse?> {
        return apiClient.get().deletedBill(billSyncRequest, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker()).map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw Error.parse(res)
                }
            }
    }

    override fun uploadNewBillDocs(
        operationList: BillApiMessages.BillSyncRequest,
        businessId: String,
    ): Single<BillApiMessages.BillSyncResponse> {
        return apiClient.get().postBills(operationList, businessId).subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker()).map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw Error.parse(res)
                }
            }
    }

    override fun updateNote(
        operationList: BillApiMessages.BillSyncRequest,
        businessId: String
    ): Single<BillApiMessages.BillSyncResponse> {
        return apiClient.get().postBills(operationList, businessId).subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker()).map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw Error.parse(res)
                }
            }
    }

    override fun deleteBillDoc(
        billSyncRequest: BillApiMessages.BillSyncRequest,
        businessId: String
    ): Single<BillApiMessages.BillSyncResponse> {
        return apiClient.get().postBills(billSyncRequest, businessId).subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker()).map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw Error.parse(res)
                }
            }
    }
}
