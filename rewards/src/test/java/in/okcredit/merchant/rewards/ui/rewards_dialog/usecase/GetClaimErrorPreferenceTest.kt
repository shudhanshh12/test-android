package `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase

import `in`.okcredit.rewards.contract.RewardsRepository
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Observable
import org.junit.Test

class GetClaimErrorPreferenceTest {
    private val rewardsRepository: RewardsRepository = mock()
    private val getClaimErrorPreference: GetClaimErrorPreference = GetClaimErrorPreference(
        Lazy { rewardsRepository }
    )

    @Test
    fun `getClaimErrorPreference should return true when repository return true`() {
        // given
        whenever(rewardsRepository.getClaimErrorPreference()).thenReturn(Observable.just(true))

        // when
        val result = getClaimErrorPreference.execute().test()

        // then
        result.assertValues(
            Result.Progress(),
            Result.Success(true)
        )
    }

    @Test
    fun `getClaimErrorPreference should return false when repository return false`() {
        // given
        whenever(rewardsRepository.getClaimErrorPreference()).thenReturn(Observable.just(false))

        // when
        val result = getClaimErrorPreference.execute().test()

        // then
        result.assertValues(
            Result.Progress(),
            Result.Success(false)
        )
    }
}
