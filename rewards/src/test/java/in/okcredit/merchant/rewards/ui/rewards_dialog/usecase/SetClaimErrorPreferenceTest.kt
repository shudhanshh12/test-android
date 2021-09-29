package `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase

import `in`.okcredit.rewards.contract.RewardsRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import dagger.Lazy
import org.junit.Test

class SetClaimErrorPreferenceTest {
    private val rewardRepository: RewardsRepository = mock()
    private val setClaimErrorPreference = SetClaimErrorPreference(Lazy { rewardRepository })

    @Test
    fun `verify usecase should call repository`() {
        setClaimErrorPreference.execute(true)

        verify(rewardRepository).setClaimErrorPreference(true)
    }
}
