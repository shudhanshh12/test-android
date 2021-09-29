package `in`.okcredit.communication_inappnotification.usecase

import `in`.okcredit.communication_inappnotification.BuildConfig
import `in`.okcredit.communication_inappnotification.analytics.InAppNotificationTracker
import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import `in`.okcredit.communication_inappnotification.contract.InAppNotificationHandler.Companion.NOTIFICATION_COUNT_PER_DAY
import `in`.okcredit.communication_inappnotification.contract.ui.remote.TapTarget
import `in`.okcredit.communication_inappnotification.contract.ui.remote.Tooltip
import `in`.okcredit.communication_inappnotification.exception.RendererNotFoundException
import `in`.okcredit.communication_inappnotification.local.InAppNotificationLocalSource
import `in`.okcredit.communication_inappnotification.local.InAppNotificationPreferences
import `in`.okcredit.communication_inappnotification.local.InAppNotificationPreferences.Keys.PREF_BUSINESS_DATE
import `in`.okcredit.communication_inappnotification.local.InAppNotificationPreferences.Keys.PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT
import `in`.okcredit.communication_inappnotification.model.DisplayStatus
import `in`.okcredit.communication_inappnotification.model.TapTarget
import `in`.okcredit.communication_inappnotification.model.Tooltip
import `in`.okcredit.communication_inappnotification.usecase.render.RemoteInAppNotificationRenderer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.utils.TimeUtils.toSeconds
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.common.truth.Truth.assertThat
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.datetime.epoch
import java.lang.ref.WeakReference

class InAppNotificationHandlerImplTest {

    private val dispatcherProvider: DispatcherProvider = mock()
    private val localSource: InAppNotificationLocalSource = mock()
    private val preferences: InAppNotificationPreferences = mock()
    private val renderers: Map<String, @JvmSuppressWildcards RemoteInAppNotificationRenderer> = mock()
    private val tracker: InAppNotificationTracker = mock()
    private val displayStatusUpdater: DisplayStatusUpdater = mock()
    private val firebaseRemoteConfig: FirebaseRemoteConfig = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val dailyLimit = 5L
    private var inAppNotificationHandlerImpl: RemoteInAppNotificationHandlerImpl
    private val businessId = "business-id"

    init {
        whenever(firebaseRemoteConfig.getLong(NOTIFICATION_COUNT_PER_DAY)).thenReturn(dailyLimit)
        inAppNotificationHandlerImpl = RemoteInAppNotificationHandlerImpl(
            dispatcherProvider = { dispatcherProvider },
            localSource = { localSource },
            preferences = { preferences },
            renderers = { renderers },
            tracker = { tracker },
            displayStatusUpdater = { displayStatusUpdater },
            firebaseRemoteConfig = { firebaseRemoteConfig },
            getActiveBusinessId = { getActiveBusinessId }
        )
    }

    @Before
    fun setUp() {
        whenever(dispatcherProvider.io()).thenReturn(Dispatchers.Unconfined)
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
    }

    @Test
    fun `given no notifications to display then render and statusUpdater should not be called`() = runBlocking {
        // given
        val screenName = "screen-name"
        whenever(localSource.getNotificationsNotDisplayedForScreen(screenName, businessId)).thenReturn(emptyList())

        // when
        inAppNotificationHandlerImpl.execute(screenName, mock(), mock())

        // then
        verify(getActiveBusinessId).execute()
        verify(preferences, times(0)).getLong(eq(PREF_BUSINESS_DATE), any(), anyOrNull())
        verify(preferences, times(0)).getInt(eq(PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT), any(), anyOrNull())
        verify(renderers, times(0))[any()]
        verify(displayStatusUpdater, times(0)).execute(eq(screenName), any(), eq(businessId))
    }

    @Test
    fun `given daily notification limit is exhausted then render and statusUpdater should not be called`() =
        runBlocking {
            // given
            val screenName = "screen-name"
            whenever(localSource.getNotificationsNotDisplayedForScreen(screenName, businessId)).thenReturn(listOf(mock()))
            val date = DateTime().withTimeAtStartOfDay().epoch
            whenever(preferences.getLong(eq(PREF_BUSINESS_DATE), any(), anyOrNull())).thenReturn(flowOf(date))
            whenever(preferences.getInt(eq(PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT), any(), anyOrNull()))
                .thenReturn(flowOf(dailyLimit.toInt()))

            // when
            inAppNotificationHandlerImpl.execute(screenName, mock(), mock())

            // then
            verify(getActiveBusinessId).execute()
            verify(preferences).getLong(eq(PREF_BUSINESS_DATE), any(), anyOrNull())
            verify(preferences).getInt(eq(PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT), any(), anyOrNull())
            verify(renderers, times(0))[any()]
            verify(displayStatusUpdater, times(0)).execute(eq(screenName), any(), eq(businessId))
        }

    @Test
    fun `given notification does not fulfill minBuildNumber constraint then render should not be called`() =
        runBlocking {
            // given
            val screenName = "screen-name"
            val id = "id"
            val kind = "kind"
            val name = "name"
            val currentAppBuildNumber = BuildConfig.VERSION_CODE.toInt()
            val minAppBuildNumber = currentAppBuildNumber + 1
            val notification1 = mock<Tooltip>().apply {
                whenever(this.minAppBuildNumber).thenReturn(minAppBuildNumber)
                whenever(this.maxAppBuildNumber).thenReturn(currentAppBuildNumber)
                whenever(this.id).thenReturn(id)
                whenever(this.kind).thenReturn(kind)
                whenever(this.getTypeForAnalyticsTracking()).thenReturn(kind)
                whenever(this.screenName).thenReturn(screenName)
                whenever(this.name).thenReturn(name)
            }
            whenever(localSource.getNotificationsNotDisplayedForScreen(screenName, businessId)).thenReturn(listOf(notification1))
            val date = DateTime().withTimeAtStartOfDay().epoch
            whenever(preferences.getLong(eq(PREF_BUSINESS_DATE), any(), anyOrNull())).thenReturn(flowOf(date))
            whenever(
                preferences.getInt(
                    eq(PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT),
                    any(),
                    anyOrNull()
                )
            ).thenReturn(flowOf(0))

            // when
            inAppNotificationHandlerImpl.execute(screenName, mock(), mock())

            // then
            verify(getActiveBusinessId).execute()
            verify(preferences).getLong(eq(PREF_BUSINESS_DATE), any(), anyOrNull())
            verify(preferences).getInt(eq(PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT), any(), anyOrNull())
            verify(renderers, times(0))[any()]
            verify(displayStatusUpdater).execute(id, DisplayStatus.NOT_DISPLAYED, businessId)
            verify(tracker).trackNotificationNotDisplayed(
                notificationId = id,
                type = kind,
                screenName = screenName,
                reason = "AppVersion=$currentAppBuildNumber,($minAppBuildNumber,$currentAppBuildNumber)",
                name = name
            )
        }

    @Test
    fun `given notification does not fulfill maxBuildNumber constraint then render should not be called`() =
        runBlocking {
            // given
            val screenName = "screen-name"
            val id = "id"
            val kind = "kind"
            val name = "name"
            val currentAppBuildNumber = BuildConfig.VERSION_CODE.toInt()
            val maxAppBuildNumber = currentAppBuildNumber - 1
            val notification1 = mock<Tooltip>().apply {
                whenever(this.minAppBuildNumber).thenReturn(currentAppBuildNumber)
                whenever(this.maxAppBuildNumber).thenReturn(maxAppBuildNumber)
                whenever(this.id).thenReturn(id)
                whenever(this.kind).thenReturn(kind)
                whenever(this.getTypeForAnalyticsTracking()).thenReturn(kind)
                whenever(this.screenName).thenReturn(screenName)
                whenever(this.name).thenReturn(name)
            }
            whenever(localSource.getNotificationsNotDisplayedForScreen(screenName, businessId)).thenReturn(listOf(notification1))
            val date = DateTime().withTimeAtStartOfDay().epoch
            whenever(preferences.getLong(eq(PREF_BUSINESS_DATE), any(), anyOrNull())).thenReturn(flowOf(date))
            whenever(
                preferences.getInt(
                    eq(PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT),
                    any(),
                    anyOrNull()
                )
            ).thenReturn(flowOf(0))

            // when
            inAppNotificationHandlerImpl.execute(screenName, mock(), mock())

            // then
            verify(getActiveBusinessId).execute()
            verify(preferences).getLong(eq(PREF_BUSINESS_DATE), any(), anyOrNull())
            verify(preferences).getInt(eq(PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT), any(), anyOrNull())
            verify(renderers, times(0))[any()]
            verify(displayStatusUpdater).execute(id, DisplayStatus.NOT_DISPLAYED, businessId)
            verify(tracker).trackNotificationNotDisplayed(
                notificationId = id,
                type = kind,
                screenName = screenName,
                reason = "AppVersion=$currentAppBuildNumber,($currentAppBuildNumber,$maxAppBuildNumber)",
                name = name
            )
        }

    @Test
    fun `given notification does not fulfill expiryTime constraint then render should not be called`() =
        runBlocking {
            // given
            val screenName = "screen-name"
            val id = "id"
            val kind = "kind"
            val name = "name"
            val expiryTime = System.currentTimeMillis().toSeconds() - 1
            val currentAppBuildNumber = BuildConfig.VERSION_CODE.toInt()
            val notification1 = mock<Tooltip>().apply {
                whenever(this.minAppBuildNumber).thenReturn(currentAppBuildNumber)
                whenever(this.maxAppBuildNumber).thenReturn(currentAppBuildNumber)
                whenever(this.expiryTime).thenReturn(expiryTime)
                whenever(this.id).thenReturn(id)
                whenever(this.kind).thenReturn(kind)
                whenever(this.getTypeForAnalyticsTracking()).thenReturn(kind)
                whenever(this.screenName).thenReturn(screenName)
                whenever(this.name).thenReturn(name)
            }
            whenever(localSource.getNotificationsNotDisplayedForScreen(screenName, businessId)).thenReturn(listOf(notification1))
            val date = DateTime().withTimeAtStartOfDay().epoch
            whenever(preferences.getLong(eq(PREF_BUSINESS_DATE), any(), anyOrNull())).thenReturn(flowOf(date))
            whenever(
                preferences.getInt(
                    eq(PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT),
                    any(),
                    anyOrNull()
                )
            ).thenReturn(flowOf(0))

            // when
            inAppNotificationHandlerImpl.execute(screenName, mock(), mock())

            // then
            verify(getActiveBusinessId).execute()
            verify(preferences).getLong(eq(PREF_BUSINESS_DATE), any(), anyOrNull())
            verify(preferences).getInt(eq(PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT), any(), anyOrNull())
            verify(renderers, times(0))[any()]
            verify(displayStatusUpdater).execute(id, DisplayStatus.NOT_DISPLAYED, businessId)
            verify(tracker).trackNotificationNotDisplayed(
                notificationId = id,
                type = kind,
                screenName = screenName,
                reason = "ExpiryTime=$expiryTime",
                name = name
            )
        }

    @Test
    fun `given two notifications when one notification has higher priority is displayed then render should be called`() =
        runBlocking {
            // given
            val screenName = "screen-name"
            val id1 = "id1"
            val id2 = "id2"
            val kind1 = "kind1"
            val kind2 = "kind2"
            val expiryTime = System.currentTimeMillis().toSeconds() + 100
            val currentAppBuildNumber = BuildConfig.VERSION_CODE.toInt()
            val notification1 = mock<Tooltip>().apply {
                whenever(this.minAppBuildNumber).thenReturn(currentAppBuildNumber)
                whenever(this.maxAppBuildNumber).thenReturn(currentAppBuildNumber)
                whenever(this.expiryTime).thenReturn(expiryTime)
                whenever(this.id).thenReturn(id1)
                whenever(this.kind).thenReturn(kind1)
                whenever(this.screenName).thenReturn(screenName)
                whenever(this.priority).thenReturn(2)
            }
            val notification2 = mock<TapTarget>().apply {
                whenever(this.minAppBuildNumber).thenReturn(currentAppBuildNumber)
                whenever(this.maxAppBuildNumber).thenReturn(currentAppBuildNumber)
                whenever(this.expiryTime).thenReturn(expiryTime)
                whenever(this.id).thenReturn(id2)
                whenever(this.kind).thenReturn(kind2)
                whenever(this.screenName).thenReturn(screenName)
                whenever(this.priority).thenReturn(3)
            }
            whenever(localSource.getNotificationsNotDisplayedForScreen(screenName, businessId))
                .thenReturn(listOf(notification1, notification2))
            val date = DateTime().withTimeAtStartOfDay().epoch
            whenever(preferences.getLong(eq(PREF_BUSINESS_DATE), any(), anyOrNull())).thenReturn(flowOf(date))
            whenever(
                preferences.getInt(
                    eq(PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT),
                    any(),
                    anyOrNull()
                )
            ).thenReturn(flowOf(0))
            val renderer = mock<RemoteInAppNotificationRenderer>()
            whenever(renderers[kind2]).thenReturn(renderer)
            val weakScreen = mock<WeakReference<FragmentActivity>>()
            val weakView = mock<WeakReference<View>>()
            whenever(renderer.renderRemoteNotification(weakScreen, weakView, notification2)).thenReturn(DisplayStatus.DISPLAYED)

            // when
            inAppNotificationHandlerImpl.execute(screenName, weakScreen, weakView)

            // then
            verify(getActiveBusinessId).execute()
            verify(preferences).getLong(eq(PREF_BUSINESS_DATE), any(), anyOrNull())
            verify(preferences).getInt(eq(PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT), any(), anyOrNull())
            verify(renderers)[kind2]
            verify(displayStatusUpdater).execute(id2, DisplayStatus.DISPLAYED, businessId)
        }

    @Test
    fun `given two notifications when one notification has higher priority is not displayed then render should be called`() =
        runBlocking {
            // given
            val screenName = "screen-name"
            val id1 = "id1"
            val id2 = "id2"
            val kind1 = "kind1"
            val kind2 = "kind2"
            val expiryTime = System.currentTimeMillis().toSeconds()
            val currentAppBuildNumber = BuildConfig.VERSION_CODE.toInt()
            val notification1 = mock<Tooltip>().apply {
                whenever(this.minAppBuildNumber).thenReturn(currentAppBuildNumber)
                whenever(this.maxAppBuildNumber).thenReturn(currentAppBuildNumber)
                whenever(this.expiryTime).thenReturn(expiryTime)
                whenever(this.id).thenReturn(id1)
                whenever(this.kind).thenReturn(kind1)
                whenever(this.screenName).thenReturn(screenName)
                whenever(this.priority).thenReturn(2)
            }
            val notification2 = mock<TapTarget>().apply {
                whenever(this.minAppBuildNumber).thenReturn(currentAppBuildNumber)
                whenever(this.maxAppBuildNumber).thenReturn(currentAppBuildNumber)
                whenever(this.expiryTime).thenReturn(expiryTime)
                whenever(this.id).thenReturn(id2)
                whenever(this.kind).thenReturn(kind2)
                whenever(this.screenName).thenReturn(screenName)
                whenever(this.priority).thenReturn(3)
            }
            whenever(localSource.getNotificationsNotDisplayedForScreen(screenName, businessId))
                .thenReturn(listOf(notification1, notification2))
            val date = DateTime().withTimeAtStartOfDay().epoch
            whenever(preferences.getLong(eq(PREF_BUSINESS_DATE), any(), anyOrNull())).thenReturn(flowOf(date))
            whenever(
                preferences.getInt(
                    eq(PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT),
                    any(),
                    anyOrNull()
                )
            ).thenReturn(flowOf(0))
            val renderer = mock<RemoteInAppNotificationRenderer>()
            whenever(renderers[kind2]).thenReturn(renderer)
            val weakScreen = mock<WeakReference<FragmentActivity>>()
            val weakView = mock<WeakReference<View>>()
            whenever(renderer.renderRemoteNotification(weakScreen, weakView, notification2)).thenReturn(DisplayStatus.NOT_DISPLAYED)

            // when
            inAppNotificationHandlerImpl.execute(screenName, weakScreen, weakView)

            // then
            verify(getActiveBusinessId).execute()
            verify(preferences).getLong(eq(PREF_BUSINESS_DATE), any(), anyOrNull())
            verify(preferences).getInt(eq(PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT), any(), anyOrNull())
            verify(displayStatusUpdater).execute(id2, DisplayStatus.NOT_DISPLAYED, businessId)
        }

    @Test
    fun `given notifications with unsupported kind then render should not be called`() =
        runBlocking {
            // given
            val screenName = "screen-name"
            val id1 = "id1"
            val kind1 = "kind1"
            val name = "name"
            val expiryTime = System.currentTimeMillis().toSeconds()
            val currentAppBuildNumber = BuildConfig.VERSION_CODE.toInt()
            val notification1 = mock<Tooltip>().apply {
                whenever(this.minAppBuildNumber).thenReturn(currentAppBuildNumber)
                whenever(this.maxAppBuildNumber).thenReturn(currentAppBuildNumber)
                whenever(this.expiryTime).thenReturn(expiryTime)
                whenever(this.id).thenReturn(id1)
                whenever(this.kind).thenReturn(kind1)
                whenever(this.getTypeForAnalyticsTracking()).thenReturn(kind1)
                whenever(this.screenName).thenReturn(screenName)
                whenever(this.priority).thenReturn(2)
                whenever(this.name).thenReturn(name)
            }
            whenever(localSource.getNotificationsNotDisplayedForScreen(screenName, businessId)).thenReturn(listOf(notification1))
            val date = DateTime().withTimeAtStartOfDay().epoch - 1
            whenever(preferences.getLong(eq(PREF_BUSINESS_DATE), any(), anyOrNull())).thenReturn(flowOf(date))
            whenever(
                preferences.getInt(
                    eq(PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT),
                    any(),
                    anyOrNull()
                )
            ).thenReturn(flowOf(0))
            whenever(renderers[kind1]).thenReturn(null)

            // when
            inAppNotificationHandlerImpl.execute(screenName, mock(), mock())

            // then
            verify(getActiveBusinessId).execute()
            verify(preferences).getLong(eq(PREF_BUSINESS_DATE), any(), anyOrNull())
            verify(preferences, times(0)).getInt(eq(PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT), any(), anyOrNull())
            verify(renderers)[kind1]
            verify(displayStatusUpdater, times(0)).execute(any(), any(), eq(businessId))
        }

    @Test
    fun `given RendererNotFoundException then exceptionHandler should track exception`() =
        runBlocking {
            // given
            val message = "message"

            // when
            val handler = inAppNotificationHandlerImpl.getExceptionHandler()
            handler.handleException(mock(), RendererNotFoundException(message))

            // then
            val exceptionCaptor = argumentCaptor<Exception>()
            verify(tracker).trackException(exceptionCaptor.capture())
            assertThat(exceptionCaptor.firstValue).isInstanceOf(RendererNotFoundException::class.java)
            assertThat(exceptionCaptor.firstValue.message).isEqualTo(message)
        }
}
