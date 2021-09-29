package `in`.okcredit.merchant.store.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime

@Entity
data class Business(
    @PrimaryKey
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
    var category: BusinessCategory? = null,
    var business: BusinessType? = null,
    var isFirst: Boolean = false,
)

@Entity
data class BusinessType(
    @PrimaryKey
    val id: String,
    val name: String? = null,
    val image_url: String? = null,
    val title: String? = null,
    val sub_title: String? = null,
)

@Entity
data class BusinessCategory(
    @PrimaryKey
    var id: String,
    var name: String,
    var type: Int,
    var imageUrl: String? = null,
    var isPopular: Boolean,
)

@Deprecated("Moved to IndividualPreferences")
@Entity(primaryKeys = ["businessId", "key"])
data class BusinessPreference(
    var businessId: String,
    var key: String,
    var value: String,
)
