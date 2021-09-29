package `in`.okcredit.communication_inappnotification.local

import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface InAppNotificationDatabaseDao {

    // Replace only the notifications which are not yet displayed.
    @androidx.room.Transaction
    suspend fun replaceNotifications(notifications: Array<InAppNotification>, businessId: String) {
        clearNotDisplayedNotifications(businessId = businessId)
        insertNotifications(*notifications)
    }

    @Query("DELETE FROM InAppNotification WHERE displayStatus != :displayStatus AND businessId = :businessId")
    suspend fun clearNotDisplayedNotifications(
        displayStatus: DisplayStatus = DisplayStatus.DISPLAYED,
        businessId: String,
    )

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotifications(vararg notifications: InAppNotification)

    @Query("DELETE FROM InAppNotification")
    suspend fun clearNotificationTable()

    @Query("SELECT * FROM InAppNotification WHERE screenName = :screenName AND displayStatus != :displayStatus AND businessId = :businessId")
    suspend fun getNotificationsNotDisplayedForScreen(
        screenName: String,
        displayStatus: DisplayStatus,
        businessId: String,
    ): List<InAppNotification>

    @Query("UPDATE InAppNotification SET displayStatus = :displayStatus WHERE id = :notificationId")
    suspend fun updateNotificationDisplayStatus(notificationId: String, displayStatus: DisplayStatus)

    @Query("SELECT * FROM InAppNotification WHERE displayStatus == :displayStatus and businessId = :businessId")
    suspend fun getNotificationsToBeSynced(displayStatus: DisplayStatus, businessId: String): List<InAppNotification>

    @Query("SELECT * FROM InAppNotification WHERE businessId = :businessId")
    suspend fun getAllNotifications(businessId: String): List<InAppNotification>

    @Query("DELETE FROM InAppNotification WHERE id IN (:notificationIds)")
    suspend fun clearNotifications(notificationIds: Array<String>)
}
