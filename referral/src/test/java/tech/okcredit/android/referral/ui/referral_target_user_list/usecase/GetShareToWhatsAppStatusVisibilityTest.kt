package tech.okcredit.android.referral.ui.referral_target_user_list.usecase

import `in`.okcredit.referral.contract.utils.ReferralVersion
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import tech.okcredit.android.referral.utils.GetReferralVersionImpl

class GetShareToWhatsAppStatusVisibilityTest {
    private val getReferralVersionImpl: GetReferralVersionImpl = mock()
    private val GetShareToWhatsAppStatusVisibility =
        GetShareToWhatsAppStatusVisibility(Lazy { getReferralVersionImpl })

    @Test
    fun `Usecase Should Return true when ReferralVersion is TARGETED_REFERRAL_WITH_SHARE_OPTION`() {
        // Given
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION))

        // when
        val testObserver =
            GetShareToWhatsAppStatusVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(true)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase Should Return false when ReferralVersion is No_Rewards`() {
        // Given
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.NO_REWARD))

        // when
        val testObserver =
            GetShareToWhatsAppStatusVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase Should Return false when ReferralVersion is TARGETED_REFERRAL`() {
        // Given
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.TARGETED_REFERRAL))

        // when
        val testObserver =
            GetShareToWhatsAppStatusVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase Should Return false when ReferralVersion is UNKNOWN`() {
        // Given
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.UNKNOWN))

        // when
        val testObserver =
            GetShareToWhatsAppStatusVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase Should Return false when ReferralVersion is REWARDS_ON_ACTIVATION`() {
        // Given
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.REWARDS_ON_ACTIVATION))

        // when
        val testObserver =
            GetShareToWhatsAppStatusVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }
}
