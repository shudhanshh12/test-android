package `in`.okcredit.onboarding.social_validation.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

interface SocialValidationService {

    @POST("v1/onboarding/social")
    suspend fun getScreens(@Body request: SocialValidationRequest): SocialValidationResponse
}

@Keep
data class SocialValidationRequest(
    @SerializedName("locale")
    val locale: String,

    @SerializedName("device_id")
    val deviceId: String,
)

@Keep
data class SocialValidationResponse(
    @SerializedName("screens")
    val screens: List<Screen>,
)

@Keep
data class Screen(
    @SerializedName("type")
    val type: String,

    @SerializedName("url")
    val url: String,

    @SerializedName("duration")
    val duration: Long?,

    @SerializedName("subtitle")
    val subtitle: String?,
)
