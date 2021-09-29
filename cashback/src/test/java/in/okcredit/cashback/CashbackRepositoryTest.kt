package `in`.okcredit.cashback

import `in`.okcredit.cashback.datasource.local.CashbackLocalCacheSourceImpl
import `in`.okcredit.cashback.datasource.remote.CashbackRemoteSourceImpl
import `in`.okcredit.cashback.datasource.remote.apiClient.CashbackEntityMapper
import `in`.okcredit.cashback.datasource.remoteConfig.CashbackRemoteConfigSourceImpl
import `in`.okcredit.cashback.repository.CashbackRepositoryImpl
import `in`.okcredit.merchant.rewards.server.internal.toRewardModel
import `in`.okcredit.shared.utils.CommonUtils
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.ThreadUtils

class CashbackRepositoryTest {
    private val schedulerProvider: SchedulerProvider = mock()

    private val cashbackRemoteSource: CashbackRemoteSourceImpl = mock()
    private val cashbackLocalCacheSource: CashbackLocalCacheSourceImpl = mock()
    private val cashbackRemoteConfigSource: CashbackRemoteConfigSourceImpl = mock()

    private lateinit var cashbackRepository: CashbackRepositoryImpl
    private val businessId = "businessId"

    @Before
    fun setUp() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        mockkObject(CommonUtils)
        whenever(schedulerProvider.io()).thenReturn(Schedulers.trampoline())
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()

        cashbackRepository = CashbackRepositoryImpl(
            { cashbackRemoteSource },
            { cashbackLocalCacheSource },
            { cashbackRemoteConfigSource }
        )
    }

    @Test
    fun `test when cashback_message_details are not present in cache`() {
        val cashbackMessageDetailsDto = TestData.cashbackMessageDetailsDto
        whenever(cashbackLocalCacheSource.getCachedCashbackMessageDetailsTimestamp()).thenReturn(Observable.just(-1))
        whenever(cashbackLocalCacheSource.setCashbackMessageDetailsCache(cashbackMessageDetailsDto)).thenReturn(
            Completable.complete()
        )
        whenever(cashbackRemoteConfigSource.getTtlForLocalCache()).thenReturn(1L)
        whenever(cashbackRemoteSource.getCashbackMessageDetails(businessId)).thenReturn(Single.just(cashbackMessageDetailsDto))
        every { CommonUtils.currentDateTime().millis } returns (36000000L)

        cashbackRepository.getCashbackMessageDetails(businessId).test().apply {
            assertValue(CashbackEntityMapper.CASHBACK_MESSAGE_DETAILS_CONVERTER.convert(cashbackMessageDetailsDto))
            dispose()
        }
    }

    @Test
    fun `test when cashback_message_details are present in cache and are valid`() {
        val cashbackMessageDetailsDto = TestData.cashbackMessageDetailsDto
        whenever(cashbackLocalCacheSource.getCachedCashbackMessageDetailsTimestamp()).thenReturn(Observable.just(100))
        whenever(cashbackRemoteConfigSource.getTtlForLocalCache()).thenReturn(10000L)
        whenever(cashbackLocalCacheSource.getCachedCashbackMessageDetails()).thenReturn(
            Single.just(
                cashbackMessageDetailsDto
            )
        )
        every { CommonUtils.currentDateTime().millis } returns 1000

        cashbackRepository.getCashbackMessageDetails(businessId).test().apply {
            assertValue(CashbackEntityMapper.CASHBACK_MESSAGE_DETAILS_CONVERTER.convert(cashbackMessageDetailsDto))
            dispose()
        }
    }

    @Test
    fun `test when cashback_message_details are present in cache and are invalid`() {
        val cashbackMessageDetailsDto = TestData.cashbackMessageDetailsDto
        whenever(cashbackLocalCacheSource.getCachedCashbackMessageDetailsTimestamp()).thenReturn(Observable.just(100))
        whenever(cashbackLocalCacheSource.setCashbackMessageDetailsCache(cashbackMessageDetailsDto)).thenReturn(
            Completable.complete()
        )
        whenever(cashbackRemoteConfigSource.getTtlForLocalCache()).thenReturn(1)
        whenever(cashbackRemoteSource.getCashbackMessageDetails(businessId)).thenReturn(Single.just(cashbackMessageDetailsDto))
        every { CommonUtils.currentDateTime().millis } returns 36000000L

        cashbackRepository.getCashbackMessageDetails(businessId).test().apply {
            assertValue(CashbackEntityMapper.CASHBACK_MESSAGE_DETAILS_CONVERTER.convert(cashbackMessageDetailsDto))
            dispose()
        }
    }

    @Test
    fun `test whether cashback_message_details cache are cleared and then refetched`() {
        val cashbackMessageDetailsDto = TestData.cashbackMessageDetailsDto
        val cashbackMessageDetailsDto2 = cashbackMessageDetailsDto.copy(
            cashbackAmount = 999
        )
        whenever(cashbackLocalCacheSource.getCachedCashbackMessageDetailsTimestamp()).thenReturn(
            Observable.just(100, -1)
        )
        whenever(cashbackLocalCacheSource.getCachedCashbackMessageDetails()).thenReturn(
            Single.just(
                cashbackMessageDetailsDto
            )
        )
        `when`(cashbackRemoteConfigSource.getTtlForLocalCache()).thenReturn(1L)
        whenever(cashbackRemoteSource.getCashbackMessageDetails(businessId)).thenReturn(Single.just(cashbackMessageDetailsDto2))
        whenever(cashbackLocalCacheSource.setCashbackMessageDetailsCache(cashbackMessageDetailsDto2)).thenReturn(
            Completable.complete()
        )
        every { CommonUtils.currentDateTime().millis } returns 3600000L

        cashbackRepository.getCashbackMessageDetails(businessId).test().apply {
            assertEquals(
                values()[0],
                CashbackEntityMapper.CASHBACK_MESSAGE_DETAILS_CONVERTER.convert(cashbackMessageDetailsDto)
            )

            assertEquals(
                values()[1],
                CashbackEntityMapper.CASHBACK_MESSAGE_DETAILS_CONVERTER.convert(cashbackMessageDetailsDto2)
            )

            assertComplete()
        }
    }

    @Test
    fun `test when remote source flowable emits RewardFromApi`() {
        val rewardFromApi = TestData.rewardFromApi
        val dummyPaymentId = "DUMMY_PAYMENT_ID"
        val retryInterval = 500L
        val timeLimit = 5000L

        whenever(cashbackRemoteConfigSource.getCashbackRewardRequestRetryInterval()).thenReturn(retryInterval)
        whenever(cashbackRemoteConfigSource.getCashbackRewardRequestTimeLimit()).thenReturn(timeLimit)
        whenever(
            cashbackRemoteSource.getCashbackRewardForPaymentId(
                dummyPaymentId,
                cashbackRemoteConfigSource.getCashbackRewardRequestRetryInterval(),
                cashbackRemoteConfigSource.getCashbackRewardRequestTimeLimit(),
                businessId
            )
        ).thenReturn(Flowable.just(rewardFromApi))

        cashbackRepository.getCashbackRewardForPaymentId(dummyPaymentId, businessId).test().apply {
            assertValue(rewardFromApi.toRewardModel())
        }
    }
}
