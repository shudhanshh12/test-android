package `in`.okcredit.communication_inappnotification.local

import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class InAppNotification(
    @PrimaryKey val id: String,
    val screenName: String,
    val notificationJson: String,
    val displayStatus: DisplayStatus,
    @ColumnInfo(index = true) val businessId: String
)
