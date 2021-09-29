package tech.okcredit.android.ab.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.device.Device
import `in`.okcredit.merchant.device.DeviceRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.Profile
import tech.okcredit.android.ab.server.AbRemoteSource
import tech.okcredit.android.ab.store.AbLocalSource
import tech.okcredit.android.auth.AuthService

class SyncAbProfileTest {

    private val authService: AuthService = mock()
    private val deviceRepository: DeviceRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val localSource: AbLocalSource = mock()
    private val remoteSource: AbRemoteSource = mock()
    private val syncAbProfile = SyncAbProfile(
        { authService },
        { deviceRepository },
        { getActiveBusinessId },
        { localSource },
        { remoteSource }
    )
    private val deviceId = "deviceId"
    private val businessId = "businessId"

    @Before
    fun setup() {
        val device = mock<Device>().apply { whenever(this.id).thenReturn(deviceId) }
        whenever(deviceRepository.deviceDeprecated).thenReturn(device)
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
    }

    @Test
    fun `given user is authenticated should call getProfile`() {
        whenever(authService.isAuthenticated()).thenReturn(true)
        val profile = mock<Profile>()
        whenever(remoteSource.getProfile(deviceId, businessId, "app_open")).thenReturn(Single.just(profile))
        whenever(localSource.setProfile(profile, businessId)).thenReturn(Completable.complete())

        val testObserver = syncAbProfile.execute(businessId, "app_open").test()

        testObserver.assertComplete()
        verify(authService).isAuthenticated()
        verify(remoteSource).getProfile(deviceId, businessId, "app_open")
        verify(localSource).setProfile(profile, businessId)
    }

    @Test
    fun `given user is not authenticated should call getDeviceProfile`() {
        whenever(authService.isAuthenticated()).thenReturn(false)
        val profile = mock<Profile>()
        whenever(remoteSource.getDeviceProfile(deviceId, "app_open")).thenReturn(Single.just(profile))
        whenever(localSource.setProfile(profile, businessId)).thenReturn(Completable.complete())

        val testObserver = syncAbProfile.execute(businessId, "app_open").test()

        testObserver.assertComplete()
        verify(authService).isAuthenticated()
        verify(remoteSource).getDeviceProfile(deviceId, "app_open")
        verify(localSource).setProfile(profile, businessId)
    }
}
