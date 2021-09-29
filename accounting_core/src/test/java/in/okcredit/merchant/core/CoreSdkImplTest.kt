package `in`.okcredit.merchant.core

import `in`.okcredit.accounting_core.contract.SuggestedCustomerIdsForAddTransaction
import `in`.okcredit.accounting_core.contract.SyncState
import `in`.okcredit.fileupload.usecase.IUploadFile
import `in`.okcredit.merchant.core.analytics.CoreTracker
import `in`.okcredit.merchant.core.common.CoreException
import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.common.TimestampUtils
import `in`.okcredit.merchant.core.common.toTimestamp
import `in`.okcredit.merchant.core.model.Customer
import `in`.okcredit.merchant.core.model.Transaction
import `in`.okcredit.merchant.core.model.TransactionAmountHistory
import `in`.okcredit.merchant.core.server.CoreRemoteSource
import `in`.okcredit.merchant.core.server.internal.bulk_search_transactions.BulkSearchTransactionsResponse
import `in`.okcredit.merchant.core.store.CoreLocalSource
import `in`.okcredit.merchant.core.store.database.BulkReminderDbInfo
import `in`.okcredit.merchant.core.sync.CoreTransactionSyncer
import `in`.okcredit.merchant.core.sync.SyncCustomer
import `in`.okcredit.merchant.core.sync.SyncCustomers
import `in`.okcredit.merchant.core.sync.SyncTransactionsCommands
import `in`.okcredit.merchant.core.usecase.ClearAllLocalData
import `in`.okcredit.merchant.core.usecase.OfflineAddCustomerAbHelper
import com.nhaarman.mockitokotlin2.*
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.ThreadUtils

class CoreSdkImplTest {

    companion object {
        private const val PREF_BUSINESS_CORE_SDK_ENABLED = "core_sdk_enabled"
    }

    private val mockLocalSource: CoreLocalSource = mock()
    private val mockRemoteSource: CoreRemoteSource = mock()
    private val mockUploadFile: IUploadFile = mock()
    private val mockTracker: CoreTracker = mock()
    private val mockSyncTransactions: CoreTransactionSyncer = mock()
    private val mockSyncTransactionsCommands: SyncTransactionsCommands = mock()
    private val mockSyncCustomer: SyncCustomer = mock()
    private val mockSyncCustomers: SyncCustomers = mock()
    private val mockRxPref: DefaultPreferences = mock()
    private val mockSchedulerProvider: SchedulerProvider = mock()
    private val mockOfflineAddCustomerAbHelper: OfflineAddCustomerAbHelper = mock()
    private val mockClearAllLocalData: ClearAllLocalData = mock()
    private val coreSdkImpl = CoreSdkImpl(
        { mockLocalSource },
        { mockRemoteSource },
        { mockUploadFile },
        { mockTracker },
        { mockSyncTransactions },
        { mockSyncTransactionsCommands },
        { mockSyncCustomer },
        { mockSyncCustomers },
        { mockRxPref },
        { mockSchedulerProvider },
        { mockOfflineAddCustomerAbHelper },
        { mockClearAllLocalData }
    )
    private val coreSdk: CoreSdk = coreSdkImpl
    private val suggestedCustomerIdsForAddTransaction: SuggestedCustomerIdsForAddTransaction = coreSdkImpl

    @Before
    fun setup() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.database() } returns Schedulers.trampoline()
    }

    @Test
    fun `isCoreSdkFeatureEnabled() should get value from shared pref`() {
        val businessId = "businessId"
        whenever(mockRxPref.getBoolean(eq(PREF_BUSINESS_CORE_SDK_ENABLED), any(), anyOrNull())).thenReturn(flowOf(true))

        val testObserver = coreSdk.isCoreSdkFeatureEnabled(businessId).test()

        testObserver.assertValue(true)
    }

    @Test
    fun `setCoreSdkFeatureStatus() should set true value in shared pref`() {
        runBlocking {
            val businessId = "businessId"
            whenever(mockRxPref.getBoolean(eq(PREF_BUSINESS_CORE_SDK_ENABLED), any(), anyOrNull())).thenReturn(
                flowOf(
                    true
                )
            )

            val testObserver = coreSdk.setCoreSdkFeatureStatus(true, businessId).test().awaitCount(1)

            verify(mockRxPref).set(eq(PREF_BUSINESS_CORE_SDK_ENABLED), eq(true), any())
            testObserver.assertComplete()
        }
    }

    @Test
    fun `setCoreSdkFeatureStatus() should set false value in shared pref`() {
        runBlocking {
            val businessId = "businessId"
            whenever(
                mockRxPref.getBoolean(
                    eq(PREF_BUSINESS_CORE_SDK_ENABLED),
                    any(),
                    anyOrNull()
                )
            ).thenReturn(flowOf(false))

            val testObserver = coreSdk.setCoreSdkFeatureStatus(false, businessId).test().awaitCount(1)

            verify(mockRxPref).set(eq(PREF_BUSINESS_CORE_SDK_ENABLED), eq(false), any())
            testObserver.assertComplete()
        }
    }

    @Test
    fun `clearLocalData() should call clear on all tables and shared pref`() {
        runBlocking {
            whenever(mockLocalSource.clearCommandTableForBusiness(TestData.BUSINESS_ID)).thenReturn(Completable.complete())
            whenever(mockLocalSource.clearTransactionTableForBusiness(TestData.BUSINESS_ID)).thenReturn(Completable.complete())
            whenever(mockLocalSource.clearCustomerTableForBusiness(TestData.BUSINESS_ID)).thenReturn(Completable.complete())

            val testObserver = coreSdk.clearLocalData(TestData.BUSINESS_ID).test()

            verify(mockRxPref).remove(eq(PREF_BUSINESS_CORE_SDK_ENABLED), any())
            testObserver.assertComplete()
        }
    }

    @Test
    fun `clearLocalData() should call clearAllLocalData on all tables`() {
        runBlocking {
            whenever(mockClearAllLocalData.execute()).thenReturn(Completable.complete())

            val testObserver = coreSdk.clearLocalData().test()

            verify(mockClearAllLocalData).execute()
            testObserver.assertComplete()
        }
    }

    @Test
    fun `process() given add transaction command when no transaction id conflict should return transaction`() {
        val timestamp = TimestampUtils.currentTimestamp()
        val command: Command.CreateTransaction = mock()
        val transaction: Transaction = mock()
        val businessId = "business-id"
        whenever(transaction.id).thenReturn("txn_id")
        whenever(command.transactionId).thenReturn("txn_id")
        whenever(command.timestamp).thenReturn(timestamp)
        whenever(command.customerId).thenReturn("cust_id")
        whenever(command.type).thenReturn(Transaction.Type.CREDIT)
        whenever(command.amount).thenReturn(10000)
        whenever(command.transactionImages).thenReturn(listOf())
        whenever(command.note).thenReturn("Note1")
        whenever(command.commandType).thenReturn(Command.CommandType.CREATE_TRANSACTION)
        whenever(mockLocalSource.getTransaction("txn_id")).thenReturn(Observable.just(transaction))
        whenever(mockLocalSource.isTransactionPresent("txn_id")).thenReturn(Single.just(false))
        whenever(
            mockLocalSource.createTransaction(
                any(),
                eq(command),
                eq(businessId)
            )
        ).thenReturn(Completable.complete())
        whenever(mockUploadFile.schedule(any(), any(), any())).thenReturn(Completable.complete())
        whenever(mockSyncTransactions.schedule(Command.CommandType.CREATE_TRANSACTION.name, businessId)).thenReturn(
            Completable.complete()
        )

        val testObserver = coreSdk.processTransactionCommand(command, businessId).test()

        verify(mockTracker, times(0)).trackDebug(eq(CoreTracker.DebugType.LOCAL_CONFLICT), any())
        verify(mockLocalSource).createTransaction(any(), eq(command), eq(businessId))
        verify(mockUploadFile, times(0)).schedule(any(), any(), any())
        verify(mockSyncTransactions).schedule(Command.CommandType.CREATE_TRANSACTION.name, businessId)
        testObserver.assertValue(transaction)
    }

    @Test
    fun `process() given add transaction command when transaction id conflict once should handle conflict and return transaction`() {
        val timestamp = TimestampUtils.currentTimestamp()
        val command: Command.CreateTransaction = mock()
        val transaction: Transaction = mock()
        val businessId = "business-id"
        whenever(transaction.id).thenReturn("txn_id")
        whenever(command.transactionId).thenReturn("txn_id")
        whenever(command.timestamp).thenReturn(timestamp)
        whenever(command.customerId).thenReturn("cust_id")
        whenever(command.type).thenReturn(Transaction.Type.CREDIT)
        whenever(command.amount).thenReturn(10000)
        whenever(command.transactionImages).thenReturn(listOf())
        whenever(command.note).thenReturn("Note1")
        whenever(command.commandType).thenReturn(Command.CommandType.CREATE_TRANSACTION)
        whenever(mockLocalSource.getTransaction("txn_id")).thenReturn(Observable.just(transaction))
        whenever(mockLocalSource.isTransactionPresent("txn_id")).thenReturn(Single.just(true))
            .thenReturn(Single.just(false))
        whenever(
            mockLocalSource.createTransaction(
                any(),
                eq(command),
                eq(businessId)
            )
        ).thenReturn(Completable.complete())
        whenever(mockUploadFile.schedule(any(), any(), any())).thenReturn(Completable.complete())
        whenever(mockSyncTransactions.schedule(Command.CommandType.CREATE_TRANSACTION.name, businessId)).thenReturn(
            Completable.complete()
        )
        doNothing().whenever(mockTracker).trackDebug(any(), any())

        val testObserver = coreSdk.processTransactionCommand(command, businessId).test()

        verify(mockTracker).trackDebug(eq(CoreTracker.DebugType.LOCAL_CONFLICT), any())
        verify(mockLocalSource).createTransaction(any(), eq(command), eq(businessId))
        verify(mockUploadFile, times(0)).schedule(any(), any(), any())
        verify(mockSyncTransactions).schedule(Command.CommandType.CREATE_TRANSACTION.name, businessId)
        testObserver.assertValue(transaction)
    }

    @Test
    fun `process() given update transaction note command when transaction present should return transaction`() {
        val command: Command.UpdateTransactionNote = mock()
        val transaction: Transaction = mock()
        val businessId = "business-id"
        whenever(command.transactionId).thenReturn("txn_id")
        whenever(command.commandType).thenReturn(Command.CommandType.UPDATE_TRANSACTION_NOTE)
        whenever(mockLocalSource.getTransaction("txn_id")).thenReturn(Observable.just(transaction))
        whenever(mockLocalSource.isTransactionPresent("txn_id")).thenReturn(Single.just(true))
        whenever(mockLocalSource.updateTransactionNote(command, businessId)).thenReturn(Completable.complete())
        whenever(
            mockSyncTransactions.schedule(
                Command.CommandType.UPDATE_TRANSACTION_NOTE.name,
                businessId
            )
        ).thenReturn(
            Completable.complete()
        )
        doNothing().whenever(mockTracker).trackDebug(any(), any())

        val testObserver = coreSdk.processTransactionCommand(command, businessId).test()

        verify(mockTracker, times(0)).trackDebug(eq(CoreTracker.DebugType.TRANSACTION_NOT_FOUND), any())
        verify(mockLocalSource).updateTransactionNote(command, businessId)
        verify(mockSyncTransactions).schedule(Command.CommandType.UPDATE_TRANSACTION_NOTE.name, businessId)
        testObserver.assertValue(transaction)
    }

    @Test
    fun `process() given update transaction note command when transaction not present should call mockTracker and throw exception`() {
        val command: Command.UpdateTransactionNote = mock()
        val transaction: Transaction = mock()
        val businessId = "business-id"
        whenever(command.transactionId).thenReturn("txn_id")
        whenever(command.commandType).thenReturn(Command.CommandType.UPDATE_TRANSACTION_NOTE)
        whenever(mockLocalSource.getTransaction("txn_id")).thenReturn(Observable.just(transaction))
        whenever(mockLocalSource.isTransactionPresent("txn_id")).thenReturn(Single.just(false))
        whenever(mockLocalSource.updateTransactionNote(command, businessId)).thenReturn(Completable.complete())
        whenever(
            mockSyncTransactions.schedule(
                Command.CommandType.UPDATE_TRANSACTION_NOTE.name,
                businessId
            )
        ).thenReturn(
            Completable.complete()
        )
        doNothing().whenever(mockTracker).trackDebug(any(), any())

        val testObserver = coreSdk.processTransactionCommand(command, businessId).test()

        verify(mockTracker).trackDebug(eq(CoreTracker.DebugType.TRANSACTION_NOT_FOUND), any())
        verify(mockLocalSource, times(0)).updateTransactionNote(command, businessId)
        testObserver.assertError(CoreException.TransactionNotFoundException)
    }

    @Test
    fun `process() given delete transaction command when transaction present should return transaction`() {
        val command: Command.DeleteTransaction = mock()
        val transaction: Transaction = mock()
        val businessId = "business-id"
        whenever(command.transactionId).thenReturn("txn_id")
        whenever(command.commandType).thenReturn(Command.CommandType.DELETE_TRANSACTION)
        whenever(mockSyncCustomers.schedule(businessId)).thenReturn(Completable.complete())
        whenever(mockLocalSource.getTransaction("txn_id")).thenReturn(Observable.just(transaction))
        whenever(mockLocalSource.isTransactionPresent("txn_id")).thenReturn(Single.just(true))
        whenever(mockLocalSource.deleteTransaction(command, businessId)).thenReturn(Completable.complete())
        whenever(mockSyncTransactions.schedule(Command.CommandType.DELETE_TRANSACTION.name, businessId)).thenReturn(
            Completable.complete()
        )
        doNothing().whenever(mockTracker).trackDebug(any(), any())

        val testObserver = coreSdk.processTransactionCommand(command, businessId).test()

        verify(mockTracker, times(0)).trackDebug(eq(CoreTracker.DebugType.TRANSACTION_NOT_FOUND), any())
        verify(mockLocalSource).deleteTransaction(command, businessId)
        verify(mockSyncTransactions).schedule(Command.CommandType.DELETE_TRANSACTION.name, businessId)
        testObserver.assertValue(transaction)
    }

    @Test
    fun `process() given delete transaction command when transaction not present should call mockTracker and throw exception`() {
        val command: Command.DeleteTransaction = mock()
        val transaction: Transaction = mock()
        val businessId = "business-id"
        whenever(command.transactionId).thenReturn("txn_id")
        whenever(command.commandType).thenReturn(Command.CommandType.DELETE_TRANSACTION)
        whenever(mockSyncCustomers.schedule(businessId)).thenReturn(Completable.complete())
        whenever(mockLocalSource.getTransaction("txn_id")).thenReturn(Observable.just(transaction))
        whenever(mockLocalSource.isTransactionPresent("txn_id")).thenReturn(Single.just(false))
        whenever(mockLocalSource.deleteTransaction(command, businessId)).thenReturn(Completable.complete())
        whenever(mockSyncTransactions.schedule(Command.CommandType.DELETE_TRANSACTION.name, businessId)).thenReturn(
            Completable.complete()
        )
        doNothing().whenever(mockTracker).trackDebug(any(), any())

        val testObserver = coreSdk.processTransactionCommand(command, businessId).test()

        verify(mockTracker).trackDebug(eq(CoreTracker.DebugType.TRANSACTION_NOT_FOUND), any())
        verify(mockLocalSource, times(0)).deleteTransaction(command, businessId)
        testObserver.assertError(CoreException.TransactionNotFoundException)
    }

    @Test
    fun `process() given update transaction images command when transaction present should return transaction`() {
        val command: Command.UpdateTransactionImages = mock()
        val transaction: Transaction = mock()
        val businessId = "business-id"
        whenever(command.transactionId).thenReturn("txn_id")
        whenever(command.commandType).thenReturn(Command.CommandType.UPDATE_TRANSACTION_IMAGES)
        whenever(command.updatedImagesUriList).thenReturn(listOf("image1", "image2"))
        whenever(transaction.images).thenReturn(listOf())
        whenever(mockLocalSource.getTransaction("txn_id")).thenReturn(Observable.just(transaction))
        whenever(mockLocalSource.isTransactionPresent("txn_id")).thenReturn(Single.just(true))
        whenever(
            mockLocalSource.updateTransactionImagesAndInsertCommands(
                any(),
                any(),
                any(),
                eq(businessId)
            )
        ).thenReturn(Completable.complete())
        whenever(
            mockSyncTransactions.schedule(
                Command.CommandType.UPDATE_TRANSACTION_IMAGES.name,
                businessId
            )
        ).thenReturn(
            Completable.complete()
        )
        doNothing().whenever(mockTracker).trackDebug(any(), any())

        // TODO imageFile.exists() returns false
        val testObserver = coreSdk.processTransactionCommand(command, businessId).test()

        verify(mockLocalSource).updateTransactionImagesAndInsertCommands(any(), any(), any(), eq(businessId))
        whenever(
            mockSyncTransactions.schedule(
                Command.CommandType.UPDATE_TRANSACTION_IMAGES.name,
                businessId
            )
        ).thenReturn(
            Completable.complete()
        )
        testObserver.assertValues(transaction)
    }

    @Test
    fun `process() given update transaction images command when transaction not present should call mockTracker and throw exception`() {
        val command: Command.UpdateTransactionImages = mock()
        val transaction: Transaction = mock()
        val businessId = "business-id"
        whenever(command.transactionId).thenReturn("txn_id")
        whenever(command.commandType).thenReturn(Command.CommandType.UPDATE_TRANSACTION_IMAGES)
        whenever(command.updatedImagesUriList).thenReturn(listOf("image1", "image2"))
        whenever(transaction.images).thenReturn(listOf())
        whenever(mockLocalSource.getTransaction("txn_id")).thenReturn(Observable.just(transaction))
        whenever(mockLocalSource.isTransactionPresent("txn_id")).thenReturn(Single.just(false))
        whenever(
            mockLocalSource.updateTransactionImagesAndInsertCommands(
                any(),
                any(),
                any(),
                eq(businessId)
            )
        ).thenReturn(Completable.complete())
        whenever(
            mockSyncTransactions.schedule(
                Command.CommandType.UPDATE_TRANSACTION_IMAGES.name,
                businessId
            )
        ).thenReturn(
            Completable.complete()
        )
        doNothing().whenever(mockTracker).trackDebug(any(), any())

        val testObserver = coreSdk.processTransactionCommand(command, businessId).test()

        verify(mockTracker).trackDebug(eq(CoreTracker.DebugType.TRANSACTION_NOT_FOUND), any())
        verify(mockLocalSource, times(0)).updateTransactionImagesAndInsertCommands(any(), any(), any(), eq(businessId))
        testObserver.assertError(CoreException.TransactionNotFoundException)
    }

    @Test
    fun `syncCommands() should return completable`() {
        whenever(mockSyncTransactionsCommands.execute()).thenReturn(Completable.complete())

        val testObserver = coreSdk.syncTransactionsCommands().test()

        testObserver.assertComplete()
    }

    @Test
    fun `scheduleSyncTransactions() should schedule transactions sync`() {
        val businessId = "business-id"
        whenever(mockSyncTransactions.schedule("source", businessId)).thenReturn(Completable.complete())

        val testObserver = coreSdk.scheduleSyncTransactions("source", businessId = businessId).test()

        verify(mockSyncTransactions).schedule("source", businessId)
        testObserver.assertComplete()
    }

    @Test
    fun `mockSyncTransactions() when schedule is false should execute transactions sync`() {
        val behaviorSubject = BehaviorSubject.create<SyncState>()
        whenever(mockSyncTransactions.execute("source", behaviorSubject, true)).thenReturn(Completable.complete())

        val testObserver = coreSdk.syncTransactions("source", behaviorSubject, true).test()

        verify(mockSyncTransactions).execute("source", behaviorSubject, true)
        testObserver.assertComplete()
    }

    @Test
    fun `isTransactionPresent() when transaction present should return true`() {
        whenever(mockLocalSource.isTransactionPresent("txn_id")).thenReturn(Single.just(true))

        val testObserver = coreSdk.isTransactionPresent("txn_id").test()

        testObserver.assertValue(true)
    }

    @Test
    fun `isTransactionPresent() when transaction not present should return false`() {
        whenever(mockLocalSource.isTransactionPresent("txn_id_new")).thenReturn(Single.just(false))

        val testObserver = coreSdk.isTransactionPresent("txn_id_new").test()

        testObserver.assertValue(false)
    }

    @Test
    fun `isTransactionForCollectionPresent() when transaction for collection present should return true`() {
        whenever(mockLocalSource.isTransactionForCollectionPresent("collection_id", TestData.BUSINESS_ID)).thenReturn(
            Single.just(true)
        )

        val testObserver = coreSdk.isTransactionForCollectionPresent("collection_id", TestData.BUSINESS_ID).test()

        testObserver.assertValue(true)
    }

    @Test
    fun `isTransactionForCollectionPresent() when transaction for collection not present should return false`() {
        whenever(mockLocalSource.isTransactionForCollectionPresent("collection_id", TestData.BUSINESS_ID)).thenReturn(
            Single.just(false)
        )

        val testObserver = coreSdk.isTransactionForCollectionPresent("collection_id", TestData.BUSINESS_ID).test()

        testObserver.assertValue(false)
    }

    @Test
    fun `getTransaction() when transaction present should fetch from database and return transaction`() {
        val transaction: Transaction = mock()
        val businessId = "business-id"
        whenever(transaction.id).thenReturn("txn_id")
        whenever(mockLocalSource.isTransactionPresent("txn_id")).thenReturn(Single.just(true))
        whenever(mockLocalSource.getTransaction("txn_id")).thenReturn(Observable.just(transaction))

        val testObserver = coreSdk.getTransaction("txn_id", businessId).test()

        verify(mockRemoteSource, times(0)).getTransaction(any(), eq(businessId))
        testObserver.assertValue(transaction)
    }

    @Test
    fun `getTransaction() when transaction not present should fetch from server and return transaction`() {
        val transaction: Transaction = mock()
        val businessId = "business-id"
        whenever(transaction.id).thenReturn("txn_id")
        whenever(mockLocalSource.isTransactionPresent("txn_id")).thenReturn(Single.just(false))
        whenever(mockLocalSource.getTransaction("txn_id")).thenReturn(Observable.just(transaction))
        whenever(mockRemoteSource.getTransaction("txn_id", businessId)).thenReturn(Single.just(transaction))
        whenever(mockLocalSource.putTransactions(listOf(transaction), businessId)).thenReturn(Completable.complete())
        doNothing().whenever(mockTracker).trackDebug(any(), any())

        val testObserver = coreSdk.getTransaction("txn_id", businessId).test()

        verify(mockRemoteSource).getTransaction(any(), eq(businessId))
        verify(mockTracker).trackDebug(eq(CoreTracker.DebugType.TRANSACTION_NOT_FOUND), any())
        testObserver.assertValue(transaction)
    }

    @Test
    fun `getTransactionByCollectionId() when transaction for collection present should return transaction`() {
        val transaction: Transaction = mock()
        whenever(mockLocalSource.getTransactionByCollectionId("collection_id", TestData.BUSINESS_ID)).thenReturn(
            Observable.just(transaction)
        )

        val testObserver = coreSdk.getTransactionByCollectionId("collection_id", TestData.BUSINESS_ID).test()

        testObserver.assertValue(transaction)
    }

    @Test
    fun `getAllTransactionsCount() should return transaction count`() {
        val count = 3
        val businessId = "business-id"
        whenever(mockLocalSource.getAllTransactionsCount(businessId)).thenReturn(Single.just(count))

        val testObserver = coreSdk.getAllTransactionsCount(businessId).test()

        testObserver.assertValue(3)
    }

    @Test
    fun `getSyncedTransactionsCountTillGivenUpdatedTime() should return transaction count`() {
        val count = 3
        val businessId = "business-id"
        whenever(mockLocalSource.getTransactionsCountTillGivenUpdatedTime(Timestamp((1234)), businessId)).thenReturn(
            Single.just(
                count
            )
        )

        val testObserver = coreSdk.getNumberOfSyncTransactionsTillGivenUpdatedTime(1234, businessId).test()

        testObserver.assertValue(3)
    }

    @Test
    fun `listTransactions() should return list of transaction`() {
        val transactionList: List<Transaction> = listOf(mock(), mock(), mock())
        val businessId = "business-id"
        whenever(mockLocalSource.listTransactions(businessId)).thenReturn(Observable.just(transactionList))

        val testObserver = coreSdk.listTransactions(businessId).test()

        testObserver.assertValue(transactionList)
    }

    @Test
    fun `listActiveTransactionsBetweenBillDate() should return list of transaction between start and end time`() {
        val transactionList: List<Transaction> = listOf(mock(), mock(), mock())
        val startTimestamp = 1590578649L
        val endTimestamp = 1590578649L
        val businessId = "business-id"
        whenever(
            mockLocalSource.listActiveTransactionsBetweenBillDate(
                startTimestamp.toTimestamp(),
                endTimestamp.toTimestamp(),
                businessId
            )
        ).thenReturn(Observable.just(transactionList))

        val testObserver =
            coreSdk.listActiveTransactionsBetweenBillDate(startTimestamp, endTimestamp, businessId).test()

        testObserver.assertValue(transactionList)
    }

    @Test
    fun `listTransactions() should return list of transaction after start time by customer`() {
        val transactionList: List<Transaction> = listOf(mock(), mock(), mock())
        val startTimestamp = 1590578649L
        whenever(
            mockLocalSource.listTransactions(
                "cust_id",
                startTimestamp.toTimestamp(),
                TestData.BUSINESS_ID
            )
        ).thenReturn(
            Observable.just(
                transactionList
            )
        )

        val testObserver = coreSdk.listTransactions("cust_id", startTimestamp, TestData.BUSINESS_ID).test()

        testObserver.assertValue(transactionList)
    }

    @Test
    fun `listTransactions() should return list of transaction by customer`() {
        val transactionList: List<Transaction> = listOf(mock(), mock(), mock())
        whenever(mockLocalSource.listTransactions("cust_id")).thenReturn(Observable.just(transactionList))

        val testObserver = coreSdk.listTransactions("cust_id").test()

        testObserver.assertValue(transactionList)
    }

    @Test
    fun `listTransactionsByBillDate() should return list of transaction by bill date`() {
        val transactionList: List<Transaction> = listOf(mock(), mock(), mock())
        whenever(
            mockLocalSource.listNonDeletedTransactionsByBillDate(
                "cust_id",
                TestData.BUSINESS_ID
            )
        ).thenReturn(Observable.just(transactionList))

        val testObserver = coreSdk.listNonDeletedTransactionsByBillDate("cust_id", TestData.BUSINESS_ID).test()

        testObserver.assertValue(transactionList)
    }

    @Test
    fun `listDirtyTransactions() when true should return list of dirty transaction`() {
        val transactionList: List<Transaction> = listOf(mock(), mock(), mock())
        val businessId = "business-id"
        whenever(mockLocalSource.listDirtyTransactions(true, businessId)).thenReturn(Observable.just(transactionList))

        val testObserver = coreSdk.listDirtyTransactions(true, businessId).test()

        testObserver.assertValue(transactionList)
    }

    @Test
    fun `listDirtyTransactions() when false should return list of non-dirty transaction`() {
        val transactionList: List<Transaction> = listOf(mock(), mock(), mock())
        val businessId = "business-id"
        whenever(mockLocalSource.listDirtyTransactions(false, businessId)).thenReturn(Observable.just(transactionList))

        val testObserver = coreSdk.listDirtyTransactions(false, businessId).test()

        testObserver.assertValue(transactionList)
    }

    @Test
    fun `deleteLocalTransactionsForCustomer() should delete all transactions for customer`() {
        whenever(mockLocalSource.deleteLocalTransactionsForCustomer("cust_id")).thenReturn(Completable.complete())

        val testObserver = coreSdk.deleteLocalTransactionsForCustomer("cust_id").test()

        testObserver.assertComplete()
    }

    @Test
    fun `lastUpdatedTransactionTime() should return transaction last updated timestamp`() {
        val timestamp: Timestamp = mock()
        val businessId = "business-id"
        whenever(mockLocalSource.lastUpdatedTransactionTime(businessId)).thenReturn(Single.just(timestamp))

        val testObserver = coreSdk.lastUpdatedTransactionTime(businessId).test()

        testObserver.assertValue(timestamp)
    }

    @Test
    fun `createCustomer() should make server call and add customer to database return customer`() {
        val customer: Customer = mock()
        val businessId = "business-id"
        whenever(mockUploadFile.schedule(any(), any(), any())).thenReturn(Completable.complete())
        doNothing().whenever(mockTracker).trackDebug(any(), any())
        whenever(
            mockRemoteSource.addCustomer(
                "John Doe",
                "0987654321",
                false,
                null,
                businessId = businessId
            )
        ).thenReturn(Single.just(customer))
        whenever(mockLocalSource.putCustomer(customer, businessId)).thenReturn(Completable.complete())
        whenever(mockOfflineAddCustomerAbHelper.isEligibleForOfflineAddCustomer()).thenReturn(Single.just(true))
        whenever(mockOfflineAddCustomerAbHelper.isDisableOfflineAddCustomerFeature()).thenReturn(Single.just(true))

        // TODO imageFile.exists() returns false
        val testObserver = coreSdk.createCustomer("John Doe", "0987654321", businessId = businessId).test()

        verify(mockRemoteSource).addCustomer("John Doe", "0987654321", false, null, businessId = businessId)
        verify(mockLocalSource).putCustomer(customer, businessId)
        testObserver.assertValue(customer)
    }

    @Test
    fun `deleteCustomer() should sync customers, make server call and add deleted customer to database`() {
        val customer: Customer = mock()
        val businessId = "business-id"
        whenever(mockSyncCustomer.execute("cust_id", businessId)).thenReturn(Completable.complete())
        whenever(mockRemoteSource.deleteCustomer("cust_id", businessId)).thenReturn(Completable.complete())
        whenever(mockRemoteSource.getCustomer("cust_id", businessId)).thenReturn(Single.just(customer))
        whenever(mockLocalSource.putCustomer(customer, businessId)).thenReturn(Completable.complete())

        val testObserver = coreSdk.deleteCustomer("cust_id", businessId).test()

        verify(mockSyncCustomer).execute("cust_id", businessId)
        verify(mockRemoteSource).deleteCustomer("cust_id", businessId)
        verify(mockRemoteSource).getCustomer("cust_id", businessId)
        verify(mockLocalSource).putCustomer(customer, businessId)
        testObserver.assertComplete()
    }

    @Test
    fun `updateCustomer() should make server call and update customer in database`() {
        val customer: Customer = mock()
        val businessId = "business-id"
        whenever(
            mockRemoteSource.updateCustomer(
                customerId = "cust_id",
                desc = "John",
                address = null,
                profileImage = null,
                mobile = null,
                lang = "en",
                reminderMode = "sms",
                txnAlertEnabled = true,
                isForTxnEnable = false,
                dueInfoActiveDate = null,
                updateDueCustomDate = false,
                deleteDueCustomDate = false,
                addTransactionPermission = false,
                updateAddTransactionRestricted = false,
                blockTransaction = 1,
                updateBlockTransaction = false,
                businessId = "business-id"
            )
        ).thenReturn(Single.just(customer))
        whenever(mockLocalSource.putCustomer(customer, businessId)).thenReturn(Completable.complete())

        val testObserver = coreSdk.updateCustomer(
            customerId = "cust_id",
            desc = "John",
            address = null,
            profileImage = null,
            mobile = null,
            lang = "en",
            reminderMode = "sms",
            txnAlertEnabled = true,
            isForTxnEnable = false,
            dueInfoActiveDate = null,
            updateDueCustomDate = false,
            deleteDueCustomDate = false,
            addTransactionPermission = false,
            updateAddTransactionRestricted = false,
            blockTransaction = 1,
            updateBlockTransaction = false,
            businessId = "business-id"
        ).test()

        verify(mockRemoteSource).updateCustomer(
            customerId = "cust_id",
            desc = "John",
            address = null,
            profileImage = null,
            mobile = null,
            lang = "en",
            reminderMode = "sms",
            txnAlertEnabled = true,
            isForTxnEnable = false,
            dueInfoActiveDate = null,
            updateDueCustomDate = false,
            deleteDueCustomDate = false,
            addTransactionPermission = false,
            updateAddTransactionRestricted = false,
            blockTransaction = 1,
            updateBlockTransaction = false,
            businessId = "business-id"
        )
        verify(mockLocalSource).putCustomer(customer, businessId)
        testObserver.assertComplete()
    }

    @Test
    fun `getCustomer() should return customer`() {
        val customer: Customer = mock()
        whenever(mockLocalSource.getCustomer("cust_id")).thenReturn(Observable.just(customer))

        val testObserver = coreSdk.getCustomer("cust_id").test()

        testObserver.assertValue(customer)
    }

    @Test
    fun `getCustomerByMobile() should return customer by mobile`() {
        val customer: Customer = mock()
        val businessId = "business-id"
        whenever(mockLocalSource.getCustomerByMobile("0987654321", businessId)).thenReturn(Single.just(customer))

        val testObserver = coreSdk.getCustomerByMobile("0987654321", businessId).test()

        testObserver.assertValue(customer)
    }

    @Test
    fun `listCustomers() should return list of customer`() {
        val customerList: List<Customer> = listOf(mock(), mock(), mock())
        whenever(mockLocalSource.listCustomers(TestData.BUSINESS_ID)).thenReturn(Observable.just(customerList))

        val testObserver = coreSdk.listCustomers(TestData.BUSINESS_ID).test()

        testObserver.assertValue(customerList)
    }

    @Test
    fun `listCustomersByLastPayment() should return list of customer by last payment`() {
        val customerList: List<Customer> = listOf(mock(), mock(), mock())
        whenever(mockLocalSource.listCustomersByLastPayment(TestData.BUSINESS_ID)).thenReturn(
            Observable.just(
                customerList
            )
        )

        val testObserver = coreSdk.listCustomersByLastPayment(TestData.BUSINESS_ID).test()

        testObserver.assertValue(customerList)
    }

    @Test
    fun `listActiveCustomers() should return list of active customer`() {
        val customerList: List<Customer> = listOf(mock(), mock(), mock())
        whenever(mockLocalSource.listActiveCustomers(TestData.BUSINESS_ID)).thenReturn(Observable.just(customerList))

        val testObserver = coreSdk.listActiveCustomers(TestData.BUSINESS_ID).test()

        testObserver.assertValue(customerList)
    }

    @Test
    fun `getCustomerCount() should return customer count`() {
        val businessId = "business-id"
        whenever(mockLocalSource.getCustomerCount(businessId)).thenReturn(Observable.just(5))

        val testObserver = coreSdk.getCustomerCount(businessId).test()

        testObserver.assertValue(5)
    }

    @Test
    fun `getActiveCustomerCount() should return customer count`() {
        whenever(mockLocalSource.getActiveCustomerCount(TestData.BUSINESS_ID)).thenReturn(Observable.just(5))

        val testObserver = coreSdk.getActiveCustomerCount(TestData.BUSINESS_ID).test()

        testObserver.assertValue(5)
        verify(mockLocalSource).getActiveCustomerCount(TestData.BUSINESS_ID)
    }

    @Test
    fun `markActivityAsSeen() should mark activity as seen`() {
        whenever(mockLocalSource.markActivityAsSeen("cust_id")).thenReturn(Completable.complete())

        val testObserver = coreSdk.markActivityAsSeen("cust_id").test()

        testObserver.assertComplete()
    }

    @Test
    fun `syncCustomers() when schedule is true should schedule customers sync`() {
        val businessId = "business-id"
        whenever(mockSyncCustomers.schedule(businessId)).thenReturn(Completable.complete())

        val testObserver = coreSdk.scheduleSyncCustomers(businessId).test()

        verify(mockSyncCustomers).schedule(businessId)
        testObserver.assertComplete()
    }

    @Test
    fun `syncCustomers() when schedule is false should execute customers sync`() {
        whenever(mockSyncCustomers.execute()).thenReturn(Completable.complete())

        val testObserver = coreSdk.syncCustomers().test()

        verify(mockSyncCustomers).execute()
        testObserver.assertComplete()
    }

    @Test
    fun `scheduleSyncCustomer() should schedule customer sync`() {
        val businessId = "business-id"
        whenever(mockSyncCustomer.schedule("cust_id", businessId)).thenReturn(Completable.complete())

        val testObserver = coreSdk.scheduleSyncCustomer("cust_id", businessId).test()

        verify(mockSyncCustomer).schedule("cust_id", businessId)
        testObserver.assertComplete()
    }

    @Test
    fun `syncCustomer() when schedule is false should execute customer sync`() {
        whenever(mockSyncCustomer.execute("cust_id")).thenReturn(Completable.complete())

        val testObserver = coreSdk.syncCustomer("cust_id").test()

        verify(mockSyncCustomer).execute("cust_id")
        testObserver.assertComplete()
    }

    @Test
    fun `reactivateCustomer() given invalid name should throw exception`() {
        val invalidName = "loremipsumloremipsumloremipsumloremipsum"
        val businessId = "business-id"
        val customer: Customer = mock()
        whenever(mockLocalSource.putCustomer(customer, businessId)).thenReturn(Completable.complete())
        whenever(mockLocalSource.getCustomer("cust_id")).thenReturn(Observable.just(customer))

        val testObserver = coreSdk.reactivateCustomer(invalidName, "cust_id", null, businessId).test()

        testObserver.assertError(CoreException.InvalidName)
    }

    @Test
    fun `reactivateCustomer() given valid name should make server call and save customer to database`() {
        val name = "John Doe"
        val businessId = "business-id"
        val customer: Customer = mock()
        whenever(mockLocalSource.putCustomer(customer, businessId)).thenReturn(Completable.complete())
        whenever(mockLocalSource.getCustomer("cust_id")).thenReturn(Observable.just(customer))
        whenever(
            mockRemoteSource.addCustomer(
                name,
                null,
                true,
                null,
                null,
                businessId
            )
        ).thenReturn(Single.just(customer))

        val testObserver = coreSdk.reactivateCustomer(name, "cust_id", null, businessId).test()

        testObserver.assertValue(customer)
    }

    @Test
    fun `deleteLocalCustomer() should delete customer in database`() {
        whenever(mockLocalSource.deleteCustomer("cust_id")).thenReturn(Completable.complete())

        val testObserver = coreSdk.deleteLocalCustomer("cust_id").test()

        verify(mockLocalSource).deleteCustomer("cust_id")
        testObserver.assertComplete()
    }

    @Test
    fun `getFirstTransaction() should get first transaction from database`() {
        val transaction = mock<Transaction>()
        val businessId = "business-id"
        whenever(mockLocalSource.getFirstTransaction(businessId)).thenReturn(Single.just(transaction))

        val testObserver = coreSdk.getFirstTransaction(businessId).test()

        verify(mockLocalSource).getFirstTransaction(businessId)
        testObserver.assertValue(transaction)
    }

    @Test
    fun `getLastTransaction() should get last transaction from database`() {
        val transaction = mock<Transaction>()
        val businessId = "business-id"
        whenever(mockLocalSource.getLastTransaction(businessId)).thenReturn(Single.just(transaction))

        val testObserver = coreSdk.getLastTransaction(businessId).test()

        verify(mockLocalSource).getLastTransaction(businessId)
        testObserver.assertValue(transaction)
    }

    @Test
    fun `getLatestTransactionAddedByCustomer() should get last transaction from database`() {
        val transaction = mock<Transaction>()
        val customerId = "customer-id"
        whenever(mockLocalSource.getLatestTransactionAddedByCustomer(customerId, TestData.BUSINESS_ID)).thenReturn(
            Single.just(transaction)
        )

        val testObserver = coreSdk.getLatestTransactionAddedByCustomer(customerId, TestData.BUSINESS_ID).test()

        verify(mockLocalSource).getLatestTransactionAddedByCustomer(customerId, TestData.BUSINESS_ID)
        testObserver.assertValue(transaction)
    }

    @Test
    fun `core_updateCustomerAddTransactionPermission() should update permission in store`() {
        val accountId = "account-id"
        val isDenied = false
        whenever(
            mockLocalSource.updateCustomerAddTransactionPermission(
                accountId,
                isDenied
            )
        ).thenReturn(Completable.complete())

        val testObserver = coreSdk.coreUpdateCustomerAddTransactionPermission(accountId, isDenied).test()

        verify(mockLocalSource).updateCustomerAddTransactionPermission(accountId, isDenied)
        testObserver.assertComplete()
    }

    @Test
    fun `updateLocalCustomerDescription() should update customer description in database`() {
        whenever(mockLocalSource.updateCustomerDescription("John", "cust_id")).thenReturn(Completable.complete())

        val testObserver = coreSdk.updateLocalCustomerDescription("John", "cust_id").test()

        verify(mockLocalSource).updateCustomerDescription("John", "cust_id")
        testObserver.assertComplete()
    }

    @Test
    fun `putCustomer() should put customer`() {
        val businessId = "business-id"
        whenever(mockLocalSource.putCustomer(any(), eq(businessId))).thenReturn(Completable.complete())

        val testObserver = coreSdk.putCustomer(mock(), businessId).test()

        testObserver.assertComplete()
    }

    @Test
    fun `putTransaction() should put transaction`() {
        val businessId = "business-id"
        whenever(mockLocalSource.putTransaction(any(), eq(businessId))).thenReturn(Completable.complete())

        val testObserver = coreSdk.putTransaction(mock(), businessId).test()

        testObserver.assertComplete()
    }

    @Test
    fun `process() given update transaction amount command when transaction present should return transaction`() {
        val command: Command.UpdateTransactionAmount = mock()
        val transaction: Transaction = mock()
        val businessId = "business-id"
        whenever(command.transactionId).thenReturn("txn_id")
        whenever(command.commandType).thenReturn(Command.CommandType.UPDATE_TRANSACTION_AMOUNT)
        whenever(mockLocalSource.getTransaction("txn_id")).thenReturn(Observable.just(transaction))
        whenever(mockLocalSource.isTransactionPresent("txn_id")).thenReturn(Single.just(true))
        whenever(mockLocalSource.updateTransactionAmount(command, businessId)).thenReturn(Completable.complete())
        whenever(
            mockSyncTransactions.schedule(
                Command.CommandType.UPDATE_TRANSACTION_AMOUNT.name,
                businessId
            )
        ).thenReturn(
            Completable.complete()
        )
        doNothing().whenever(mockTracker).trackDebug(any(), any())

        val testObserver = coreSdk.processTransactionCommand(command, businessId).test()

        verify(mockTracker, times(0)).trackDebug(eq(CoreTracker.DebugType.TRANSACTION_NOT_FOUND), any())
        verify(mockLocalSource).updateTransactionAmount(command, businessId)
        verify(mockSyncTransactions).schedule(Command.CommandType.UPDATE_TRANSACTION_AMOUNT.name, businessId)
        testObserver.assertValue(transaction)
    }

    @Test
    fun `process() given update transaction amount command when transaction not present should call mockTracker and throw exception`() {
        val command: Command.UpdateTransactionAmount = mock()
        val transaction: Transaction = mock()
        val businessId = "business-id"
        whenever(command.transactionId).thenReturn("txn_id")
        whenever(command.commandType).thenReturn(Command.CommandType.UPDATE_TRANSACTION_AMOUNT)
        whenever(mockLocalSource.getTransaction("txn_id")).thenReturn(Observable.just(transaction))
        whenever(mockLocalSource.isTransactionPresent("txn_id")).thenReturn(Single.just(false))
        whenever(mockLocalSource.updateTransactionAmount(command, businessId)).thenReturn(Completable.complete())
        whenever(
            mockSyncTransactions.schedule(
                Command.CommandType.UPDATE_TRANSACTION_AMOUNT.name,
                businessId
            )
        ).thenReturn(
            Completable.complete()
        )
        doNothing().whenever(mockTracker).trackDebug(any(), any())

        val testObserver = coreSdk.processTransactionCommand(command, businessId).test()

        verify(mockTracker).trackDebug(eq(CoreTracker.DebugType.TRANSACTION_NOT_FOUND), any())
        verify(mockLocalSource, times(0)).updateTransactionAmount(command, businessId)
        testObserver.assertError(CoreException.TransactionNotFoundException)
    }

    @Test
    fun `getTxnAmountHistory() should return transaction amount history`() {
        val transactionAmountHistory: TransactionAmountHistory = mock()
        val businessId = "business-id"
        whenever(mockRemoteSource.getTxnAmountHistory("transaction_id", businessId))
            .thenReturn(Single.just(transactionAmountHistory))

        val testObserver = coreSdk.getTxnAmountHistory("transaction_id", businessId).test()

        testObserver.assertValue(transactionAmountHistory)
    }

    @Test
    fun `getTransactionsIdsByCreatedTime() should return transaction id list`() {
        val startTime = 1234567890L
        val endTime = 9876543210L
        val businessId = "business-id"
        val transactionIdList = mock<List<String>>()
        whenever(
            mockLocalSource.getTransactionsIdsByCreatedTime(
                startTime.toTimestamp(),
                endTime.toTimestamp(),
                businessId
            )
        ).thenReturn(
            Single.just(transactionIdList)
        )

        val testObserver = coreSdk.getTransactionsIdsByCreatedTime(startTime, endTime, businessId).test()

        testObserver.assertValue(transactionIdList)
    }

    @Test
    fun `listActiveTransactionsBetweenBillDate() should return list of transaction between start and end time for given customerId`() {
        val transactionList: List<Transaction> = listOf(mock(), mock(), mock())
        val startTimestamp = 1590578649L
        val endTimestamp = 1590578649L
        val customerTxnStartTime = 1590578649L
        val customerId = "ans-nsnnd-smms-mdmd"
        whenever(
            mockLocalSource.listActiveTransactionsBetweenBillDate(
                customerId,
                customerTxnStartTime.toTimestamp(),
                startTimestamp.toTimestamp(),
                endTimestamp.toTimestamp(),
                TestData.BUSINESS_ID
            )
        ).thenReturn(Observable.just(transactionList))

        val testObserver =
            coreSdk.listActiveTransactionsBetweenBillDate(
                customerId,
                customerTxnStartTime,
                startTimestamp,
                endTimestamp,
                TestData.BUSINESS_ID
            ).test()

        testObserver.assertValue(transactionList)
    }

    @Test
    fun `bulkSearchTransactions() should return transaction id list`() {
        val actionId = "action_id"
        val businessId = "business-id"
        val requestTransactionIdList = mock<List<String>>()
        val responseTransactionIdList = mock<List<String>>()
        val response = mock<BulkSearchTransactionsResponse>().apply {
            whenever(missingTransactionIds).thenReturn(responseTransactionIdList)
        }
        whenever(mockRemoteSource.bulkSearchTransactions(actionId, requestTransactionIdList, businessId)).thenReturn(
            Single.just(
                response
            )
        )

        val testObserver = coreSdk.bulkSearchTransactions(actionId, requestTransactionIdList, businessId).test()

        testObserver.assertValue(responseTransactionIdList)
    }

    @Test
    fun `getSuggestionsFromStore() should get suggestions from store`() {
        val list = mock<List<String>>()
        val businessId = "business-id"
        whenever(mockLocalSource.getSuggestedCustomerIdsForAddTransaction(businessId)).thenReturn(Single.just(list))

        val testObserver = suggestedCustomerIdsForAddTransaction.getSuggestionsFromStore(businessId).test()

        verify(mockLocalSource).getSuggestedCustomerIdsForAddTransaction(businessId)
        testObserver.assertValue(list)
    }

    @Test
    fun `getSuggestionsFromServer() should get suggestions from server`() {
        val list = mock<List<String>>()
        val businessId = "business-id"
        whenever(mockRemoteSource.getSuggestedCustomerIdsForAddTransaction(businessId)).thenReturn(Single.just(list))

        val testObserver = suggestedCustomerIdsForAddTransaction.getSuggestionsFromServer(businessId).test()

        verify(mockRemoteSource).getSuggestedCustomerIdsForAddTransaction(businessId)
        testObserver.assertValue(list)
    }

    @Test
    fun `replaceSuggestedCustomerIdsForAddTransaction() should replace suggested customer ids`() {
        val list = mock<List<String>>()
        val businessId = "business-id"
        whenever(
            mockLocalSource.replaceSuggestedCustomerIdsForAddTransaction(
                list,
                businessId
            )
        ).thenReturn(Completable.complete())

        val testObserver =
            suggestedCustomerIdsForAddTransaction.replaceSuggestedCustomerIdsForAddTransaction(list, businessId).test()

        verify(mockLocalSource).replaceSuggestedCustomerIdsForAddTransaction(list, businessId)
        testObserver.assertComplete()
    }

    @Test
    fun `getDefaultersDataForHomeBanner should call the local source method getDefaultersDataForHomeBanner`() {
        runBlocking {
            val businessId = "businessId"

            // given
            val fakeBulkReminderDbInfo = BulkReminderDbInfo(
                totalBalanceDue = -300,
                countNumberOfCustomers = 0
            )
            val fakeDefaulterSince = "4"
            val argumentCaptor = argumentCaptor<String>()

            // when
            whenever(mockLocalSource.getDefaultersDataForBanner(argumentCaptor.capture(), eq(businessId))).thenReturn(
                flowOf(fakeBulkReminderDbInfo)
            )

            // then
            val result = coreSdkImpl.getDefaultersDataForBanner(fakeDefaulterSince, businessId).first()

            verify(mockLocalSource).getDefaultersDataForBanner(argumentCaptor.capture(), eq(businessId))
            assert(argumentCaptor.firstValue == fakeDefaulterSince)
            assert(result.totalBalanceDue == fakeBulkReminderDbInfo.totalBalanceDue)
            assert(result.countNumberOfCustomers == fakeBulkReminderDbInfo.countNumberOfCustomers)
        }
    }

    @Test
    fun `isCoreSdkEnabledSus should call the mockLocalSource preference isCoreSdkEnabledSus`() {
        runBlocking {
            val businessId = "businessId"
            // when
            whenever(
                mockRxPref.getBoolean(eq(PREF_BUSINESS_CORE_SDK_ENABLED), any(), anyOrNull())
            ).thenReturn(flowOf(true))

            val result = coreSdk.isCoreSdkFeatureEnabledFlow(businessId).first()

            assert(result)
        }
    }
}
