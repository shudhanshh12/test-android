package `in`.okcredit.merchant.core.sync

import `in`.okcredit.accounting_core.contract.SyncState
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.analytics.CoreTracker
import `in`.okcredit.merchant.core.common.toTimestamp
import `in`.okcredit.merchant.core.server.CoreRemoteSource
import `in`.okcredit.merchant.core.server.internal.CoreApiMessages.*
import `in`.okcredit.merchant.core.server.internal.CoreApiMessages.Companion.FILE_CREATION_COMPLETED
import `in`.okcredit.merchant.core.server.internal.CoreApiMessages.Companion.FILE_CREATION_IN_PROGRESS
import `in`.okcredit.merchant.core.server.internal.CoreApiMessages.Companion.TYPE_FILE
import `in`.okcredit.merchant.core.server.internal.CoreApiMessages.Companion.TYPE_LIST
import `in`.okcredit.merchant.core.store.CoreLocalSource
import android.content.Context
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.*
import com.squareup.moshi.Moshi
import dagger.Lazy
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.io.File

class SyncTransactionsTest {

    private val workManagerLazy: Lazy<OkcWorkManager> = mock()
    private val localSourceLazy: Lazy<CoreLocalSource> = mock()
    private val remoteSourceLazy: Lazy<CoreRemoteSource> = mock()
    private val trackerLazy: Lazy<CoreTracker> = mock()
    private val syncTransactionsCommandsLazy: Lazy<SyncTransactionsCommands> = mock()
    private val transferUtilityLazy: Lazy<TransferUtility> = mock()
    private val rxPrefLazy: Lazy<DefaultPreferences> = mock()
    private val contextLazy: Lazy<Context> = mock()
    private val moshiLazy: Lazy<Moshi> = mock()
    private val firebasePerformanceLazy: Lazy<FirebasePerformance> = mock()

    private val workManager: OkcWorkManager = mock()
    private val localSource: CoreLocalSource = mock()
    private val remoteSource: CoreRemoteSource = mock()
    private val tracker: CoreTracker = mock()
    private val syncTransactionsCommands: SyncTransactionsCommands = mock()
    private val transferUtility: TransferUtility = mock()
    private val rxPref: DefaultPreferences = mock()
    private val context: Context = mock()
    private val firebasePerformance: FirebasePerformance = mock()
    private val firebaseRemoteConfig: FirebaseRemoteConfig = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private lateinit var syncTransactions: CoreTransactionSyncer

    @Before
    fun setUp() {
        whenever(workManagerLazy.get()).thenReturn(workManager)
        whenever(localSourceLazy.get()).thenReturn(localSource)
        whenever(remoteSourceLazy.get()).thenReturn(remoteSource)
        whenever(trackerLazy.get()).thenReturn(tracker)
        whenever(syncTransactionsCommandsLazy.get()).thenReturn(syncTransactionsCommands)
        whenever(transferUtilityLazy.get()).thenReturn(transferUtility)
        whenever(rxPrefLazy.get()).thenReturn(rxPref)
        whenever(contextLazy.get()).thenReturn(context)
        whenever(firebasePerformanceLazy.get()).thenReturn(firebasePerformance)

        val trace: Trace = mock()
        whenever(firebasePerformance.newTrace(any())).thenReturn(trace)
        syncTransactions = SyncTransactions(
            workManagerLazy,
            localSourceLazy,
            remoteSourceLazy,
            trackerLazy,
            syncTransactionsCommandsLazy,
            transferUtilityLazy,
            rxPrefLazy,
            contextLazy,
            moshiLazy,
            firebasePerformanceLazy,
            { firebaseRemoteConfig },
            { getActiveBusinessId }
        )

        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit

        mockkObject(ThreadUtils)
        every { ThreadUtils.database() } returns Schedulers.trampoline()
        every { ThreadUtils.computation() } returns Schedulers.trampoline()
        every { ThreadUtils.newThread() } returns Schedulers.trampoline()
    }

    @Test
    fun `execute() given source is sync screen when sync is not completed once and server response type is list should perform complete sync`() {
        val businessId = "business-id"
        whenever(syncTransactionsCommands.execute(businessId)).thenReturn(Completable.complete())
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
        whenever(rxPref.getLong(eq("last_transaction_sync_time"), any(), anyOrNull())).thenReturn(flowOf(0L))
        whenever(localSource.lastUpdatedTransactionTime(businessId)).thenReturn(Single.just(0L.toTimestamp()))
        val behaviorSubject: BehaviorSubject<SyncState> = BehaviorSubject.createDefault(SyncState.WAITING)
        whenever(localSource.clearCommandTableForBusiness(businessId)).thenReturn(Completable.complete())
        whenever(localSource.clearTransactionTableForBusiness(businessId)).thenReturn(Completable.complete())
        val transactionList = mutableListOf<Transaction>()
        repeat(100) {
            val timestamp = System.currentTimeMillis()
            transactionList.add(
                Transaction(
                    id = "txn_id$it",
                    account_id = "cust_id",
                    type = 1,
                    amount = it.times(10).toLong(),
                    create_time_ms = timestamp,
                    deleted = false,
                    bill_date_ms = timestamp,
                    update_time_ms = timestamp,
                    alert_sent_by_creator = false,
                    transaction_state = 1,
                    tx_category = 0
                )
            )
        }
        val response = GetTransactionsResponse(TYPE_LIST, ListData(transactionList), null)
        whenever(remoteSource.getTransactions(any(), any(), eq(businessId))).thenReturn(Single.just(response))
        whenever(localSource.putTransactions(any(), eq(businessId))).thenReturn(Completable.complete())

        val testObserver = syncTransactions.execute("source", behaviorSubject, true, businessId = businessId).test().awaitCount(1)

        testObserver.assertComplete()
        verify(remoteSource, times(1)).getTransactions(any(), any(), eq(businessId))
        verify(localSource, times(1)).putTransactions(any(), eq(businessId))

        // since user has not logged off (indicated by -1L as last sync time) these will not called
        // verify(localSource, times(1)).clearCommandTable()
        // verify(localSource, times(1)).clearTransactionTable()
    }

    @Test
    fun `execute() given source is not sync screen when sync is not completed once should return`() {
        val businessId = "business-id"
        whenever(syncTransactionsCommands.execute(businessId)).thenReturn(Completable.complete())
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
        whenever(rxPref.getLong(eq("last_transaction_sync_time"), any(), anyOrNull())).thenReturn(flowOf(0L))
        whenever(localSource.lastUpdatedTransactionTime(businessId)).thenReturn(Single.just(0L.toTimestamp()))
        val behaviorSubject: BehaviorSubject<SyncState> = BehaviorSubject.createDefault(SyncState.WAITING)

        val testObserver = syncTransactions.execute("source", behaviorSubject, false, businessId = businessId).test()

        testObserver.assertComplete()
        verify(remoteSource, times(0)).getTransactions(any(), any(), eq(businessId))
        verify(localSource, times(0)).putTransactions(any(), eq(businessId))
    }

    // @Test Todo WIP
    fun `execute() given source is sync screen when sync is not completed once and server response type is file should perform complete sync`() {
        whenever(syncTransactionsCommands.execute()).thenReturn(Completable.complete())
        val businessId = "business-id"
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
        whenever(rxPref.getLong(eq("last_transaction_sync_time"), any(), anyOrNull())).thenReturn(flowOf(0L))
        whenever(localSource.lastUpdatedTransactionTime(businessId)).thenReturn(Single.just(0L.toTimestamp()))
        val behaviorSubject: BehaviorSubject<SyncState> = BehaviorSubject.createDefault(SyncState.WAITING)
        whenever(localSource.clearCommandTableForBusiness(businessId)).thenReturn(Completable.complete())
        whenever(localSource.clearTransactionTableForBusiness(businessId)).thenReturn(Completable.complete())
        val transactionList = mutableListOf<Transaction>()
        repeat(100) {
            val timestamp = System.currentTimeMillis()
            transactionList.add(
                Transaction(
                    id = "txn_id$it",
                    account_id = "cust_id",
                    type = 1,
                    amount = it.times(10).toLong(),
                    create_time_ms = timestamp,
                    deleted = false,
                    bill_date_ms = timestamp,
                    update_time_ms = timestamp,
                    alert_sent_by_creator = false,
                    transaction_state = 1,
                    tx_category = 0
                )
            )
        }
        val response = GetTransactionsResponse(
            TYPE_FILE,
            null,
            FileData(
                TransactionFile(
                    "file_id",
                    1,
                    "encryption_key",
                    "https://okcredit-transaction-files.s3.ap-south-1.amazonaws.com"
                )
            )
        )
        whenever(remoteSource.getTransactions(any(), any(), eq(businessId))).thenReturn(Single.just(response))
        val response1 = GetTransactionFileResponse(TransactionFile("file_id", FILE_CREATION_IN_PROGRESS, null, null))
        val response2 = GetTransactionFileResponse(TransactionFile("file_id", FILE_CREATION_IN_PROGRESS, null, null))
        val response3 = GetTransactionFileResponse(TransactionFile("file_id", FILE_CREATION_IN_PROGRESS, null, null))
        val response4 = GetTransactionFileResponse(TransactionFile("file_id", FILE_CREATION_IN_PROGRESS, null, null))
        val response5 = GetTransactionFileResponse(TransactionFile("file_id", FILE_CREATION_IN_PROGRESS, null, null))
        val response6 = GetTransactionFileResponse(TransactionFile("file_id", FILE_CREATION_IN_PROGRESS, null, null))
        val response7 = GetTransactionFileResponse(TransactionFile("file_id", FILE_CREATION_IN_PROGRESS, null, null))
        val response8 = GetTransactionFileResponse(TransactionFile("file_id", FILE_CREATION_COMPLETED, "key", "file"))
        whenever(remoteSource.getTransactionFile("file_id", businessId)).thenReturn(Single.just(response1))
            .thenReturn(Single.just(response2))
            .thenReturn(Single.just(response3)).thenReturn(Single.just(response4)).thenReturn(Single.just(response5))
            .thenReturn(Single.just(response6)).thenReturn(Single.just(response7)).thenReturn(Single.just(response8))
        whenever(context.startService(any())).thenReturn(mock())
        val observer = mock<TransferObserver>()
        whenever(transferUtility.download(anyOrNull(), anyOrNull(), anyOrNull<File>())).thenReturn(observer)
        whenever(localSource.putTransactions(any(), eq(businessId))).thenReturn(Completable.complete())

        // TODO download() and decrypt() functions are not testable
        val testObserver = syncTransactions.execute("source", behaviorSubject, true).test()

        testObserver.assertComplete()
        verify(remoteSource, times(1)).getTransactions(any(), any(), eq(businessId))
        verify(localSource, times(1)).putTransactions(any(), eq(businessId))
        verify(localSource, times(1)).clearCommandTableForBusiness(businessId)
        verify(localSource, times(1)).clearTransactionTableForBusiness(businessId)
        verify(tracker, times(1)).trackSyncTransactions(eq("1_Started"), any(), any(), any(), any())
        verify(tracker, times(1)).trackSyncTransactions(eq("2_Server_Call"), any(), any(), any(), any())
        verify(tracker, times(1)).trackSyncTransactions(eq("3_Server_Call_Completed"), any(), any(), any(), any())
        verify(tracker, times(1)).trackSyncTransactions(eq("4-1_saving_into_database"), any(), any(), any(), any())
        verify(tracker, times(1)).trackSyncTransactions(eq("5_Completed"), any(), any(), any(), any())
    }

    @Test
    fun `execute() with force sync should send last sync time as 0 to server`() {
        val businessId = "business-id"
        whenever(syncTransactionsCommands.execute(businessId)).thenReturn(Completable.complete())
        whenever(rxPref.getLong(eq("last_transaction_sync_time"), any(), anyOrNull())).thenReturn(flowOf(120L))
        whenever(localSource.lastUpdatedTransactionTime(businessId)).thenReturn(Single.just(12120L.toTimestamp()))
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
        val behaviorSubject: BehaviorSubject<SyncState> = BehaviorSubject.createDefault(SyncState.WAITING)

        val testObserver = syncTransactions.execute(
            "source", behaviorSubject,
            isFromSyncScreen = false,
            isFromForceSync = true,
            businessId = businessId
        ).test()

        verify(remoteSource).getTransactions(0L, "source", businessId)
    }
}
