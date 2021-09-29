package tech.okcredit.android.referral.store

import `in`.okcredit.referral.contract.models.TargetedUser
import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.TestData
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils

class ReferralLocalSourceTest {
    private val referralPreferences: ReferralPreferences = mock()
    private val dispatcherProvider: DispatcherProvider = mock()
    private val context: Context = mock()
    private val localSource = ReferralLocalSource(
        { referralPreferences },
        { dispatcherProvider },
        { context }
    )

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        whenever(dispatcherProvider.io()).thenReturn(Dispatchers.Unconfined)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
        every { ThreadUtils.database() } returns Schedulers.trampoline()
    }

    @Test
    fun `should save convert and save json format of the generic share content in preference when setGenericShareContent() is called`() {
        runBlocking {
            localSource.setGenericShareContent(TestData.SHARE_CONTENT_RESPONSE)

            verify(referralPreferences).set("share_content", Gson().toJson(TestData.GENERIC_CONTENT), Scope.Individual)
        }
    }

    @Test
    fun `should save convert and save json format of the list in preference when setTargetedUsers() is called`() {
        runBlocking {
            localSource.setTargetedUsers(TestData.TARGETED_USERS)

            verify(referralPreferences).set("targeted_users", Gson().toJson(TestData.TARGETED_USERS), Scope.Individual)
        }
    }

    @Test
    fun `should return true when pref returns true`() {
        whenever(referralPreferences.getBoolean(eq("referral_inapp_displayed"), any(), anyOrNull()))
            .thenReturn(flowOf(true))

        localSource.isReferralInAppDisplayed().test().assertValue(true)
    }

    @Test
    fun `should return false when pref returns false`() {
        whenever(referralPreferences.getBoolean(eq("referral_inapp_displayed"), any(), anyOrNull()))
            .thenReturn(flowOf(false))

        localSource.isReferralInAppDisplayed().test().assertValue(false)
    }

    @Test
    fun `getTargetedUsers() should return empty list when preference are empty`() {
        runBlocking {
            whenever(referralPreferences.getString("targeted_users", Scope.Individual)).thenReturn(flowOf(""))

            val result = localSource.getTargetedUsers()

            assertThat(result).isEmpty()
        }
    }

    @Test
    fun `getTargetedUsers() should return list returned by the preferences`() {
        runBlocking {
            val list = listOf(
                TargetedUser(
                    "id",
                    "Sunil Gavaskar",
                    "5678909876",
                    "imageurl",
                    "source"
                )
            )
            whenever(
                referralPreferences.getString(
                    "targeted_users",
                    Scope.Individual
                )
            ).thenReturn(flowOf(Gson().toJson(list)))

            val result = localSource.getTargetedUsers()

            assertThat(result).isEqualTo(list)
        }
    }

    @Test
    fun `getReferralTarget() should return list returned by the preferences`() {
        runBlocking {
            whenever(referralPreferences.getString("referral_target", Scope.Individual)).thenReturn(
                flowOf(Gson().toJson(TestData.listOfReferralTargets))
            )

            val result = localSource.getReferralTarget()

            assertThat(result).isEqualTo(TestData.listOfReferralTargets)
        }
    }

    @Test
    fun `should save convert and save json format of the list in preference when setReferralTarget() is called`() {
        runBlocking {
            localSource.setReferralTarget(TestData.listOfReferralTargets)

            verify(referralPreferences).set(
                "referral_target",
                Gson().toJson(TestData.listOfReferralTargets),
                Scope.Individual
            )
        }
    }

    @Test
    fun `getTargetBannerClosedAt() should return close Time returned by the preferences`() {
        runBlocking {
            val timeValue = 2338389499L
            whenever(referralPreferences.getLong("target_banner_closed_at", Scope.Individual)).thenReturn(flowOf(timeValue))

            val result = localSource.getTargetBannerClosedAt()

            assertThat(result).isEqualTo(timeValue)
        }
    }

    @Test
    fun `setTargetBannerClosedAt() should return close Time returned by the preferences`() {
        runBlocking {
            val timeValue = 2338389499L
            localSource.setTargetBannerClosedAt(timeValue)

            verify(referralPreferences).set("target_banner_closed_at", timeValue, Scope.Individual)
        }
    }

    @Test
    fun `getTransactionInitiatedTime() should return transaction initiated time returned by the preferences`() {
        runBlocking {
            val timeValue = 2338389499L
            whenever(
                referralPreferences.getLong(
                    "transaction_initiated_time",
                    Scope.Individual
                )
            ).thenReturn(flowOf(timeValue))

            val result = localSource.getTransactionInitiatedTime()

            assertThat(result).isEqualTo(timeValue)
        }
    }

    @Test
    fun `setTransactionInitiatedTime() should return transaction initiated time by the preferences`() {
        runBlocking {
            val timeValue = 2338389499L
            localSource.setTransactionInitiatedTime(timeValue)

            verify(referralPreferences).set("transaction_initiated_time", timeValue, Scope.Individual)
        }
    }

    @Test
    fun testShouldShareNudgeIfReturnsBoolean() {
        runBlocking {
            val fakeValue = false

            whenever(referralPreferences.getBoolean("should_show_share_nudge", Scope.Individual, true))
                .thenReturn(flowOf(fakeValue))

            val result = localSource.shouldShowShareNudge()

            assertThat(result).isEqualTo(fakeValue)
        }
    }

    @Test
    fun testSetNudgeValueToSharedReferences() {
        runBlocking {
            val fakeValue = true

            localSource.setShareNudge(fakeValue)

            verify(referralPreferences).set("should_show_share_nudge", fakeValue, Scope.Individual)
        }
    }
}
