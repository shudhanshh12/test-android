package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend.contract.GetTotalTxnCount
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.customer.contract.CustomerRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.crashlytics.RecordException

class GetCollectionNudgeOnSetDueDateTest {

    private val ab: AbRepository = mock()
    private val getTotalTxnCount: GetTotalTxnCount = mock()
    private val customerRepository: CustomerRepository = mock()
    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val businessId = "businessId"

    private val getCollectionNudgeOnSetDueDate: GetCollectionNudgeOnSetDueDate =
        GetCollectionNudgeOnSetDueDate(
            { ab },
            { getTotalTxnCount },
            { customerRepository },
            { collectionRepository },
            { getActiveBusinessId },
        )

    companion object {
        private const val EXPT = "postlogin_android-all-collection_adoption_post_due_date"
        private const val SHOW = "show"
        private const val DONT_SHOW = "dont_show"
        private const val COLLECTION_FEATURE = "collection"
    }

    @Before
    fun setup() {
        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
    }

    @Test
    fun `execute() should return true`() {
        val totalAddTxnCountOnTrigger = 8
        val totalAddTxnCount = 9
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(false))
        whenever(getTotalTxnCount.execute()).thenReturn(Single.just(totalAddTxnCount))
        whenever(customerRepository.getTxnCntForCollectionNudgeOnSetDueDate(businessId)).thenReturn(totalAddTxnCountOnTrigger)
        whenever(ab.isFeatureEnabled(COLLECTION_FEATURE, businessId = businessId)).thenReturn(Observable.just(true))
        whenever(ab.isExperimentEnabled(EXPT)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(EXPT)).thenReturn(Observable.just(SHOW))

        val testObserver = getCollectionNudgeOnSetDueDate.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(true)
        )
        testObserver.dispose()
    }

    @Test
    fun `execute() should return false if exp not enabled`() {
        val totalAddTxnCountOnTrigger = 8
        val totalAddTxnCount = 9
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(false))
        whenever(getTotalTxnCount.execute()).thenReturn(Single.just(totalAddTxnCount))
        whenever(customerRepository.getTxnCntForCollectionNudgeOnSetDueDate(businessId)).thenReturn(totalAddTxnCountOnTrigger)
        whenever(ab.isFeatureEnabled(COLLECTION_FEATURE, businessId = businessId)).thenReturn(Observable.just(true))
        whenever(ab.isExperimentEnabled(EXPT)).thenReturn(Observable.just(false))
        whenever(ab.getExperimentVariant(EXPT)).thenReturn(Observable.just(SHOW))

        val testObserver = getCollectionNudgeOnSetDueDate.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )
        testObserver.dispose()
    }

    @Test
    fun `execute() should return false if feature not enabled`() {
        val totalAddTxnCountOnTrigger = 8
        val totalAddTxnCount = 9
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(false))
        whenever(getTotalTxnCount.execute()).thenReturn(Single.just(totalAddTxnCount))
        whenever(customerRepository.getTxnCntForCollectionNudgeOnSetDueDate(businessId)).thenReturn(totalAddTxnCountOnTrigger)
        whenever(ab.isFeatureEnabled(COLLECTION_FEATURE, businessId = businessId)).thenReturn(Observable.just(false))
        whenever(ab.isExperimentEnabled(EXPT)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(EXPT)).thenReturn(Observable.just(SHOW))

        val testObserver = getCollectionNudgeOnSetDueDate.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )
        testObserver.dispose()
    }

    @Test
    fun `execute() should return false if collection activated`() {
        val totalAddTxnCountOnTrigger = 8
        val totalAddTxnCount = 9
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(true))
        whenever(getTotalTxnCount.execute()).thenReturn(Single.just(totalAddTxnCount))
        whenever(customerRepository.getTxnCntForCollectionNudgeOnSetDueDate(businessId)).thenReturn(totalAddTxnCountOnTrigger)
        whenever(ab.isFeatureEnabled(COLLECTION_FEATURE, businessId = businessId)).thenReturn(Observable.just(false))
        whenever(ab.isExperimentEnabled(EXPT)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(EXPT)).thenReturn(Observable.just(SHOW))

        val testObserver = getCollectionNudgeOnSetDueDate.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )
        testObserver.dispose()
    }
}
