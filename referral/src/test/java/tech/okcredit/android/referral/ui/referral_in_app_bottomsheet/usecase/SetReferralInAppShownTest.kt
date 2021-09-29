package tech.okcredit.android.referral.ui.referral_in_app_bottomsheet.usecase

import `in`.okcredit.referral.contract.ReferralRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Test

class SetReferralInAppShownTest {

    private val mockReferralRepository: ReferralRepository = mock()

    private val setReferralInAppShown = SetReferralInAppShown { mockReferralRepository }

    @Before
    fun setup() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.Default } returns Dispatchers.Unconfined
    }

    @Test
    fun `should call the repository setReferralInAppPreference`() {
        whenever(mockReferralRepository.setReferralInAppPreference(any())).thenReturn(Completable.complete())

        val result = setReferralInAppShown.execute().test()

        result.assertComplete()
        verify(mockReferralRepository, times(1)).setReferralInAppPreference(true)
    }
}
