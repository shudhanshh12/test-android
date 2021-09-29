package `in`.okcredit.cashback

import `in`.okcredit.cashback.repository.CashbackRepository
import `in`.okcredit.cashback.usecase.GetCashbackRewardForPaymentImpl
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.rewards.server.internal.toRewardModel
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test

class GetCashbackRewardForPaymentTest {
    private val cashbackRepository: CashbackRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getCashbackRewardForPayment =
        GetCashbackRewardForPaymentImpl({ cashbackRepository }, { getActiveBusinessId })

    @Test
    fun `test execute`() {
        val rewardModel = TestData.rewardFromApi.toRewardModel()
        val dummyPaymentId = "DUMMY_PAYMENT_ID"
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(cashbackRepository.getCashbackRewardForPaymentId(dummyPaymentId, businessId))
            .thenReturn(Observable.just(rewardModel))

        getCashbackRewardForPayment.execute(dummyPaymentId).test().apply {
            assertValue(rewardModel)
            assertComplete()
        }
    }
}
