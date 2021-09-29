package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionStatus
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.collection.contract.PayoutType
import `in`.okcredit.collection.contract.SetOnlinePaymentStatusLocally
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.coJustRun
import io.mockk.mockk
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.junit.Test

class TriggerMerchantPayoutImplTest {
    private val collectionRepository: CollectionRepository = mock()
    private val collectionSyncer: CollectionSyncer = mockk()
    private val setOnlinePaymentStatusLocally: SetOnlinePaymentStatusLocally = mock()
    private val setOnlinePaymentStatusForRefundTxn: SetOnlinePaymentStatusForRefundTxn = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val triggerMerchantPayoutImpl = TriggerMerchantPayoutImpl(
        collectionRepository = { collectionRepository },
        collectionSyncer = { collectionSyncer },
        setOnlinePaymentStatusLocally = { setOnlinePaymentStatusLocally },
        setOnlinePaymentStatusForRefundTxn = { setOnlinePaymentStatusForRefundTxn },
        getActiveBusinessId = { getActiveBusinessId }
    )

    @Test
    fun `execute should complete`() {
        runBlocking {
            val businessId = "business-id"
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(
                collectionRepository.triggerMerchantPayout(
                    PayoutType.PAYOUT.value,
                    collectionType = "merchant_qr",
                    paymentId = "",
                    payoutId = "",
                    businessId = businessId
                )
            )
                .thenReturn(Completable.complete())
            coJustRun { (collectionSyncer.executeSyncOnlinePayments(businessId)) }
            whenever(
                setOnlinePaymentStatusLocally.execute(
                    CollectionStatus.PAYOUT_FAILED,
                    CollectionStatus.PAYOUT_INITIATED
                )
            ).thenReturn(Completable.complete())

            val testObserver =
                triggerMerchantPayoutImpl.executePayout(
                    PayoutType.PAYOUT.value,
                    collectionType = "merchant_qr",
                    paymentId = "",
                    payoutId = ""
                ).test()

            verify(collectionRepository).triggerMerchantPayout(
                PayoutType.PAYOUT.value,
                collectionType = "merchant_qr",
                paymentId = "",
                payoutId = "",
                businessId = businessId
            )
            verify(setOnlinePaymentStatusLocally).execute(
                CollectionStatus.PAYOUT_FAILED,
                CollectionStatus.PAYOUT_INITIATED
            )
            testObserver.assertComplete()
            testObserver.dispose()
        }
    }

    @Test
    fun `execute returns error`() {
        val mockError: Exception = mock()
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        coJustRun { (collectionSyncer.executeSyncOnlinePayments(businessId)) }
        whenever(
            setOnlinePaymentStatusLocally.execute(
                CollectionStatus.PAYOUT_FAILED,
                CollectionStatus.PAYOUT_INITIATED
            )
        ).thenReturn(Completable.complete())

        whenever(
            collectionRepository.triggerMerchantPayout(
                PayoutType.PAYOUT.value,
                collectionType = "merchant_qr",
                paymentId = "",
                payoutId = "",
                businessId = businessId
            )
        )
            .thenReturn(Completable.error(mockError))

        val testObserver =
            triggerMerchantPayoutImpl.executePayout(
                PayoutType.PAYOUT.value,
                collectionType = "merchant_qr",
                paymentId = "",
                payoutId = ""
            ).test()

        verify(collectionRepository).triggerMerchantPayout(
            PayoutType.PAYOUT.value,
            collectionType = "merchant_qr",
            paymentId = "",
            payoutId = "",
            businessId = businessId
        )
        testObserver.assertError(mockError)
        testObserver.dispose()
    }
}
