package `in`.okcredit.sales_sdk.models

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import org.json.JSONObject

object Models {

    data class SalesListResponse(
        @SerializedName("sales")
        var salesList: List<Sale>,
        @SerializedName("total_amount")
        var totalAmount: Double,
        @SerializedName("total_number_of_sales")
        var totalNumberOfSales: Double,
        @SerializedName("start_date")
        var startDate: DateTime? = null,
        @SerializedName("end_date")
        var endDate: DateTime? = null
    )

    data class SaleItemResponse(
        @SerializedName("sale")
        var sale: Sale
    )

    data class Sale(
        @SerializedName("amount")
        var amount: Double,
        @SerializedName("created_at")
        var createdAt: DateTime,
        @SerializedName("deleted_at")
        var deletedAt: DateTime? = null,
        @SerializedName("id")
        var id: String,
        @SerializedName("notes")
        var note: String?,
        @SerializedName("buyer_name")
        var buyerName: String?,
        @SerializedName("buyer_mobile")
        var buyerMobile: String?,
        @SerializedName("updated_at")
        var updatedAt: DateTime? = null,
        @SerializedName("sale_date")
        var saleDate: DateTime,
        @SerializedName("billed_items")
        var billedItems: SaleItems?
    ) {
        fun toJsonString(): String {
            val json = JSONObject()
            json.put("amount", amount)
            json.put("created_at", createdAt.millis)
            json.put("deleted_at", deletedAt?.millis)
            json.put("id", id)
            json.put("notes", note)
            json.put("buyer_name", buyerName)
            json.put("buyer_mobile", buyerMobile)
            json.put("updated_at", updatedAt?.millis)
            json.put("sale_date", saleDate.millis)
            return json.toString()
        }
    }

    data class SaleItems(
        @SerializedName("items")
        var items: List<BillModel.BillItem>
    )

    data class SaleRequestModel(
        @SerializedName("sale")
        private var sale: AddSale
    )

    data class AddSale(
        @SerializedName("merchant_id")
        var merchantId: String,
        @SerializedName("amount")
        var amount: Double,
        @SerializedName("notes")
        var note: String,
        @SerializedName("sale_date")
        var saleDate: DateTime,
        @SerializedName("buyer_name")
        var buyerName: String?,
        @SerializedName("buyer_mobile")
        var buyerMobile: String?,
        @SerializedName("billed_items")
        var billedItems: BillModel.BilledItems? = null
    )

    data class AddSaleResponse(
        @SerializedName("sale")
        var sale: Sale
    )

    data class UpdateSaleItemRequest(
        @SerializedName("sale")
        var sale: UpdateSale,
        @SerializedName("mask")
        var mask: List<String>
    )

    data class UpdateSale(
        @SerializedName("buyer_name")
        var name: String? = null,
        @SerializedName("buyer_mobile")
        var mobile: String? = null,
        @SerializedName("sale_date")
        var saleDate: DateTime? = null
    )
}
