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
import tech.okcredit.android.ab.server.AbRemoteSource
import tech.okcredit.android.auth.AuthService

class AcknowledgeExperimentTest {

    private val authService: AuthService = mock()
    private val deviceRepository: DeviceRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val remoteSource: AbRemoteSource = mock()
    private val acknowledgeExperiment = AcknowledgeExperiment(
        { authService },
        { deviceRepository },
        { remoteSource },
        { getActiveBusinessId }
    )
    private val deviceId = "deviceId"
    private val businessId = "businessId"

    private val experimentName: String = "sample-expt"
    private val experimentVariant: String = "variant-1"
    private val experimentStatus: Int = 0
    private val acknowledgeTime: Long = 1L

    @Before
    fun setup() {
        val device = mock<Device>().apply { whenever(this.id).thenReturn(deviceId) }
        whenever(deviceRepository.deviceDeprecated).thenReturn(device)
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
    }

    @Test
    fun `given user is authenticated should call acknowledgeExperiment`() {
        whenever(authService.isAuthenticated()).thenReturn(true)
        whenever(
            remoteSource.acknowledgeExperiment(
                deviceId, experimentName, experimentVariant, experimentStatus, acknowledgeTime, businessId
            )
        ).thenReturn(Completable.complete())

        val testObserver = acknowledgeExperiment
            .execute(experimentName, experimentVariant, experimentStatus, acknowledgeTime, businessId).test()

        testObserver.assertComplete()
        verify(authService).isAuthenticated()
        verify(remoteSource).acknowledgeExperiment(
            deviceId, experimentName, experimentVariant, experimentStatus, acknowledgeTime, businessId
        )
    }

    @Test
    fun `given user is not authenticated should call deviceAcknowledgeExperiment`() {
        whenever(authService.isAuthenticated()).thenReturn(false)
        whenever(
            remoteSource.deviceAcknowledgeExperiment(
                deviceId, experimentName, experimentVariant, experimentStatus, acknowledgeTime
            )
        ).thenReturn(Completable.complete())

        val testObserver = acknowledgeExperiment
            .execute(experimentName, experimentVariant, experimentStatus, acknowledgeTime, businessId).test()

        testObserver.assertComplete()
        verify(authService).isAuthenticated()
        verify(remoteSource).deviceAcknowledgeExperiment(
            deviceId, experimentName, experimentVariant, experimentStatus, acknowledgeTime
        )
    }
}
