package `in`.okcredit.merchant.collection.server.internal

import `in`.okcredit.collection.contract.CreateInventoryBillsResponse
import `in`.okcredit.collection.contract.GetInventoryBillsResponse
import `in`.okcredit.collection.contract.GetInventoryItemRequest
import `in`.okcredit.collection.contract.GetInventoryItemsRequest
import `in`.okcredit.collection.contract.InventoryItem
import `in`.okcredit.collection.contract.InventoryItemResponse
import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface CollectionBillingApiClient {

    @POST("v1/CreateItem")
    suspend fun createBillingItem(
        @Body request: InventoryItem,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    )

    @POST("v1/GetItems")
    suspend fun getBillingItems(
        @Body request: GetInventoryItemsRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): InventoryItemResponse

    @POST("v1/CreateBill")
    suspend fun createBill(
        @Body request: GetInventoryItemRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): CreateInventoryBillsResponse

    @POST("v1/GetBills")
    suspend fun getBills(
        @Body request: GetInventoryItemsRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): GetInventoryBillsResponse
}
