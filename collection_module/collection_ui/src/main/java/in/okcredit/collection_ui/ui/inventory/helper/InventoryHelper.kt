package `in`.okcredit.collection_ui.ui.inventory.helper

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CreateInventoryBillsResponse
import `in`.okcredit.collection.contract.GetInventoryBillsResponse
import `in`.okcredit.collection.contract.InventoryItem
import `in`.okcredit.collection.contract.InventoryItemResponse
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class InventoryHelper @Inject constructor(
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val collectionRepository: Lazy<CollectionRepository>,
) {

    suspend fun getBillsList(): GetInventoryBillsResponse {
        val businessId = getActiveBusinessId.get().execute().await()
        return collectionRepository.get().getBills(businessId)
    }

    suspend fun getItemsList(): InventoryItemResponse {
        val businessId = getActiveBusinessId.get().execute().await()
        return collectionRepository.get().getBillingItem(businessId)
    }

    suspend fun createItem(inventoryItem: InventoryItem) {
        val businessId = getActiveBusinessId.get().execute().await()
        collectionRepository.get().createBillingItem(inventoryItem, businessId)
    }

    suspend fun createBill(listBillItem: List<InventoryItem>): CreateInventoryBillsResponse {
        val businessId = getActiveBusinessId.get().execute().await()
        val response = collectionRepository.get().createBill(listBillItem, businessId)
        return response.copy(businessId = businessId)
    }
}
