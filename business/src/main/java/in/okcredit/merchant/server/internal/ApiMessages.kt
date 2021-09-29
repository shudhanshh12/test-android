package `in`.okcredit.merchant.server.internal

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

interface ApiMessages {

    @Keep
    data class GetBusinessResponse(
        val business_user: GetBusinessResponseWrapper,
    )

    @Keep
    data class GetBusinessResponseWrapper(
        val user: BusinessUser,
        var business_category: Category? = null,
        var business_type: Business? = null,
        val is_first: Boolean? = null,
        val contact_name: String? = null,
    )

    @Keep
    data class BusinessUser(
        val id: String,
        val mobile: String,
        val email: String? = null,
        val display_name: String?,
        val profile_image: String? = null,
        val address: Address? = null,
        val about: String? = null,
        val create_time: DateTime,
    )

    @Keep
    data class Address(
        val text: String?,
        val address_latitude: Double?,
        val address_longitude: Double?,
    )

    @Keep
    data class MerchantRequest(
        val id: String,
        val name: String,
        val mobile: String,
        val contact_name: String?,
        val profile_image: String?,
        val address: String?,
        val address_latitude: Double?,
        val address_longitude: Double?,
        val about: String?,
        val email: String?,
        val category_id: String? = null,
        val others_category_name: String? = null,
        val update_category: Boolean = false,
        val newMobileOtpToken: String? = null,
        val currentMobileOtpToken: String? = null,
        val updateMobile: Boolean = false,
        val business_type_id: String?,
        val update_business_type: Boolean = false,
    )

    @Keep
    data class Category(
        val id: String?,
        val type: Int? = null,
        val name: String?,
        val image_url: String? = null,
        val is_popular: Boolean? = null,
    )

    @Keep
    data class Business(
        val id: String,
        val name: String? = null,
        val image_url: String? = null,
        val title: String? = null,
        val sub_title: String? = null,
    )

    @Keep
    data class CategoryResponse(
        val categories: List<Category>,
    )

    @Keep
    data class BusinessTypeResponse(
        val business_type: List<Business>,
    )

    @Keep
    data class SetMerchantPreferenceRequest(
        val key: String,
        val value: String,
    )

    data class IsMerchantActivatedApiRequest(
        @SerializedName("merchant_id")
        val merchantId: String,
    )

    data class IsMerchantActivated(
        @SerializedName("is_activated")
        val isActivated: Boolean,
    )

    data class GetBusinessRequest(
        @SerializedName("business_user_id")
        val businessId: String,
    )

    data class CreateBusinessRequest(
        @SerializedName("name")
        val name: String,
    )

    @Keep
    data class UpdateBusinessRequest(
        val business_user_id: String,
        val business_user: GetBusinessResponseWrapper,
        val update_contact_name: Boolean = false,
        val update_category: Boolean = false,
        val update_email: Boolean = false,
        val update_display_name: Boolean = false,
        val update_profile_image: Boolean = false,
        val update_about: Boolean = false,
        val update_address: Boolean = false,
        val update_address_longitude: Boolean = false,
        val update_address_latitude: Boolean = false,
        val update_business_type: Boolean = false,
    )
}
