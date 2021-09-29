package tech.okcredit.android.referral.ui.referral_target_user_list.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.referral.contract.models.TargetedUser
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import tech.okcredit.android.base.coroutines.DispatcherProvider

@ExperimentalCoroutinesApi
class GetTargetUsersTest {

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    private val mockReferralRepository: ReferralRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val mockDispatcherProvider = mock<DispatcherProvider>()
        .apply { whenever(io()).thenReturn(testCoroutineDispatcher) }

    private val getTargetUsers = GetTargetUsers(
        { mockReferralRepository },
        { mockDispatcherProvider },
        { getActiveBusinessId }
    )

    @Test
    fun `when getTargetedUsers returns unregistered and registered users`() = runBlockingTest {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(mockReferralRepository.getTargetedUsers(businessId))
            .thenReturn(listOf(fakeRegisteredTargetedUser, fakeUnregisteredTargetedUser))

        val result = getTargetUsers.execute().test()

        result.assertValue(listOf(fakeRegisteredTargetedUser, fakeUnregisteredTargetedUser))
    }

    companion object {
        private val fakeRegisteredTargetedUser = buildTargetedUser(desiredConversion = true)
        private val fakeUnregisteredTargetedUser = buildTargetedUser(desiredConversion = false)

        private fun buildTargetedUser(desiredConversion: Boolean) = mock<TargetedUser>()
            .apply { whenever(converted).thenReturn(desiredConversion) }
    }
}
