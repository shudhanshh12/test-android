package `in`.okcredit.merchant.contract

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class Category(
    var id: String? = null,
    var type: Int,
    var name: String? = null,
    var imageUrl: String? = null,
    var isPopular: Boolean = true
) : Parcelable
