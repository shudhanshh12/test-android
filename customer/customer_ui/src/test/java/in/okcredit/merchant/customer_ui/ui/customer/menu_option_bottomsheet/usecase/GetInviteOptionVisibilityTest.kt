package `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.usecase

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.referral.contract.models.TargetedUser
import `in`.okcredit.referral.contract.utils.ReferralVersion
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import org.junit.Test
import tech.okcredit.android.referral.ui.referral_target_user_list.usecase.GetTargetUsers
import tech.okcredit.android.referral.utils.GetReferralVersionImpl

class GetInviteOptionVisibilityTest {
    private val getTargetedUsers: GetTargetUsers = mock()
    private val getReferralVersionImpl: GetReferralVersionImpl = mock()
    private val targetedUser: TargetedUser = mock()

    private val getInviteOptionVisibility =
        GetInviteOptionVisibility({ getTargetedUsers }, { getReferralVersionImpl })

    @Test
    fun `Usecase should return true when ReferralVersion is Targeted User and TargetedUserFound`() {
        // Given
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.TARGETED_REFERRAL))
        whenever(getTargetedUsers.execute()).thenReturn(Single.just(listOf(customerFromTargetedUser, targetedUser)))

        // When
        val testObserver =
            getInviteOptionVisibility.execute(CUSTOMER).subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(true)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase should return true when ReferralVersion With Share Option is Targeted User and TargetedUserFound`() {
        // Given
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION))
        whenever(getTargetedUsers.execute()).thenReturn(Single.just(listOf(customerFromTargetedUser, targetedUser)))

        // When
        val testObserver =
            getInviteOptionVisibility.execute(CUSTOMER).subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(true)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase should return false when ReferralVersion With NO_REWARD is Targeted User and TargetedUserFound`() {
        // Given
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.NO_REWARD))
        whenever(getTargetedUsers.execute()).thenReturn(Single.just(listOf(customerFromTargetedUser, targetedUser)))

        // When
        val testObserver =
            getInviteOptionVisibility.execute(CUSTOMER).subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase should return false when ReferralVersion is REWARDS_ON_ACTIVATION is Targeted User and TargetedUserFound`() {
        // Given
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.REWARDS_ON_ACTIVATION))
        whenever(getTargetedUsers.execute()).thenReturn(Single.just(listOf(customerFromTargetedUser, targetedUser)))

        // When
        val testObserver =
            getInviteOptionVisibility.execute(CUSTOMER).subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase should return false when ReferralVersion is UnKnown is Targeted User and TargetedUserFound`() {
        // Given
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.UNKNOWN))
        whenever(getTargetedUsers.execute()).thenReturn(Single.just(listOf(customerFromTargetedUser, targetedUser)))

        // When
        val testObserver =
            getInviteOptionVisibility.execute(CUSTOMER).subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase should return false when ReferralVersion  is Targeted User and TargetedUser not Found`() {
        // Given
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.TARGETED_REFERRAL))
        whenever(getTargetedUsers.execute()).thenReturn(Single.just(listOf(targetedUser, targetedUser)))

        // When
        val testObserver =
            getInviteOptionVisibility.execute(CUSTOMER).subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase should return false when ReferralVersion With share option is Targeted User and TargetedUser not Found`() {
        // Given
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION))
        whenever(getTargetedUsers.execute()).thenReturn(Single.just(listOf(targetedUser, targetedUser)))

        // When
        val testObserver =
            getInviteOptionVisibility.execute(CUSTOMER).subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase should return false when NO Referral is Targeted User and TargetedUser not Found`() {
        // Given
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.NO_REWARD))
        whenever(getTargetedUsers.execute()).thenReturn(Single.just(listOf(targetedUser, targetedUser)))

        // When
        val testObserver =
            getInviteOptionVisibility.execute(CUSTOMER).subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase should return false when V3 without list is Targeted User and TargetedUser not Found`() {
        // Given
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.REWARDS_ON_ACTIVATION))
        whenever(getTargetedUsers.execute()).thenReturn(Single.just(listOf(targetedUser, targetedUser)))

        // When
        val testObserver =
            getInviteOptionVisibility.execute(CUSTOMER).subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase should return false when ReferralVersion  list is Targeted User and TargetedUser not Found`() {
        // Given
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.UNKNOWN))
        whenever(getTargetedUsers.execute()).thenReturn(Single.just(listOf(targetedUser, targetedUser)))

        // When
        val testObserver =
            getInviteOptionVisibility.execute(CUSTOMER).subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    val CUSTOMER = Customer(
        "1234",
        0,
        Transaction.CREDIT,
        "9999999999",
        "John Lennon",
        DateTime(2018, 10, 2, 0, 0, 0),
        100L,
        2,
        0,
        DateTime(2018, 10, 2, 0, 0, 0),
        DateTime(2018, 10, 2, 0, 0, 0),
        "http://okcredit.in",
        null,
        null,
        null,
        0L,
        DateTime(2018, 10, 2, 0, 0, 0),
        true,
        DateTime(2018, 10, 2, 0, 0, 0),
        false,
        "en",
        "sms",
        false,
        false,
        null,
        false,
        false, false, DateTime(2018, 10, 2, 0, 0, 0), false, 0, 0,
        lastReminderSendTime = DateTime(0)
    )

    val customerFromTargetedUser = TargetedUser(
        "1234",
        "John Lennon",
        "9999999999",
        "",
        "",
        false,
        5000L
    )
}
