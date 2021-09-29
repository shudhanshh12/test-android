package `in`.okcredit.merchant.device

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime

@Keep
@Parcelize
data class Device(
    val id: String,
    val versionCode: Int,
    val apiLevel: Int,
    val aaid: String? = null,
    val fcmToken: String? = null,
    val referrers: MutableList<Referrer> = mutableListOf(),
    val createTime: DateTime,
    val updateTime: DateTime,
    val syncTime: DateTime? = null,
    @SerializedName("rooted")
    val isRooted: Boolean = false
) : Parcelable

@Keep
@Parcelize
data class Referrer(
    val source: String,
    val value: String
) : Parcelable

enum class ReferrerSource(val value: String) {
    DIRECT("DIRECT"),
    PLAY_STORE("PLAY_STORE"),
    APPS_FLYER("APPS_FLYER"),
    BRANCH("BRANCH"),
    BRANCH_LAST_TOUCH("BRANCH_LAST_TOUCH")
}

@Keep
data class IpAddressData(
    val city: String? = null,
    val country_code: String? = null,
    val country_name: String? = null,
    val ip: String? = null,
    val region: String? = null,
    val region_code: String? = null
)
