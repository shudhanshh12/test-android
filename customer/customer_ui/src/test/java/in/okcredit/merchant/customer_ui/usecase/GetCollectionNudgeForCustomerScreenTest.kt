package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend.contract.GetTotalTxnCount
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.customer.contract.CustomerRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class GetCollectionNudgeForCustomerScreenTest {

    private val ab: AbRepository = mock()
    private val getTotalTxnCount: GetTotalTxnCount = mock()
    private val customerRepository: CustomerRepository = mock()
    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val businessId = "businessId"

    private val getCollectionNudgeForCustomerScreen: GetCollectionNudgeForCustomerScreen =
        GetCollectionNudgeForCustomerScreen(
            { ab },
            { getTotalTxnCount },
            { customerRepository },
            { collectionRepository },
            { getActiveBusinessId },
        )

    companion object {
        private const val EXPT = "postlogin_android-all-collection_adoption_customer_page_widget"
        private const val PAYMENT_SIDE_VARIANT = "payment_side"
        private const val CREDIT_SIDE_VARIANT = "credit_side"
        private const val COLLECTION_FEATURE = "collection"
    }

    @Before
    fun setup() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
    }

    @Test
    fun `execute should return show on payment side`() {
        val totalAddTxnCountOnTrigger = 5
        val totalAddTxnCount = 9
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(false))
        whenever(getTotalTxnCount.execute()).thenReturn(Single.just(totalAddTxnCount))
        whenever(customerRepository.getTxnCntForCollectionNudgeOnCustomerScr(businessId)).thenReturn(
            totalAddTxnCountOnTrigger
        )
        whenever(ab.isFeatureEnabled(COLLECTION_FEATURE)).thenReturn(Observable.just(true))
        whenever(ab.isExperimentEnabled(EXPT)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(EXPT)).thenReturn(Observable.just(PAYMENT_SIDE_VARIANT))

        val testObserver = getCollectionNudgeForCustomerScreen.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(GetCollectionNudgeForCustomerScreen.Show.PAYMENT_SIDE)
        )
        testObserver.dispose()
    }

    @Test
    fun `execute should return show on credit side`() {
        val totalAddTxnCountOnTrigger = 5
        val totalAddTxnCount = 9
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(false))
        whenever(getTotalTxnCount.execute()).thenReturn(Single.just(totalAddTxnCount))
        whenever(customerRepository.getTxnCntForCollectionNudgeOnCustomerScr(businessId)).thenReturn(
            totalAddTxnCountOnTrigger
        )
        whenever(ab.isFeatureEnabled(COLLECTION_FEATURE)).thenReturn(Observable.just(true))
        whenever(ab.isExperimentEnabled(EXPT)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(EXPT)).thenReturn(Observable.just(CREDIT_SIDE_VARIANT))

        val testObserver = getCollectionNudgeForCustomerScreen.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(GetCollectionNudgeForCustomerScreen.Show.CREDIT_SIDE)
        )
        testObserver.dispose()
    }

    @Test
    fun `execute should return show none if experiment not enabled`() {
        val totalAddTxnCountOnTrigger = 5
        val totalAddTxnCount = 9
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(false))
        whenever(getTotalTxnCount.execute()).thenReturn(Single.just(totalAddTxnCount))
        whenever(customerRepository.getTxnCntForCollectionNudgeOnCustomerScr(businessId)).thenReturn(
            totalAddTxnCountOnTrigger
        )
        whenever(ab.isFeatureEnabled(COLLECTION_FEATURE)).thenReturn(Observable.just(true))
        whenever(ab.isExperimentEnabled(EXPT)).thenReturn(Observable.just(false))
        whenever(ab.getExperimentVariant(EXPT)).thenReturn(Observable.just(PAYMENT_SIDE_VARIANT))

        val testObserver = getCollectionNudgeForCustomerScreen.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(GetCollectionNudgeForCustomerScreen.Show.NONE)
        )
        testObserver.dispose()
    }

    @Test
    fun `execute should return show none if feature not enabled`() {
        val totalAddTxnCountOnTrigger = 5
        val totalAddTxnCount = 9
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(false))
        whenever(getTotalTxnCount.execute()).thenReturn(Single.just(totalAddTxnCount))
        whenever(customerRepository.getTxnCntForCollectionNudgeOnCustomerScr(businessId)).thenReturn(
            totalAddTxnCountOnTrigger
        )
        whenever(ab.isFeatureEnabled(COLLECTION_FEATURE)).thenReturn(Observable.just(false))
        whenever(ab.isExperimentEnabled(EXPT)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(EXPT)).thenReturn(Observable.just(PAYMENT_SIDE_VARIANT))

        val testObserver = getCollectionNudgeForCustomerScreen.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(GetCollectionNudgeForCustomerScreen.Show.NONE)
        )
        testObserver.dispose()
    }
}
