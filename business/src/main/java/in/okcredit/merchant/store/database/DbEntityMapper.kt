package `in`.okcredit.merchant.store.database

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.BusinessType
import `in`.okcredit.merchant.contract.Category
import com.google.common.base.Converter
import `in`.okcredit.merchant.store.database.Business as DbBusiness
import `in`.okcredit.merchant.store.database.BusinessType as DbBusinessType

object DbEntityMapper {

    /************************************* MerchantResponse *************************************/

    fun Business.toDbBusiness() = DbBusiness(
        id = this.id,
        name = this.name,
        mobile = this.mobile,
        profileImage = this.profileImage,
        address = this.address,
        addressLatitude = this.addressLatitude,
        addressLongitude = this.addressLongitude,
        about = this.about,
        email = this.email,
        contactName = this.contactName,
        createdAt = this.createdAt,
        category = CATEGORY.convert(
            this.category
        ),
        business = BUSINESS.convert(
            this.businessType
        )
    )

    fun DbBusiness.toBusiness() = Business(
        id = this.id,
        name = this.name,
        mobile = this.mobile,
        profileImage = this.profileImage,
        address = this.address,
        addressLatitude = this.addressLatitude,
        addressLongitude = this.addressLongitude,
        about = this.about,
        email = this.email,
        contactName = this.contactName,
        createdAt = this.createdAt,
        category = CATEGORY.reverse().convert(
            this.category
        ),
        businessType = BUSINESS.reverse().convert(
            this.business
        )
    )

    /************************************* Category *************************************/

    var BUSINESS: Converter<BusinessType, DbBusinessType> =
        object : Converter<BusinessType, DbBusinessType>() {
            override fun doForward(business: BusinessType): DbBusinessType {
                return DbBusinessType(
                    id = business.id,
                    name = business.name,
                    image_url = business.image_url,
                    title = business.title,
                    sub_title = business.sub_title
                )
            }

            //  Category (DB entity) to Category (UI)
            override fun doBackward(dbEntity: DbBusinessType): BusinessType {
                return BusinessType(
                    id = dbEntity.id,
                    name = dbEntity.name,
                    image_url = dbEntity.image_url,
                    title = dbEntity.title,
                    sub_title = dbEntity.sub_title
                )
            }
        }

    /************************************* Category *************************************/

    // Category (UI) to MerchantResponse (DB entity)
    var CATEGORY: Converter<Category, BusinessCategory> =
        object : Converter<Category, BusinessCategory>() {
            override fun doForward(category: Category): BusinessCategory {
                return BusinessCategory(
                    id = category.id!!,
                    name = category.name!!,
                    type = category.type,
                    imageUrl = category.imageUrl!!,
                    isPopular = category.isPopular
                )
            }

            //  Category (DB entity) to Category (UI)
            override fun doBackward(dbEntity: BusinessCategory): Category {
                return Category(
                    id = dbEntity.id,
                    name = dbEntity.name,
                    type = dbEntity.type,
                    imageUrl = dbEntity.imageUrl,
                    isPopular = dbEntity.isPopular
                )
            }
        }
}
