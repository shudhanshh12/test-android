package `in`.okcredit.merchant.rewards.store.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime

@Entity
data class Rewards(
    @PrimaryKey
    val id: String,
    var createTime: DateTime,
    var updateTime: DateTime,
    var status: String,
    var rewardType: String?,
    var amount: Long = 0,
    var featureName: String?,
    var featureTitle: String?,
    var description: String?,
    var deepLink: String?,
    var icon: String?,
    var labels: String,
    var createdBy: String,
)
