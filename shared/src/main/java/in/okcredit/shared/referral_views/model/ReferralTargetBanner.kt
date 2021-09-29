package `in`.okcredit.shared.referral_views.model

import com.google.gson.annotations.SerializedName
import tech.okcredit.android.base.utils.Optional
import tech.okcredit.android.base.utils.ofNullable

data class ReferralTargetBanner(
    @SerializedName("id")
    val id: String? = "",
    @SerializedName("referrer_merchant_prize")
    val referrerMerchantPrize: Long = 0L,
    @SerializedName("referred_merchant_prize")
    val referralMerchantPrize: Long = 0L,
    @SerializedName("events")
    val isActivated: List<IsActivated> = emptyList(),
    @SerializedName("title")
    val title: String? = "",
    @SerializedName("description")
    val description: String? = "",
    @SerializedName("icon_link")
    val icon: String? = "",
    @SerializedName("deep_link")
    val deepLink: String? = "",
    @SerializedName("banner_places")
    val bannerPlace: List<Int> = emptyList(),
    @SerializedName("how_does_it_works")
    val howDoesItWorks: String? = "",
    @SerializedName("referrer_title")
    val referrerTitle: String? = "",
    @SerializedName("referrer_description")
    val referrerDescription: String? = ""
)

fun ReferralTargetBanner.canShowBanner(place: Place): Optional<ReferralTargetBanner> {
    return if (this.bannerPlace.contains(place.value)) {
        this.ofNullable()
    } else {
        Optional.Absent
    }
}
