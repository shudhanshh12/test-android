package `in`.okcredit.collection.contract

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

data class InventoryItem(
    @SerializedName("item")
    var item: String = "",
    @SerializedName("quantity")
    var quantity: Int = 0,
    @SerializedName("price")
    var price: Long = 0L,
    @SerializedName("create_time")
    val createTime: DateTime = DateTime(),
    @SerializedName("business_id")
    val merchantId: String? = "",
    @SerializedName("supplierName")
    val supplierName: String? = "",
    val link: String? = "",
    val billId: String? = "",
)

data class InventoryEpoxyModel(
    var inventoryItem: InventoryItem = InventoryItem(),
    var source: InventorySource = InventorySource.BILL,
)

enum class InventorySource(val value: String) {
    ITEM("item"),
    BILL("bill")
}

data class InventoryItemResponse(
    @SerializedName("items")
    var items: List<InventoryItem>,
)

data class GetInventoryItemsRequest(
    @SerializedName("business_id")
    var merchantId: String,
)

data class GetInventoryItemRequest(
    @SerializedName("business_id")
    var merchantId: String,
    @SerializedName("items")
    var items: List<InventoryItem>,
)

data class GetInventoryBillsResponse(
    @SerializedName("bills")
    var itemInventories: List<InventoryBillItemResponse>,
)

data class InventoryBillItemResponse(
    @SerializedName("link")
    var link: String = "",
    @SerializedName("create_time")
    val createTime: DateTime = DateTime(),
    @SerializedName("total_items")
    var quantity: Int = 0,
    @SerializedName("total_amount")
    var price: Long = 0L,
    @SerializedName("bill_id")
    var billId: String = "",
    @SerializedName("business_id")
    var businessId: String = "",
)

data class CreateInventoryBillsResponse(
    @SerializedName("bill_url")
    var billUrl: String,
    @SerializedName("bill_id")
    var billId: String,
    var businessId: String = "",
)
