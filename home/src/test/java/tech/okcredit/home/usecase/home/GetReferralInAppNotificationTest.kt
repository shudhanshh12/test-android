package tech.okcredit.home.usecase.home

import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.referral.contract.usecase.GetReferralVersion
import com.nhaarman.mockitokotlin2.mock
import dagger.Lazy
import org.junit.Rule
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.VerificationCollector

class GetReferralInAppNotificationTest {

    @get:Rule
    var verificationCollector: VerificationCollector = MockitoJUnit.collector()

    private val getReferralVersion: GetReferralVersion = mock()
    private val getReferralVersionLazy: Lazy<GetReferralVersion> = Lazy { getReferralVersion }
    private val referralRepository: ReferralRepository = mock()
    private val referralRepositoryLazy: Lazy<ReferralRepository> = Lazy { referralRepository }
    private val getReferralInAppNotification =
        GetReferralInAppNotification(
            getReferralVersionLazy, referralRepositoryLazy
        )

//    @Test
//    fun `should return TRUE  when referral ab V1 enabled & in-app NOT displayed`() {
//        whenever(referralApiLazy.get().isReferralInAppDisplayed())
//            .thenReturn(false)
//
//        whenever(getReferralVersionLazy.get().execute())
//            .thenReturn(Observable.just(ReferralVersion.REWARDS_ON_ACTIVATION))
//
//        val testObserver = getReferralInAppNotification.execute(Unit).test()
//        testObserver.assertValues(
//            `in`.okcredit.shared.usecase.Result.Progress(),
//            `in`.okcredit.shared.usecase.Result.Success(true)
//        )
//
//        verify(getReferralVersionLazy.get()).execute()
//        verify(referralApiLazy.get()).isReferralInAppDisplayed()
//
//        testObserver.dispose()
//    }
//
//    @Test
//    fun `should return TRUE  when referral AB V2 enabled & in-app NOT displayed`() {
//        whenever(referralApiLazy.get().isReferralInAppDisplayed())
//            .thenReturn(false)
//
//        whenever(getReferralVersionLazy.get().execute())
//            .thenReturn(Observable.just(ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION))
//
//        val testObserver = getReferralInAppNotification.execute(Unit).test()
//        testObserver.assertValues(
//            `in`.okcredit.shared.usecase.Result.Progress(),
//            `in`.okcredit.shared.usecase.Result.Success(true)
//        )
//
//        verify(getReferralVersionLazy.get()).execute()
//        verify(referralApiLazy.get()).isReferralInAppDisplayed()
//
//        testObserver.dispose()
//    }
//
//    @Test
//    fun `should return FALSE when referral AB V0 enabled & in-app NOT displayed`() {
//        whenever(referralApiLazy.get().isReferralInAppDisplayed())
//            .thenReturn(false)
//
//        whenever(getReferralVersionLazy.get().execute())
//            .thenReturn(Observable.just(ReferralVersion.NO_REWARD))
//
//        val testObserver = getReferralInAppNotification.execute(Unit).test()
//        testObserver.assertValues(
//            `in`.okcredit.shared.usecase.Result.Progress(),
//            `in`.okcredit.shared.usecase.Result.Success(false)
//        )
//
//        verify(getReferralVersionLazy.get()).execute()
//        verify(referralApiLazy.get()).isReferralInAppDisplayed()
//
//        testObserver.dispose()
//    }
//
//    @Test
//    fun `should return FALSE  when referral ab V1 enabled & in-app displayed`() {
//
//        whenever(getReferralVersionLazy.get().execute())
//            .thenReturn(Observable.just(ReferralVersion.REWARDS_ON_ACTIVATION))
//
//        whenever(referralApiLazy.get().isReferralInAppDisplayed())
//            .thenReturn(true)
//
//        val testObserver = getReferralInAppNotification.execute(Unit).test()
//        testObserver.assertValues(
//            `in`.okcredit.shared.usecase.Result.Progress(),
//            `in`.okcredit.shared.usecase.Result.Success(false)
//        )
//
//        verify(getReferralVersionLazy.get(), times(0)).execute()
//        verify(referralApiLazy.get()).isReferralInAppDisplayed()
//
//        testObserver.dispose()
//    }
//
//    @Test
//    fun `should return FALSE  when referral AB V2 enabled & in-app displayed`() {
//        whenever(referralApiLazy.get().isReferralInAppDisplayed())
//            .thenReturn(true)
//
//        whenever(getReferralVersionLazy.get().execute())
//            .thenReturn(Observable.just(ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION))
//
//        val testObserver = getReferralInAppNotification.execute(Unit).test()
//        testObserver.assertValues(
//            `in`.okcredit.shared.usecase.Result.Progress(),
//            `in`.okcredit.shared.usecase.Result.Success(false)
//        )
//
//        verify(getReferralVersionLazy.get(), times(0)).execute()
//        verify(referralApiLazy.get()).isReferralInAppDisplayed()
//
//        testObserver.dispose()
//    }
//
//    @Test
//    fun `should return FALSE when referral AB V0 enabled & in-app displayed`() {
//
//        whenever(getReferralVersionLazy.get().execute())
//            .thenReturn(Observable.just(ReferralVersion.NO_REWARD))
//
//        whenever(referralApiLazy.get().isReferralInAppDisplayed())
//            .thenReturn(true)
//
//        val testObserver = getReferralInAppNotification.execute(Unit).test()
//        testObserver.assertValues(
//            `in`.okcredit.shared.usecase.Result.Progress(),
//            `in`.okcredit.shared.usecase.Result.Success(false)
//        )
//
//        verify(getReferralVersionLazy.get(), times(0)).execute()
//        verify(referralApiLazy.get()).isReferralInAppDisplayed()
//
//        testObserver.dispose()
//    }
}
