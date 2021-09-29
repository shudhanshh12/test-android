package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.model.DueInfo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.backend.contract.GetTotalTxnCount
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.customer.contract.CustomerRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.crashlytics.RecordException

class GetCollectionNudgeOnDueDateCrossedTest {

    private val getCustomer: GetCustomer = mock()
    private val ab: AbRepository = mock()
    private val getTotalTxnCount: GetTotalTxnCount = mock()
    private val customerRepository: CustomerRepository = mock()
    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val businessId = "businessId"

    private val getCollectionNudgeOnDueDateCrossed: GetCollectionNudgeOnDueDateCrossed =
        GetCollectionNudgeOnDueDateCrossed(
            { getCustomer },
            { ab },
            { getTotalTxnCount },
            { customerRepository },
            { collectionRepository },
            { getActiveBusinessId },
        )

    companion object {
        private const val EXPT = "postlogin_android-all-due_date_crossed"
        private const val CONTROL = "control"
        private const val COLLECTIONS = "collections"
        private const val UPDATE_DUE_DATE = "update_due_date"
        private const val FEATURE_COLLECTIONS = "collection"
    }

    private var dueInfo = DueInfo("asd", true)

    @Before
    fun setUp() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics

        dueInfo.activeDate = mock()
        val currentDate = DateTime.now().withTimeAtStartOfDay()
        mockkStatic(DateTime::class)
        mockkObject(DateTime.now())
        every { DateTime.now().withTimeAtStartOfDay() } returns currentDate
        whenever(dueInfo.activeDate!!.isBefore(currentDate)).thenReturn(true)

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
    }

    @Test
    fun `execute should return response update variant`() {
        val totalAddTxnCountOnTrigger = 8
        val totalAddTxnCount = 9
        val customer: Customer = mock()
        val show = GetCollectionNudgeOnDueDateCrossed.Show.UPDATE
        val response = GetCollectionNudgeOnDueDateCrossed.Response(customer, dueInfo, show)

        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(false))
        whenever(getTotalTxnCount.execute()).thenReturn(Single.just(totalAddTxnCount))
        whenever(customerRepository.getTxnCntForCollectionNudgeOnDueDateCrossed(businessId)).thenReturn(totalAddTxnCountOnTrigger)
        whenever(ab.isFeatureEnabled(FEATURE_COLLECTIONS, businessId = businessId)).thenReturn(Observable.just(true))
        whenever(ab.isExperimentEnabled(EXPT)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(EXPT)).thenReturn(Observable.just(UPDATE_DUE_DATE))
        whenever(getCustomer.execute(dueInfo.customerId)).thenReturn(Observable.just(customer))

        val testObserver = getCollectionNudgeOnDueDateCrossed.execute(dueInfo).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(response)
        )
        testObserver.dispose()
    }
}
