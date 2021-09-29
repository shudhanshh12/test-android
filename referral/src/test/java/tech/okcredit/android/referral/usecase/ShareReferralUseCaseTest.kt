package tech.okcredit.android.referral.usecase

import `in`.okcredit.referral.contract.ReferralRepository
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.ThreadUtils

class ShareReferralUseCaseTest {

    private val mockReferralRepository: ReferralRepository = mock()

    @Suppress("MoveLambdaOutsideParentheses")
    private val shareReferralUseCase = ShareReferralUseCase(
        { mockReferralRepository },
    )

    @Before
    fun setup() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `when call shouldShowShareNudge then returns output from repository`() {
        runBlocking {
            val fakeShouldShowNudge = false
            whenever(mockReferralRepository.shouldShowShareNudge()).thenReturn(fakeShouldShowNudge)

            val result = shareReferralUseCase.shouldShowShareNudge().test().awaitCount(1)

            result.assertValue(fakeShouldShowNudge)
        }
    }

    @Test
    fun `when call setShareNudge then calls setShareNudge from repository`() {
        runBlocking {
            val fakeShouldShowNudge = false

            val result = shareReferralUseCase.setShareNudge(fakeShouldShowNudge).test().awaitCount(1)

            result.assertComplete()
            val captor = argumentCaptor<Boolean>()
            verify(mockReferralRepository).setShareNudge(captor.capture())
            Assert.assertEquals(fakeShouldShowNudge, captor.firstValue)
        }
    }
}
