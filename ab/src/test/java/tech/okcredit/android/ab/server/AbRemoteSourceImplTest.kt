package tech.okcredit.android.ab.server

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AbRemoteSourceImplTest {

    private val abApiClient: AbApiClient = mock()
    private val abServerImpl = AbRemoteSourceImpl { abApiClient }

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.io() } returns Schedulers.trampoline()
    }

    @Test
    fun `getProfile() should call getProfile on apiClient on success to call toProfile()`() {
        val deviceId = "device-id"
        val businessId = "business-id"
        val features = mapOf("feature1" to true, "feature2" to false)
        val experiment = mapOf("experiment1" to mock<Experiment>())
        val profile = Profile(businessId, features, experiment)
        val response = GetProfileResponse(profile = profile)
        whenever(abApiClient.getProfile(deviceId, businessId, "sync", "app_open")).thenReturn(Single.just(Response.success(response)))

        val testObserver = abServerImpl.getProfile(deviceId, businessId, "app_open").test()

        testObserver.assertValue(profile.toProfile())
        verify(abApiClient).getProfile(deviceId, businessId, "sync", "app_open")
    }

    @Test
    fun `getProfile() should call getProfile on apiClient on null profile should return empty profile`() {
        val deviceId = "device-id"
        val businessId = "business-id"
        val response = mock<GetProfileResponse>().apply {
            whenever(this.profile).thenReturn(null)
        }
        whenever(
            abApiClient.getProfile(
                deviceId,
                businessId,
                "sync",
                "app_open"
            )
        ).thenReturn(Single.just(Response.success(response)))

        val testObserver = abServerImpl.getProfile(deviceId, businessId, "app_open").test()

        testObserver.assertValue(tech.okcredit.android.ab.Profile(hashMapOf()))
        verify(abApiClient).getProfile(deviceId, businessId, "sync", "app_open")
    }

    @Test
    fun `getProfile() should call getProfile on apiClient on error should throw ApiError`() {
        val deviceId = "device-id"
        val businessId = "business-id"
        val mockError: Error = mock()
        whenever(abApiClient.getProfile(deviceId, businessId, "sync", "app_open")).thenReturn(Single.error(mockError))

        val testObserver = abServerImpl.getProfile(deviceId, businessId, "app_open").test()

        testObserver.assertError(mockError)
        verify(abApiClient).getProfile(deviceId, businessId, "sync", "app_open")
    }
}
