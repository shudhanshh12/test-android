package `in`.okcredit.merchant.customer_ui.onboarding.usecase

import `in`.okcredit.backend.contract.GetTotalTxnCount
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test

class GetEligibilityOnboardingNudgeTest {
    private val getTotalTxnCount: GetTotalTxnCount = mock()
    private val getEligibilityOnboardingNudge = GetEligibilityOnboardingNudge { getTotalTxnCount }

    @Test
    fun `usecase should return false when transaction count more than one`() {
        whenever(getTotalTxnCount.execute()).thenReturn(Single.just(2))

        val result = getEligibilityOnboardingNudge.execute().test()

        result.assertValue(false)
    }

    @Test
    fun `usecase should return true when transaction count is zero`() {
        whenever(getTotalTxnCount.execute()).thenReturn(Single.just(0))

        val result = getEligibilityOnboardingNudge.execute().test()

        result.assertValue(true)
    }
}
