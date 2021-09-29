package `in`.okcredit.merchant.rewards

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.rewards.helpers.RewardsClaimHelperImpl
import `in`.okcredit.merchant.rewards.server.internal.ApiMessages.ClaimRewardResponse
import `in`.okcredit.merchant.rewards.temp.SyncableRewardsRepository
import `in`.okcredit.rewards.contract.RewardStatus.*
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.language.LocaleManager
import java.lang.IllegalStateException

class RewardsClaimHelperImplTest {

    private val mockRewardsRepository: SyncableRewardsRepository = mock()
    private val mockLocaleManager: LocaleManager = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val impl = RewardsClaimHelperImpl(
        { mockRewardsRepository },
        { mockLocaleManager },
        { getActiveBusinessId }
    )

    @Before
    fun setup() {
        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit
    }

    @Test
    fun `when reward is unclaimed then returns Unclaimed Status`() {
        val status = "unclaimed/fake"
        val fakeCustomMessage = "fake custom message"
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(mockRewardsRepository.claimReward(fakeRewardId, fakeLocale, businessId))
            .thenReturn(Single.just(ClaimRewardResponse(status, fakeCustomMessage)))
        whenever(mockLocaleManager.getLanguage())
            .thenReturn(fakeLocale)

        val result = impl.claim(fakeRewardId).test().values().first()

        Truth.assertThat(result).isInstanceOf(UNCLAIMED::class.java)
        result as UNCLAIMED

        Truth.assertThat(result.status).isEqualTo(status)
        Truth.assertThat(result.customMessage).isEqualTo(fakeCustomMessage)
    }

    @Test
    fun `when reward is claimed then returns Claimed Status`() {
        val status = "claimed/fake"
        val fakeCustomMessage = "fake custom message"
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(mockRewardsRepository.claimReward(fakeRewardId, fakeLocale, businessId))
            .thenReturn(Single.just(ClaimRewardResponse(status, fakeCustomMessage)))
        whenever(mockLocaleManager.getLanguage())
            .thenReturn(fakeLocale)

        val result = impl.claim(fakeRewardId).test().values().first()

        Truth.assertThat(result).isInstanceOf(CLAIMED::class.java)
        result as CLAIMED

        Truth.assertThat(result.status).isEqualTo(status)
        Truth.assertThat(result.customMessage).isEqualTo(fakeCustomMessage)
    }

    @Test
    fun `when reward is failed then returns Failed Status`() {
        val status = "failed/fake"
        val fakeCustomMessage = "fake custom message"
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(mockRewardsRepository.claimReward(fakeRewardId, fakeLocale, businessId))
            .thenReturn(Single.just(ClaimRewardResponse(status, fakeCustomMessage)))
        whenever(mockLocaleManager.getLanguage())
            .thenReturn(fakeLocale)

        val result = impl.claim(fakeRewardId).test().values().first()

        Truth.assertThat(result).isInstanceOf(FAILED::class.java)
        result as FAILED

        Truth.assertThat(result.status).isEqualTo(status)
        Truth.assertThat(result.customMessage).isEqualTo(fakeCustomMessage)
    }

    @Test
    fun `when reward is on_hold then returns OnHold Status`() {
        val status = "on_hold/fake"
        val fakeCustomMessage = "fake custom message"
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(mockRewardsRepository.claimReward(fakeRewardId, fakeLocale, businessId))
            .thenReturn(Single.just(ClaimRewardResponse(status, fakeCustomMessage)))
        whenever(mockLocaleManager.getLanguage())
            .thenReturn(fakeLocale)

        val result = impl.claim(fakeRewardId).test().values().first()

        Truth.assertThat(result).isInstanceOf(ON_HOLD::class.java)
        result as ON_HOLD

        Truth.assertThat(result.status).isEqualTo(status)
        Truth.assertThat(result.customMessage).isEqualTo(fakeCustomMessage)
    }

    @Test
    fun `when reward is processing then returns Processing Status`() {
        val status = "processing/fake"
        val fakeCustomMessage = "fake custom message"
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(mockRewardsRepository.claimReward(fakeRewardId, fakeLocale, businessId))
            .thenReturn(Single.just(ClaimRewardResponse(status, fakeCustomMessage)))
        whenever(mockLocaleManager.getLanguage())
            .thenReturn(fakeLocale)

        val result = impl.claim(fakeRewardId).test().values().first()

        Truth.assertThat(result).isInstanceOf(PROCESSING::class.java)
        result as PROCESSING

        Truth.assertThat(result.status).isEqualTo(status)
        Truth.assertThat(result.customMessage).isEqualTo(fakeCustomMessage)
    }

    @Test
    fun `when reward is unknow then throws exception`() {
        val status = "unknown/fake"
        val fakeCustomMessage = "fake custom message"
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(mockRewardsRepository.claimReward(fakeRewardId, fakeLocale, businessId))
            .thenReturn(Single.just(ClaimRewardResponse(status, fakeCustomMessage)))
        whenever(mockLocaleManager.getLanguage())
            .thenReturn(fakeLocale)

        val result = impl.claim(fakeRewardId).test()

        result.assertError(IllegalStateException::class.java)
        io.mockk.verify { RecordException.recordException(any()) }
    }

    @Test
    fun `verify on_hold statuses`() {
        Truth.assertThat(
            impl.isBankDetailsDuplication(ON_HOLD("on_hold/bank/user_details/duplicate", ""))
        ).isTrue()
        Truth.assertThat(
            impl.isUpiInactive(ON_HOLD("on_hold/bank/user_details/inactive", ""))
        ).isTrue()
        Truth.assertThat(
            impl.isDailyPayoutLimitReached(ON_HOLD("on_hold/bank/payout/daily_limit_reached", ""))
        ).isTrue()
    }

    @Test
    fun `verify processing statuses`() {
        Truth.assertThat(
            impl.isPayoutDelayed(PROCESSING("processing/bank/payout/delayed", ""))
        ).isTrue()
        Truth.assertThat(
            impl.isPayoutStarted(PROCESSING("processing/bank/payout/started", ""))
        ).isTrue()
        Truth.assertThat(
            impl.isBudgetExhausted(PROCESSING("processing/okcredit/budget/exhausted", ""))
        ).isTrue()
    }

    @Test
    fun `verify retryable flag`() {
        Truth.assertThat(
            impl.isRetryable(FAILED("failed/fake/retryable", ""))
        ).isTrue()
        Truth.assertThat(
            impl.isRetryable(FAILED("failed/fake/retryable/custom", ""))
        ).isTrue()
    }

    @Test
    fun `verify custom flag`() {
        Truth.assertThat(
            impl.isCustom(UNCLAIMED("unclaimed/fake/custom", ""))
        ).isTrue()
        Truth.assertThat(
            impl.isCustom(CLAIMED("claimed/fake/custom", ""))
        ).isTrue()
        Truth.assertThat(
            impl.isCustom(ON_HOLD("on_hold/fake/custom", ""))
        ).isTrue()
        Truth.assertThat(
            impl.isCustom(PROCESSING("processing/fake/custom", ""))
        ).isTrue()
        Truth.assertThat(
            impl.isCustom(FAILED("failed/fake/custom", ""))
        ).isTrue()

        Truth.assertThat(
            impl.isCustom(FAILED("failed/fake/retryable/custom", ""))
        ).isTrue()
        Truth.assertThat(
            impl.isCustom(FAILED("failed/fake/custom/retryable", ""))
        ).isFalse()
    }

    companion object {
        private const val fakeRewardId = "fake_id"
        private const val fakeLocale = "fake_locale"
    }
}
