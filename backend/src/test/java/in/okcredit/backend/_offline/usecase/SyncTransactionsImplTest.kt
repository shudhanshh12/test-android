package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend._offline.serverV2.ServerV2
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncDirtyTransactions
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.backend._offline.usecase._sync_usecases.TransactionsSyncService
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.core.analytics.CoreTracker
import `in`.okcredit.merchant.core.common.Timestamp
import android.content.Context
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Single
import org.apache.commons.jcs.access.exception.InvalidArgumentException
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.workmanager.OkcWorkManager

class SyncTransactionsImplTest {

    private val serverV2: ServerV2 = mock()
    private val transactionRepo: TransactionRepo = mock()
    private val transferUtility: TransferUtility = mock()
    private val tracker: Tracker = mock()
    private val transactionsSyncService: TransactionsSyncService = mock()
    private val syncDirtyTransactions: SyncDirtyTransactions = mock()
    private val workManager: OkcWorkManager = mock()
    private val context: Context = mock()
    private val coreSdk: CoreSdk = mock()
    private val coreTracker: CoreTracker = mock()
    private val firebasePerformance: FirebasePerformance = mock()
    private val firebaseRemoteConfig: FirebaseRemoteConfig = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private lateinit var syncTransactionsImpl: SyncTransactionsImpl

    @Before
    fun setup() {
        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit

        val trace: Trace = mock()
        whenever(firebasePerformance.newTrace(any())).thenReturn(trace)

        syncTransactionsImpl = SyncTransactionsImpl(
            { serverV2 },
            { transactionRepo },
            { transferUtility },
            { tracker },
            { transactionsSyncService },
            { syncDirtyTransactions },
            { workManager },
            { context },
            { coreSdk },
            { coreTracker },
            { firebasePerformance },
            { firebaseRemoteConfig },
            { getActiveBusinessId }
        )
    }

    @Test
    fun `should track force sync start (core enable)`() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId))
            .thenReturn(Single.just(businessId))
        whenever(coreSdk.isCoreSdkFeatureEnabled(businessId))
            .thenReturn(Single.just(true))

        val testObserver = syncTransactionsImpl.executeForceSync(businessId).test()

        verify(coreTracker).trackForceTransactionSyncStarted(true)

        testObserver.dispose()
    }

    @Test
    fun `should track force sync complete if job completes (core enable)`() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId))
            .thenReturn(Single.just(businessId))
        whenever(coreSdk.isCoreSdkFeatureEnabled(businessId))
            .thenReturn(Single.just(true))

        whenever(coreSdk.lastUpdatedTransactionTime(businessId))
            .thenReturn(Single.just(Timestamp(100000000)))

        doNothing().whenever(coreTracker).trackForceTransactionSyncStarted(any())

        // TODO: mocking multiple calls of same method with same parameters. (required to test totalDifferenceInTransaction)
        whenever(coreSdk.getNumberOfSyncTransactionsTillGivenUpdatedTime(100000000, businessId))
            .thenReturn(Single.just(10))

        whenever(
            coreSdk.syncTransactions(
                source = "force_sync",
                req = null,
                isFromSyncScreen = false,
                isFromForceSync = true,
                businessId = businessId
            )
        )
            .thenReturn(Completable.complete())

        val testObserver = syncTransactionsImpl.executeForceSync(businessId).test()

        verify(coreTracker).trackForceTransactionSyncStarted(true)
        verify(coreTracker).trackForceSyncTransactionsSuccess(isCoreSync = true, totalDifferenceInTransaction = 0)

        testObserver.dispose()
    }

    @Test
    fun `should track force sync error when job throws exception (core enable)`() {
        val businessId = "business-id"
        whenever(coreSdk.isCoreSdkFeatureEnabled(businessId))
            .thenReturn(Single.just(true))

        whenever(coreSdk.lastUpdatedTransactionTime(businessId))
            .thenReturn(Single.just(Timestamp(100000000)))

        val error = InvalidArgumentException()
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId))
            .thenReturn(Single.just(businessId))
        doNothing().whenever(coreTracker).trackForceTransactionSyncStarted(any())

        whenever(coreSdk.getNumberOfSyncTransactionsTillGivenUpdatedTime(100000000, businessId))
            .thenReturn(Single.just(14))

        whenever(
            coreSdk.syncTransactions(
                source = "force_sync",
                req = null,
                isFromSyncScreen = false,
                isFromForceSync = true,
                businessId = businessId
            )
        )
            .thenReturn(Completable.error(error))
        val testObserver = syncTransactionsImpl.executeForceSync(businessId).test()

        verify(coreTracker).trackForceTransactionSyncStarted(true)
        verify(coreTracker).trackForceSyncTransactionsError(true, error)

        testObserver.dispose()
    }
}
