package `in`.okcredit.individual.data.remote

import androidx.annotation.Keep
import org.joda.time.DateTime

@Keep
data class GetIndividualResponse(
    val individual_user: Individual,
    val business_ids: List<String>,
)

@Keep
data class Individual(
    val user: IndividualUser,
    val referral_link: String? = null,
    val payment_password_enabled: Boolean? = null,
    val whatsapp_opt_in: Boolean? = null,
    val app_lock_opt_in: Boolean? = null,
    val fingerprint_lock_opt_in: Boolean? = null,
    val four_digit_pin_in: Boolean? = null,
)

@Keep
data class IndividualUser(
    val id: String,
    val create_time: DateTime? = null,
    val mobile: String,
    val email: String? = null,
    val register_time: DateTime? = null,
    val lang: String? = null,
    val display_name: String? = null,
    val profile_image: String? = null,
    val address: Address? = null,
    val about: String? = null,
)

@Keep
data class Address(
    val text: String?,
    val longitude: Double?,
    val latitude: Double?,
)

@Keep
data class UpdateIndividualRequest(
    val individual_user_id: String,
    val individual_user: Individual,
    val update_app_lock_opt_in: Boolean? = null,
    val update_payment_password_enabled: Boolean? = null,
    val update_whatsapp_opt_in: Boolean? = null,
    val update_fingerprint_lock_opt_in: Boolean? = null,
    val update_four_digit_pin_opt_in: Boolean? = null,
    val update_lang: Boolean? = null,
    val current_mobile_otp_token: String? = null,
    val new_mobile_otp_token: String? = null,
    val update_mobile: Boolean? = null,
)
