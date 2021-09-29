package `in`.okcredit.merchant.contract

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime

@Keep
@Parcelize
data class Business(
    var id: String,
    var name: String,
    var mobile: String,
    var profileImage: String? = null,
    var address: String? = null,
    var addressLatitude: Double? = null,
    var addressLongitude: Double? = null,
    var about: String? = null,
    var email: String? = null,
    var contactName: String? = null,
    var createdAt: DateTime,
    var categoryId: String? = null,
    var updateCategory: Boolean = false,
    var currentMobileOTPToken: String? = null,
    var newMobileOTPToken: String? = null,
    var updateMobile: Boolean = false,
    var othersCategoryName: String? = null,
    var category: Category? = null,
    var businessType: BusinessType? = null,
    var isFirst: Boolean = false
) : Parcelable {
    fun isNameSet(): Boolean {
        return (name != mobile)
    }
}
