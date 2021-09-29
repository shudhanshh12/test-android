package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.store.database.BulkReminderDbInfo
import com.google.common.truth.Truth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class GetHomeBannerForBulkReminderTest {
    private val mockCustomerRepo: CustomerRepo = mock()
    private val mockFirebaseRemoteConfig: FirebaseRemoteConfig = mock()
    private val mockAb: AbRepository = mock()
    private val mockGetActiveBusinessId: GetActiveBusinessId = mock()

    private val getHomeBannerForBulkReminder = GetBannerForBulkReminderImpl(
        { mockCustomerRepo },
        { mockFirebaseRemoteConfig },
        { mockAb },
        { mockGetActiveBusinessId }
    )

    companion object {
        private const val FEATURE_NAME = "bulk_reminder"
    }

    @Test
    fun `getHomeBannerForBulkReminder return canShowBanner is false when due balance is greater than 0`() {
        runBlocking {
            // given
            val fakeBulkReminderDbInfo = BulkReminderDbInfo(
                totalBalanceDue = 20,
                countNumberOfCustomers = 3,
                totalCustomers = 3
            )
            val fakeString = "4"
            val expectedDefaultedSinceForQuery = "-$fakeString day"
            val fakeBusinessId = "1243"
            val argumentCaptorForString = argumentCaptor<String>()

            // when
            whenever(mockGetActiveBusinessId.execute()).thenReturn(Single.just(fakeBusinessId))
            whenever(
                mockCustomerRepo.getDefaultersDataForBanner(
                    argumentCaptorForString.capture(),
                    argumentCaptorForString.capture()
                )
            ).thenReturn(
                flowOf(fakeBulkReminderDbInfo)
            )
            whenever(mockFirebaseRemoteConfig.getString(argumentCaptorForString.capture())).thenReturn(fakeString)
            whenever(mockAb.isFeatureEnabled(FEATURE_NAME)).thenReturn(Observable.just(true))

            val result = getHomeBannerForBulkReminder.execute().first()

            // then
            assert(!result.canShowBanner)

            Truth.assertThat(argumentCaptorForString.secondValue)
                .isEqualTo(expectedDefaultedSinceForQuery)

            Truth.assertThat(argumentCaptorForString.firstValue)
                .isEqualTo("bulk_reminder_v2_defaulted_since")
        }
    }

    @Test
    fun `getHomeBannerForBulkReminder return canShowBanner is true when due balance is less than 0`() {
        runBlocking {
            // given
            val fakeBulkReminderDbInfo = BulkReminderDbInfo(
                totalBalanceDue = -2000,
                countNumberOfCustomers = 3,
                totalCustomers = 3
            )
            val fakeString = "4"
            val expectedDefaultedSinceForQuery = "-$fakeString day"
            val argumentCaptorForString = argumentCaptor<String>()
            val fakeBusinessId = "1243"

            // when
            whenever(mockGetActiveBusinessId.execute()).thenReturn(Single.just(fakeBusinessId))
            whenever(
                mockCustomerRepo.getDefaultersDataForBanner(
                    argumentCaptorForString.capture(),
                    argumentCaptorForString.capture()
                )
            ).thenReturn(
                flowOf(fakeBulkReminderDbInfo)
            )
            whenever(mockFirebaseRemoteConfig.getString(argumentCaptorForString.capture())).thenReturn(fakeString)
            whenever(mockAb.isFeatureEnabled(FEATURE_NAME)).thenReturn(Observable.just(true))

            val result = getHomeBannerForBulkReminder.execute().first()

            // then
            assert(result.canShowBanner)

            Truth.assertThat(argumentCaptorForString.secondValue)
                .isEqualTo(expectedDefaultedSinceForQuery)

            Truth.assertThat(argumentCaptorForString.firstValue)
                .isEqualTo("bulk_reminder_v2_defaulted_since")
        }
    }

    @Test
    fun `getHomeBannerForBulkReminder return showNotificationBadge is false when number of customer is zero`() {
        runBlocking {
            // given
            val fakeBulkReminderDbInfo = BulkReminderDbInfo(
                totalBalanceDue = -300,
                countNumberOfCustomers = 0,
                totalCustomers = 3
            )
            val fakeString = "4"
            val expectedDefaultedSinceForQuery = "-$fakeString day"
            val argumentCaptorForString = argumentCaptor<String>()
            val fakeBusinessId = "1243"

            // when
            whenever(mockGetActiveBusinessId.execute()).thenReturn(Single.just(fakeBusinessId))
            whenever(
                mockCustomerRepo.getDefaultersDataForBanner(
                    argumentCaptorForString.capture(),
                    argumentCaptorForString.capture()
                )
            ).thenReturn(
                flowOf(fakeBulkReminderDbInfo)
            )
            whenever(mockFirebaseRemoteConfig.getString(argumentCaptorForString.capture())).thenReturn(fakeString)
            whenever(mockAb.isFeatureEnabled(FEATURE_NAME)).thenReturn(Observable.just(true))

            val result = getHomeBannerForBulkReminder.execute().first()

            // then
            assert(!result.canShowNotificationIcon)

            Truth.assertThat(argumentCaptorForString.secondValue)
                .isEqualTo(expectedDefaultedSinceForQuery)

            Truth.assertThat(argumentCaptorForString.firstValue)
                .isEqualTo("bulk_reminder_v2_defaulted_since")
        }
    }
}
