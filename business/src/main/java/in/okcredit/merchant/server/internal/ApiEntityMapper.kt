package `in`.okcredit.merchant.server.internal

import `in`.okcredit.merchant.store.database.BusinessCategory
import com.google.common.base.Converter
import com.google.common.base.Strings
import `in`.okcredit.merchant.store.database.Business as DbBusiness
import `in`.okcredit.merchant.store.database.BusinessType as DbBusinessType

// This is used to convert API response from server to DB data class (entity)
// so that we can save the converted DB data class (entity) directly to db
object ApiEntityMapper {

    // we pass ApiMessages.GetBusinessResponse & get DbBusiness (DB)
    var BUSINESS_RESPONSE: Converter<ApiMessages.GetBusinessResponse, DbBusiness> =
        object : Converter<ApiMessages.GetBusinessResponse, DbBusiness>() {
            override fun doForward(apiEntity: ApiMessages.GetBusinessResponse): DbBusiness {
                return DbBusiness(
                    id = apiEntity.business_user.user.id,
                    name = apiEntity.business_user.user.display_name ?: apiEntity.business_user.user.mobile,
                    mobile = apiEntity.business_user.user.mobile,
                    profileImage = apiEntity.business_user.user.profile_image,
                    address = apiEntity.business_user.user.address?.text,
                    addressLatitude = apiEntity.business_user.user.address?.address_latitude,
                    addressLongitude = apiEntity.business_user.user.address?.address_longitude,
                    about = apiEntity.business_user.user.about,
                    email = apiEntity.business_user.user.email,
                    contactName = apiEntity.business_user.contact_name,
                    createdAt = apiEntity.business_user.user.create_time,
                    category = if (Strings.isNullOrEmpty(apiEntity.business_user.business_category?.id)) null else CATEGORY.convert(
                        apiEntity.business_user.business_category
                    ),
                    business = if (Strings.isNullOrEmpty(apiEntity.business_user.business_type?.id)) null else BUSINESS.convert(
                        apiEntity.business_user.business_type
                    ),
                    isFirst = apiEntity.business_user.is_first ?: false
                )
            }

            // reverse
            override fun doBackward(store: DbBusiness): ApiMessages.GetBusinessResponse {
                throw RuntimeException("Cannot reverse DbBusiness to GetBusinessResponse")
            }
        }

    // ApiMessages.Category to Category (DB)
    var CATEGORY: Converter<ApiMessages.Category, BusinessCategory> =
        object : Converter<ApiMessages.Category, BusinessCategory>() {
            override fun doForward(apiEntity: ApiMessages.Category): BusinessCategory {

                return BusinessCategory(
                    id = apiEntity.id!!,
                    name = apiEntity.name ?: "",
                    type = apiEntity.type!!,
                    imageUrl = apiEntity.image_url,
                    isPopular = apiEntity.is_popular ?: false
                )
            }

            // reverse
            override fun doBackward(store: BusinessCategory): ApiMessages.Category {
                return ApiMessages.Category(
                    id = store.id,
                    name = store.name,
                    type = store.type,
                    image_url = store.imageUrl,
                    is_popular = store.isPopular
                )
            }
        }

    var BUSINESS: Converter<ApiMessages.Business, DbBusinessType> =
        object : Converter<ApiMessages.Business, DbBusinessType>() {
            override fun doForward(apiEntity: ApiMessages.Business): DbBusinessType {

                return DbBusinessType(
                    id = apiEntity.id,
                    name = apiEntity.name,
                    image_url = apiEntity.image_url,
                    title = apiEntity.title,
                    sub_title = apiEntity.sub_title
                )
            }

            // reverse
            override fun doBackward(store: DbBusinessType): ApiMessages.Business {
                return ApiMessages.Business(
                    id = store.id,
                    name = store.name,
                    image_url = store.image_url,
                    title = store.title,
                    sub_title = store.sub_title
                )
            }
        }
}
