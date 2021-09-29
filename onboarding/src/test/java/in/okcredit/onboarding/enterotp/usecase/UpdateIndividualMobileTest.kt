package `in`.okcredit.onboarding.enterotp.usecase

import `in`.okcredit.individual.contract.GetIndividual
import `in`.okcredit.individual.contract.model.Individual
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.auth.AuthService
import java.util.concurrent.Callable

class UpdateIndividualMobileTest {
    private val updateIndividualMobile: `in`.okcredit.individual.contract.UpdateIndividualMobile = mock()
    private val authService: AuthService = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getIndividual: GetIndividual = mock()
    private val updateMerchantNumber =
        UpdateIndividualMobile({ updateIndividualMobile }, authService, { getActiveBusinessId }, { getIndividual })

    @Before
    fun setup() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { scheduler: Callable<Scheduler?>? -> Schedulers.trampoline() }

        mockkStatic(Dispatchers::class)
        every { Dispatchers.Default } returns Dispatchers.Unconfined
    }

    @Test
    fun `show update without error`() {
        val testCurrentMobileOTPToken = "cdsbjchvdsmcv"
        val testNewMobileOTPToken = "cdsbjchvdsmcv"
        val businessId = "business-id"
        val individualId = "individual-id"
        val individual = mock<Individual>().apply { whenever(this.id).thenReturn(individualId) }

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(getIndividual.execute())
            .thenReturn(flowOf(individual))

        whenever(authService.getCurrentMobileOtpToken())
            .thenReturn(testCurrentMobileOTPToken)

        whenever(authService.getNewMobileOtpToken())
            .thenReturn(testNewMobileOTPToken)

        val testObserver = updateMerchantNumber.execute("9833426881").test()

        testObserver.assertComplete()
        verify(authService, times(1)).getCurrentMobileOtpToken()
        verify(authService, times(1)).getNewMobileOtpToken()

        testObserver.dispose()
    }
}
