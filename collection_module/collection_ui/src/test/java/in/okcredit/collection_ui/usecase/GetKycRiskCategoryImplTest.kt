package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.KycExternalInfo
import `in`.okcredit.collection.contract.KycRisk
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class GetKycRiskCategoryImplTest {

    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val businessId = "businessId"

    private val getKycRiskCategoryImpl =
        GetKycRiskCategoryImpl({ collectionRepository }, { getActiveBusinessId })

    @Before
    fun setup() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
    }

    @Test
    fun `execute() should return limit reached with risk low if upi limit reached`() {
        val response = KycExternalInfo(
            merchantId = businessId,
            kyc = "NOT_SET",
            upiDailyLimit = 10,
            nonUpiDailyLimit = 10,
            upiDailyTransactionAmount = 10,
            nonUpiDailyTransactionAmount = 0,
            category = KycRiskCategory.LOW.value
        )
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(true))
        whenever(collectionRepository.getKycExternalInfo(businessId)).thenReturn(Observable.just(response))

        val testObserver = getKycRiskCategoryImpl.execute().test()

        testObserver.assertValues(
            KycRisk(
                KycRiskCategory.valueOf(response.category),
                true,
                CollectionMerchantProfile.DAILY
            )
        )

        testObserver.dispose()
    }

    @Test
    fun `execute() should return limit reached with risk low if non upi limit reached`() {
        val response = KycExternalInfo(
            merchantId = businessId,
            kyc = "NOT_SET",
            upiDailyLimit = 10,
            nonUpiDailyLimit = 10,
            upiDailyTransactionAmount = 0,
            nonUpiDailyTransactionAmount = 10,
            category = KycRiskCategory.LOW.value
        )
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(true))
        whenever(collectionRepository.getKycExternalInfo(businessId)).thenReturn(Observable.just(response))

        val testObserver = getKycRiskCategoryImpl.execute().test()

        testObserver.assertValues(
            KycRisk(
                KycRiskCategory.valueOf(response.category),
                true,
                CollectionMerchantProfile.DAILY
            )
        )

        testObserver.dispose()
    }

    @Test
    fun `execute() should return limit reached with risk high if upi limit reached`() {
        val response = KycExternalInfo(
            merchantId = businessId,
            kyc = "NOT_SET",
            upiDailyLimit = 10,
            nonUpiDailyLimit = 10,
            upiDailyTransactionAmount = 10,
            nonUpiDailyTransactionAmount = 0,
            category = KycRiskCategory.HIGH.value
        )
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(true))
        whenever(collectionRepository.getKycExternalInfo(businessId)).thenReturn(Observable.just(response))

        val testObserver = getKycRiskCategoryImpl.execute().test()

        testObserver.assertValues(
            KycRisk(
                KycRiskCategory.valueOf(response.category),
                true,
                CollectionMerchantProfile.DAILY
            )
        )

        testObserver.dispose()
    }
}
