package `in`.okcredit.communication_inappnotification.usecase

import `in`.okcredit.communication_inappnotification.analytics.InAppNotificationTracker
import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import `in`.okcredit.communication_inappnotification.local.InAppNotificationLocalSource
import `in`.okcredit.communication_inappnotification.remote.InAppNotificationRemoteSource
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Test
import tech.okcredit.android.base.workmanager.OkcWorkManager

class InAppNotificationsSyncerTest {

    private val remoteSource: InAppNotificationRemoteSource = mock()
    private val localSource: InAppNotificationLocalSource = mock()
    private val workManager: OkcWorkManager = mock()
    private val tracker: InAppNotificationTracker = mock()
    private val inAppNotificationsSyncer = InAppNotificationsSyncer(
        { remoteSource },
        { localSource },
        { workManager },
        { tracker }
    )

    private val notification1 = mock<InAppNotification>().apply { whenever(id).thenReturn("id1") }
    private val notification2 = mock<InAppNotification>().apply { whenever(id).thenReturn("id2") }
    private val notification3 = mock<InAppNotification>().apply { whenever(id).thenReturn("id3") }
    private val notification4 = mock<InAppNotification>().apply { whenever(id).thenReturn("id4") }
    private val notification5 = mock<InAppNotification>().apply { whenever(id).thenReturn("id5") }
    private val notification6 = mock<InAppNotification>().apply { whenever(id).thenReturn("id6") }
    private val notification7 = mock<InAppNotification>().apply { whenever(id).thenReturn("id7") }

    @Test
    fun `given empty notification list in local execute should getNotifications on remote and replaceNotifications on local`() {
        runBlocking {
            // given
            val notificationsFromRemote = listOf(notification1, notification2, notification3)
            val notificationsFromLocal = listOf<InAppNotification>()
            val businessId = "business-id"
            whenever(remoteSource.getNotifications(businessId)).thenReturn(notificationsFromRemote)
            whenever(localSource.getAllNotifications(businessId)).thenReturn(notificationsFromLocal)

            // when
            inAppNotificationsSyncer.execute(businessId)

            // then
            verify(localSource).replaceNotifications(notificationsFromRemote, businessId)
            verify(tracker, times(notificationsFromRemote.size))
                .trackNotificationReceived(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
        }
    }

    @Test
    fun `given no new notifications received execute should getNotifications on remote and replaceNotifications on local`() {
        runBlocking {
            // given
            val notificationsFromRemote = listOf(notification1, notification2, notification3)
            val notificationsFromLocal = listOf(notification1, notification2, notification3)
            val businessId = "business-id"
            whenever(remoteSource.getNotifications(businessId)).thenReturn(notificationsFromRemote)
            whenever(localSource.getAllNotifications(businessId)).thenReturn(notificationsFromLocal)

            // when
            inAppNotificationsSyncer.execute(businessId)

            // then
            verify(localSource).replaceNotifications(notificationsFromRemote, businessId)
            verify(tracker, times(0))
                .trackNotificationReceived(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
        }
    }

    @Test
    fun `given 2 new notifications received execute should getNotifications on remote and replaceNotifications on local`() {
        runBlocking {
            // given
            val notificationsFromRemote = listOf(notification3, notification4, notification5)
            val notificationsFromLocal = listOf(notification1, notification2, notification3)
            val businessId = "business-id"
            whenever(remoteSource.getNotifications(businessId)).thenReturn(notificationsFromRemote)
            whenever(localSource.getAllNotifications(businessId)).thenReturn(notificationsFromLocal)

            // when
            inAppNotificationsSyncer.execute(businessId)

            // then
            verify(localSource).replaceNotifications(notificationsFromRemote, businessId)
            verify(tracker, times(2))
                .trackNotificationReceived(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
        }
    }

    @Test
    fun `given all new notifications received execute should getNotifications on remote and replaceNotifications on local`() {
        runBlocking {
            // given
            val notificationsFromRemote = listOf(notification4, notification5, notification6, notification7)
            val notificationsFromLocal = listOf(notification1, notification2, notification3)
            val businessId = "business-id"
            whenever(remoteSource.getNotifications(businessId)).thenReturn(notificationsFromRemote)
            whenever(localSource.getAllNotifications(businessId)).thenReturn(notificationsFromLocal)

            // when
            inAppNotificationsSyncer.execute(businessId)

            // then
            verify(localSource).replaceNotifications(notificationsFromRemote, businessId)
            verify(tracker, times(notificationsFromRemote.size))
                .trackNotificationReceived(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
        }
    }
}
