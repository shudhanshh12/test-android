package `in`.okcredit.merchant.contract

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class BusinessType(
    val id: String,
    val name: String? = null,
    val image_url: String? = null,
    val title: String? = null,
    val sub_title: String? = null
) : Parcelable
