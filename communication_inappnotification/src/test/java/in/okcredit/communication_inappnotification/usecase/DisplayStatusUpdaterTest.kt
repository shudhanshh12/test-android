package `in`.okcredit.communication_inappnotification.usecase

import `in`.okcredit.communication_inappnotification.analytics.InAppNotificationTracker
import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import `in`.okcredit.communication_inappnotification.local.InAppNotificationLocalSource
import `in`.okcredit.communication_inappnotification.local.InAppNotificationPreferences
import `in`.okcredit.communication_inappnotification.remote.InAppNotificationRemoteSource
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.junit.Test
import tech.okcredit.android.base.workmanager.OkcWorkManager

class DisplayStatusUpdaterTest {

    private val localSource: InAppNotificationLocalSource = mock()
    private val remoteSource: InAppNotificationRemoteSource = mock()
    private val preferences: InAppNotificationPreferences = mock()
    private val workManager: OkcWorkManager = mock()
    private val inAppNotificationTracker: InAppNotificationTracker = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val displayStatusUpdater = DisplayStatusUpdater(
        { localSource },
        { remoteSource },
        { preferences },
        { workManager },
        { inAppNotificationTracker },
        { getActiveBusinessId }
    )

    @Test
    fun `given acknowledgement successful executeUpdateStatusOnRemote should acknowledgeNotifications on remote`() {
        runBlocking {
            // given
            val ids = listOf("id1", "id2")
            val notification1 = mock<InAppNotification>().apply {
                whenever(this.id).thenReturn(ids[0])
            }
            val notification2 = mock<InAppNotification>().apply {
                whenever(this.id).thenReturn(ids[1])
            }
            val notifications = listOf(notification1, notification2)
            val businessId = "business-id"
            whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
            whenever(localSource.getNotificationsToBeSynced(businessId)).thenReturn(notifications)
            whenever(remoteSource.acknowledgeNotifications(ids, businessId)).thenReturn(true)

            // when
            displayStatusUpdater.executeUpdateStatusOnRemote(businessId)

            // then
            verify(localSource).getNotificationsToBeSynced(businessId)
            verify(localSource).clearNotifications(ids)
            verify(remoteSource).acknowledgeNotifications(ids, businessId)
            verify(inAppNotificationTracker, times(ids.size)).trackNotificationAcknowledged(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        }
    }

    @Test
    fun `given acknowledgement not successful executeUpdateStatusOnRemote should not acknowledgeNotifications on remote`() {
        runBlocking {
            // given
            val ids = listOf("id1", "id2")
            val notification1 = mock<InAppNotification>().apply {
                whenever(this.id).thenReturn(ids[0])
            }
            val notification2 = mock<InAppNotification>().apply {
                whenever(this.id).thenReturn(ids[1])
            }
            val notifications = listOf(notification1, notification2)
            val businessId = "business-id"
            whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
            whenever(localSource.getNotificationsToBeSynced(businessId)).thenReturn(notifications)
            whenever(remoteSource.acknowledgeNotifications(ids, businessId)).thenReturn(false)

            // when
            displayStatusUpdater.executeUpdateStatusOnRemote(businessId)

            // then
            verify(localSource).getNotificationsToBeSynced(businessId)
            verify(localSource, times(0)).clearNotifications(ids)
            verify(remoteSource).acknowledgeNotifications(ids, businessId)
            verify(inAppNotificationTracker, times(0)).trackNotificationAcknowledged(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        }
    }

    @Test
    fun `given no notification to be synced executeUpdateStatusOnRemote should not acknowledgeNotifications on remote`() {
        runBlocking {
            // given
            val notifications = listOf<InAppNotification>()
            val businessId = "business-id"
            whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
            whenever(localSource.getNotificationsToBeSynced(businessId)).thenReturn(notifications)
            whenever(remoteSource.acknowledgeNotifications(anyOrNull(), eq(businessId))).thenReturn(true)

            // when
            displayStatusUpdater.executeUpdateStatusOnRemote(businessId)

            // then
            verify(localSource).getNotificationsToBeSynced(businessId)
            verify(localSource, times(0)).clearNotifications(anyOrNull())
            verify(remoteSource, times(0)).acknowledgeNotifications(anyOrNull(), eq(businessId))
            verify(inAppNotificationTracker, times(0)).trackNotificationAcknowledged(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        }
    }
}
