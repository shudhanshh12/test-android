package `in`.okcredit.sales_sdk.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

object BillModel {

    @Parcelize
    data class BillItem(
        @SerializedName("id")
        var id: String,
        @SerializedName("name")
        var name: String,
        @SerializedName("rate")
        var rate: Double = 0.0,
        @SerializedName("quantity")
        var quantity: Double = 1.0
    ) : Parcelable

    data class BilledItem(
        @SerializedName("id")
        var id: String,
        @SerializedName("quantity")
        var quantity: Double = 1.0
    )

    data class AddBillItem(
        @SerializedName("name")
        var name: String,
        @SerializedName("rate")
        var rate: Double = 0.0
    )

    data class AddBillItemRequest(
        @SerializedName("item")
        var item: AddBillItem
    )

    data class UpdateBillItemRequest(
        @SerializedName("item")
        var item: AddBillItem,
        @SerializedName("mask")
        var updateName: List<String>
    )

    data class BilledItems(
        @SerializedName("total")
        var total: String,
        @SerializedName("items")
        var items: List<BilledItem>
    )

    data class BillItemListResponse(
        @SerializedName("items")
        var items: List<BillItem>
    )

    @Parcelize
    data class BillItems(
        var items: List<BillItem>
    ) : Parcelable

    data class BillItemResponse(
        @SerializedName("item")
        var item: BillItem
    )
}
