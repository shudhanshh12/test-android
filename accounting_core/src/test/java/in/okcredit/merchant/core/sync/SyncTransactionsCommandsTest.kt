package `in`.okcredit.merchant.core.sync

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.Command
import `in`.okcredit.merchant.core.Command.*
import `in`.okcredit.merchant.core.analytics.CoreTracker
import `in`.okcredit.merchant.core.common.TimestampUtils
import `in`.okcredit.merchant.core.model.Transaction
import `in`.okcredit.merchant.core.server.CoreRemoteSource
import `in`.okcredit.merchant.core.server.internal.CoreApiMessages.*
import `in`.okcredit.merchant.core.store.CoreLocalSource
import `in`.okcredit.merchant.core.sync.SyncTransactionsCommands.Companion.IMAGE_ID_EXISTS_ERROR_DESCRIPTION
import `in`.okcredit.merchant.core.sync.SyncTransactionsCommands.Companion.MAX_COUNT_PER_REQUEST_FOR_SYNC_TXN_KEY
import `in`.okcredit.merchant.core.sync.SyncTransactionsCommands.Companion.MAX_RETRY_COUNT_OF_SYNC_TRANSACTION_KEY
import `in`.okcredit.merchant.core.sync.SyncTransactionsCommands.Companion.TRANSACTION_ID_EXISTS_ERROR_DESCRIPTION
import com.google.common.truth.Truth.assertThat
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.*
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.workmanager.OkcWorkManager
import `in`.okcredit.merchant.core.model.TransactionImage as TransactionImageModel
import tech.okcredit.android.base.error.Error as BaseError

class SyncTransactionsCommandsTest {

    private val workManager: OkcWorkManager = mock()
    private val localSource: CoreLocalSource = mock()
    private val remoteSource: CoreRemoteSource = mock()
    private val tracker: CoreTracker = mock()
    private val firebaseRemoteConfig: FirebaseRemoteConfig = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val syncTransactionsCommands: SyncTransactionsCommands =
        SyncTransactionsCommands(
            { workManager },
            { localSource },
            { remoteSource },
            { tracker },
            { firebaseRemoteConfig },
            { getActiveBusinessId },
        )

    private lateinit var recordExceptionSlot: CapturingSlot<Exception>

    @Before
    fun setup() {
        mockkStatic(RecordException::class)
        recordExceptionSlot = slot<Exception>()
        every { RecordException.recordException(capture(recordExceptionSlot)) } returns Unit
    }

    @Test
    fun `execute() given commands without transaction id conflict should call tracker with steps and execute successfully`() {
        val commands: List<Command> = listOf(
            CreateTransaction("cust_id", "txn_id", Transaction.Type.CREDIT, 1200),
            UpdateTransactionNote("txn_id2", "Noted"),
            DeleteTransaction("txn_id3")
        )
        val businessId = "business-id"
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
        whenever(
            localSource.listTransactionsCommandsForCleanCustomers(
                any(),
                eq(businessId)
            )
        ).thenReturn(Observable.just(commands))
            .thenReturn(Observable.just(listOf()))
        whenever(firebaseRemoteConfig.getLong(MAX_COUNT_PER_REQUEST_FOR_SYNC_TXN_KEY)).thenReturn(5)
        val operationResponses = commands.map { OperationResponseForTransactions(it.id, Status.SUCCESS.value, null) }
        val response = PushTransactionsCommandsResponse(operationResponses)
        whenever(remoteSource.pushTransactionCommands(any(), eq(businessId))).thenReturn(Single.just(response))
        whenever(localSource.listTransactionsCommandsForCleanCustomers(businessId)).thenReturn(Observable.just(commands))
        whenever(localSource.markTransactionDirty(any(), any())).thenReturn(Completable.complete())
        whenever(localSource.deleteCommands(any())).thenReturn(Completable.complete())

        val testObserver = syncTransactionsCommands.execute(businessId).test()

        testObserver.assertComplete()
        verify(localSource, times(1)).listTransactionsCommandsForCleanCustomers(eq(businessId))
        verify(localSource, times(2)).listTransactionsCommandsForCleanCustomers(any(), eq(businessId))
        verify(localSource, times(1)).deleteCommands(any())
        verify(remoteSource, times(1)).pushTransactionCommands(any(), eq(businessId))
        verify(tracker, times(1)).trackSyncTransactionCommands(
            eq("Sent_Server_Call"),
            any(),
            any(),
            any(),
            any(),
            anyOrNull(),
            anyOrNull()
        )
        verify(tracker, times(1)).trackSyncTransactionCommands(
            eq("Completed_Server_Call"),
            any(),
            any(),
            any(),
            any(),
            anyOrNull(),
            anyOrNull()
        )
        verify(tracker, times(1)).trackSyncTransactionCommands(
            eq("Saved_To_Database"),
            any(),
            any(),
            any(),
            any(),
            anyOrNull(),
            anyOrNull()
        )
        verify(tracker, times(1)).trackSyncTransactionCommands(
            eq("Completed_Sync"),
            any(),
            any(),
            any(),
            any(),
            anyOrNull(),
            anyOrNull()
        )
    }

    @Test
    fun `execute() given commands without transaction id conflict when server error should call tracker`() {
        val commands: List<Command> = listOf(
            CreateTransaction("cust_id", "txn_id", Transaction.Type.CREDIT, 1200),
            UpdateTransactionNote("txn_id2", "Noted"),
            DeleteTransaction("txn_id3")
        )
        val businessId = "business-id"
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
        whenever(firebaseRemoteConfig.getLong(MAX_COUNT_PER_REQUEST_FOR_SYNC_TXN_KEY)).thenReturn(5)
        whenever(
            localSource.listTransactionsCommandsForCleanCustomers(
                any(),
                eq(businessId)
            )
        ).thenReturn(Observable.just(commands))
        whenever(remoteSource.pushTransactionCommands(any(), eq(businessId))).thenReturn(
            Single.error(
                BaseError(
                    404,
                    null
                )
            )
        )
        whenever(localSource.listTransactionsCommandsForCleanCustomers(businessId)).thenReturn(Observable.just(commands))
        whenever(localSource.markTransactionDirty(any(), any())).thenReturn(Completable.complete())
        whenever(localSource.deleteCommands(any())).thenReturn(Completable.complete())

        val testObserver = syncTransactionsCommands.execute(businessId).test()

        testObserver.assertError(BaseError::class.java)
        verify(tracker, times(1)).trackSyncTransactionCommandsError(
            eq("Server error"),
            any(),
            any(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull()
        )
    }

    @Test
    fun `execute() given commands with transaction id conflict should handle conflict and execute successfully`() {
        val commands: List<Command> = listOf(
            CreateTransaction("cust_id", "txn_id", Transaction.Type.CREDIT, 1200),
            UpdateTransactionNote("txn_id2", "Noted"),
            DeleteTransaction("txn_id3")
        )
        val businessId = "business-id"
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
        whenever(
            localSource.listTransactionsCommandsForCleanCustomers(
                any(),
                eq(businessId)
            )
        ).thenReturn(Observable.just(commands))
            .thenReturn(Observable.just(commands.subList(0, 1))).thenReturn(Observable.just(listOf()))
        whenever(firebaseRemoteConfig.getLong(MAX_COUNT_PER_REQUEST_FOR_SYNC_TXN_KEY)).thenReturn(5)
        val operationResponses = commands.map {
            if (it is CreateTransaction)
                OperationResponseForTransactions(
                    it.id,
                    Status.FAILURE.value,
                    Error(409, TRANSACTION_ID_EXISTS_ERROR_DESCRIPTION)
                )
            else OperationResponseForTransactions(it.id, Status.SUCCESS.value, null)
        }
        val response1 = PushTransactionsCommandsResponse(operationResponses)
        val response2 =
            PushTransactionsCommandsResponse(
                listOf(
                    OperationResponseForTransactions(
                        operationResponses[0].id,
                        Status.SUCCESS.value,
                        null
                    )
                )
            )
        whenever(remoteSource.pushTransactionCommands(any(), eq(businessId))).thenReturn(Single.just(response1))
            .thenReturn(Single.just(response2))
        whenever(localSource.listTransactionsCommandsForCleanCustomers(businessId)).thenReturn(Observable.just(commands))
        whenever(localSource.markTransactionDirty(any(), any())).thenReturn(Completable.complete())
        whenever(localSource.deleteCommands(any())).thenReturn(Completable.complete())
        whenever(localSource.replaceTransactionId(any(), any())).thenReturn(Completable.complete())

        val testObserver = syncTransactionsCommands.execute(businessId).test()

        testObserver.assertComplete()
        verify(remoteSource, times(2)).pushTransactionCommands(any(), eq(businessId))
        verify(tracker, times(1)).trackDebug(eq(CoreTracker.DebugType.SERVER_CONFLICT), anyOrNull())
    }

    @Test
    fun `execute() given commands with image id conflict in CreateTransaction should handle conflict and execute successfully`() {
        val commands: List<Command> = listOf(
            CreateTransaction("cust_id", "txn_id", Transaction.Type.CREDIT, 1200),
            UpdateTransactionNote("txn_id2", "Noted"),
            DeleteTransaction("txn_id3")
        )
        val businessId = "business-id"
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
        whenever(
            localSource.listTransactionsCommandsForCleanCustomers(
                any(),
                eq(businessId)
            )
        ).thenReturn(Observable.just(commands))
            .thenReturn(Observable.just(commands.subList(0, 1))).thenReturn(Observable.just(listOf()))
        whenever(firebaseRemoteConfig.getLong(MAX_COUNT_PER_REQUEST_FOR_SYNC_TXN_KEY)).thenReturn(5)
        val operationResponses = commands.map {
            if (it is CreateTransaction)
                OperationResponseForTransactions(
                    it.id,
                    Status.FAILURE.value,
                    Error(409, IMAGE_ID_EXISTS_ERROR_DESCRIPTION)
                )
            else OperationResponseForTransactions(it.id, Status.SUCCESS.value, null)
        }
        val response1 = PushTransactionsCommandsResponse(operationResponses)
        val response2 =
            PushTransactionsCommandsResponse(
                listOf(
                    OperationResponseForTransactions(
                        operationResponses[0].id,
                        Status.SUCCESS.value,
                        null
                    )
                )
            )
        whenever(remoteSource.pushTransactionCommands(any(), eq(businessId))).thenReturn(Single.just(response1))
            .thenReturn(Single.just(response2))
        whenever(localSource.listTransactionsCommandsForCleanCustomers(businessId)).thenReturn(Observable.just(commands))
        whenever(localSource.markTransactionDirty(any(), any())).thenReturn(Completable.complete())
        whenever(localSource.deleteCommands(any())).thenReturn(Completable.complete())
        val transactionList = listOf<`in`.okcredit.merchant.core.store.database.Transaction>()
        val commandList = listOf<`in`.okcredit.merchant.core.store.database.Command>()
        val imageId = "image_id"
        val image =
            TransactionImageModel(imageId, commands[0].transactionId, "sample_url", TimestampUtils.currentTimestamp())
        commands[0].transactionImages = listOf(image)
        whenever(localSource.getDbTransactionsWithImageId(imageId, businessId)).thenReturn(Single.just(transactionList))
        whenever(localSource.getDbCommandsWithImageId(imageId, businessId)).thenReturn(Single.just(commandList))
        whenever(localSource.updateTransactionImagesAndCommandValues(any(), any())).thenReturn(Completable.complete())

        val testObserver = syncTransactionsCommands.execute(businessId).test()

        testObserver.assertComplete()
        verify(remoteSource, times(2)).pushTransactionCommands(any(), eq(businessId))
        verify(tracker, times(1)).trackDebug(eq(CoreTracker.DebugType.SERVER_CONFLICT), anyOrNull())
        verify(localSource, times(1)).getDbTransactionsWithImageId(imageId, businessId)
        verify(localSource, times(1)).getDbCommandsWithImageId(imageId, businessId)
        verify(localSource, times(1)).updateTransactionImagesAndCommandValues(any(), any())
    }

    @Test
    fun `execute() given commands with image id conflict in CreateTransactionImage should handle conflict and execute successfully`() {
        val imageId = "image_id"
        val sampleUrl = "sample_url"
        val commands: List<Command> = listOf(
            CreateTransaction("cust_id", "txn_id", Transaction.Type.CREDIT, 1200),
            CreateTransactionImage("txn_id2", imageId, sampleUrl),
            DeleteTransaction("txn_id3")
        )
        val businessId = "business-id"
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
        whenever(
            localSource.listTransactionsCommandsForCleanCustomers(
                any(),
                eq(businessId)
            )
        ).thenReturn(Observable.just(commands))
            .thenReturn(Observable.just(commands.subList(1, 2))).thenReturn(Observable.just(listOf()))
        whenever(firebaseRemoteConfig.getLong(MAX_COUNT_PER_REQUEST_FOR_SYNC_TXN_KEY)).thenReturn(5)
        val operationResponses = commands.map {
            if (it is CreateTransactionImage)
                OperationResponseForTransactions(
                    it.id,
                    Status.FAILURE.value,
                    Error(409, IMAGE_ID_EXISTS_ERROR_DESCRIPTION)
                )
            else OperationResponseForTransactions(it.id, Status.SUCCESS.value, null)
        }
        val response1 = PushTransactionsCommandsResponse(operationResponses)
        val response2 =
            PushTransactionsCommandsResponse(
                listOf(
                    OperationResponseForTransactions(
                        operationResponses[0].id,
                        Status.SUCCESS.value,
                        null
                    )
                )
            )
        whenever(remoteSource.pushTransactionCommands(any(), eq(businessId))).thenReturn(Single.just(response1))
            .thenReturn(Single.just(response2))
        whenever(localSource.listTransactionsCommandsForCleanCustomers(businessId)).thenReturn(Observable.just(commands))
        whenever(localSource.markTransactionDirty(any(), any())).thenReturn(Completable.complete())
        whenever(localSource.deleteCommands(any())).thenReturn(Completable.complete())
        val transactionList = listOf<`in`.okcredit.merchant.core.store.database.Transaction>()
        val commandList = listOf<`in`.okcredit.merchant.core.store.database.Command>()
        val image =
            TransactionImageModel(imageId, commands[0].transactionId, sampleUrl, TimestampUtils.currentTimestamp())
        commands[0].transactionImages = listOf(image)
        whenever(localSource.getDbTransactionsWithImageId(imageId, businessId)).thenReturn(Single.just(transactionList))
        whenever(localSource.getDbCommandsWithImageId(imageId, businessId)).thenReturn(Single.just(commandList))
        whenever(localSource.updateTransactionImagesAndCommandValues(any(), any())).thenReturn(Completable.complete())

        val testObserver = syncTransactionsCommands.execute(businessId).test()

        testObserver.assertComplete()
        verify(remoteSource, times(2)).pushTransactionCommands(any(), eq(businessId))
        verify(tracker, times(1)).trackDebug(eq(CoreTracker.DebugType.SERVER_CONFLICT), anyOrNull())
        verify(localSource, times(1)).getDbTransactionsWithImageId(imageId, businessId)
        verify(localSource, times(1)).getDbCommandsWithImageId(imageId, businessId)
        verify(localSource, times(1)).updateTransactionImagesAndCommandValues(any(), any())
    }

    @Test
    fun `execute() given commands with image id conflict in DeleteTransaction should track unhandled conflict and execute successfully`() {
        val imageId = "image_id"
        val sampleUrl = "sample_url"
        val commands: List<Command> = listOf(
            CreateTransaction("cust_id", "txn_id", Transaction.Type.CREDIT, 1200),
            CreateTransactionImage("txn_id2", imageId, sampleUrl),
            DeleteTransaction("txn_id3")
        )
        val businessId = "business-id"
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
        whenever(
            localSource.listTransactionsCommandsForCleanCustomers(
                any(),
                eq(businessId)
            )
        ).thenReturn(Observable.just(commands))
            .thenReturn(Observable.just(commands.subList(2, 3))).thenReturn(Observable.just(listOf()))
        whenever(firebaseRemoteConfig.getLong(MAX_COUNT_PER_REQUEST_FOR_SYNC_TXN_KEY)).thenReturn(5)
        val operationResponses = commands.map {
            if (it is DeleteTransaction)
                OperationResponseForTransactions(
                    it.id,
                    Status.FAILURE.value,
                    Error(409, IMAGE_ID_EXISTS_ERROR_DESCRIPTION)
                )
            else OperationResponseForTransactions(it.id, Status.SUCCESS.value, null)
        }
        val response1 = PushTransactionsCommandsResponse(operationResponses)
        val response2 =
            PushTransactionsCommandsResponse(
                listOf(
                    OperationResponseForTransactions(
                        operationResponses[0].id,
                        Status.SUCCESS.value,
                        null
                    )
                )
            )
        whenever(remoteSource.pushTransactionCommands(any(), eq(businessId))).thenReturn(Single.just(response1))
            .thenReturn(Single.just(response2))
        whenever(localSource.listTransactionsCommandsForCleanCustomers(eq(businessId))).thenReturn(
            Observable.just(
                commands
            )
        )
        whenever(localSource.markTransactionDirty(any(), any())).thenReturn(Completable.complete())
        whenever(localSource.deleteCommands(any())).thenReturn(Completable.complete())

        val testObserver = syncTransactionsCommands.execute(businessId).test()

        testObserver.assertComplete()
        verify(remoteSource, times(2)).pushTransactionCommands(any(), eq(businessId))

        io.mockk.verify(exactly = 1) { RecordException.recordException(recordExceptionSlot.captured) }
        assertThat(recordExceptionSlot.captured).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(recordExceptionSlot.captured.message).isEqualTo("Image Id Conflict for ${commands.last()}")

        verify(tracker, times(0)).trackDebug(eq(CoreTracker.DebugType.SERVER_CONFLICT), anyOrNull())
        verify(localSource, times(0)).getDbTransactionsWithImageId(any(), eq(businessId))
        verify(localSource, times(0)).getDbCommandsWithImageId(any(), eq(businessId))
        verify(localSource, times(0)).updateTransactionImagesAndCommandValues(any(), any())
    }

    @Test
    fun `execute() given commands with unhandled conflict should call tracker and execute successfully`() {
        val commands: List<Command> = listOf(
            CreateTransaction("cust_id", "txn_id", Transaction.Type.CREDIT, 1200),
            UpdateTransactionNote("txn_id2", "Noted"),
            DeleteTransaction("txn_id3")
        )
        val businessId = "business-id"
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
        whenever(
            localSource.listTransactionsCommandsForCleanCustomers(
                any(),
                eq(businessId)
            )
        ).thenReturn(Observable.just(commands))
            .thenReturn(Observable.just(commands.subList(0, 1))).thenReturn(Observable.just(listOf()))
        whenever(firebaseRemoteConfig.getLong(MAX_COUNT_PER_REQUEST_FOR_SYNC_TXN_KEY)).thenReturn(5)
        val operationResponses = commands.map {
            if (it is CreateTransaction)
                OperationResponseForTransactions(it.id, Status.FAILURE.value, Error(409, "unknown"))
            else OperationResponseForTransactions(it.id, Status.SUCCESS.value, null)
        }
        val response1 = PushTransactionsCommandsResponse(operationResponses)
        val response2 =
            PushTransactionsCommandsResponse(
                listOf(
                    OperationResponseForTransactions(
                        operationResponses[0].id,
                        Status.SUCCESS.value,
                        null
                    )
                )
            )
        whenever(remoteSource.pushTransactionCommands(any(), eq(businessId))).thenReturn(Single.just(response1))
            .thenReturn(Single.just(response2))
        whenever(localSource.listTransactionsCommandsForCleanCustomers(businessId)).thenReturn(Observable.just(commands))
        whenever(localSource.markTransactionDirty(any(), any())).thenReturn(Completable.complete())
        whenever(localSource.deleteCommands(any())).thenReturn(Completable.complete())
        whenever(localSource.replaceTransactionId(any(), any())).thenReturn(Completable.complete())

        val testObserver = syncTransactionsCommands.execute(businessId).test()

        testObserver.assertComplete()
        verify(remoteSource, times(2)).pushTransactionCommands(any(), eq(businessId))
        verify(tracker, times(2)).trackDebug(
            eq(CoreTracker.DebugType.ERROR_CODE_CONFLICT_UNHANDLED_DESCRIPTION),
            anyOrNull()
        )
    }

    @Test
    fun `execute() given large number of commands should make multiple iterations and execute successfully`() {
        whenever(firebaseRemoteConfig.getLong(MAX_COUNT_PER_REQUEST_FOR_SYNC_TXN_KEY)).thenReturn(5)
        val maxCount = firebaseRemoteConfig.getLong(MAX_COUNT_PER_REQUEST_FOR_SYNC_TXN_KEY).toInt()
        val commands = mutableListOf<Command>()
        val businessId = "business-id"
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
        repeat(maxCount + 1) { commands.add(UpdateTransactionNote("txn_id2", "Noted")) }
        whenever(localSource.listTransactionsCommandsForCleanCustomers(any(), eq(businessId)))
            .thenReturn(Observable.just(commands))
            .thenReturn(Observable.just(listOf(commands.last())))
            .thenReturn(Observable.just(listOf()))
        val response1 = PushTransactionsCommandsResponse(
            commands.subList(0, maxCount)
                .map { OperationResponseForTransactions(it.id, Status.SUCCESS.value, null) }
        )
        val response2 = PushTransactionsCommandsResponse(
            listOf(
                OperationResponseForTransactions(
                    commands.last().id,
                    Status.SUCCESS.value,
                    null
                )
            )
        )
        whenever(remoteSource.pushTransactionCommands(any(), eq(businessId))).thenReturn(Single.just(response1))
            .thenReturn(Single.just(response2))
        whenever(localSource.listTransactionsCommandsForCleanCustomers(businessId)).thenReturn(Observable.just(commands))
        whenever(localSource.markTransactionDirty(any(), any())).thenReturn(Completable.complete())
        whenever(localSource.deleteCommands(any())).thenReturn(Completable.complete())
        whenever(localSource.replaceTransactionId(any(), any())).thenReturn(Completable.complete())

        val testObserver = syncTransactionsCommands.execute(businessId).test()

        testObserver.assertComplete()
        verify(remoteSource, times(2)).pushTransactionCommands(any(), eq(businessId))
    }

    @Test
    fun `execute() given commands when failure response once should retry and execute successfully`() {
        val commands: List<Command> = listOf(
            CreateTransaction("cust_id", "txn_id", Transaction.Type.CREDIT, 1200),
            UpdateTransactionNote("txn_id2", "Noted"),
            DeleteTransaction("txn_id3")
        )
        val businessId = "business-id"
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
        whenever(
            localSource.listTransactionsCommandsForCleanCustomers(
                any(),
                eq(businessId)
            )
        ).thenReturn(Observable.just(commands))
            .thenReturn(Observable.just(commands)).thenReturn(Observable.just(listOf()))
        whenever(firebaseRemoteConfig.getLong(MAX_RETRY_COUNT_OF_SYNC_TRANSACTION_KEY)).thenReturn(5)
        whenever(firebaseRemoteConfig.getLong(MAX_COUNT_PER_REQUEST_FOR_SYNC_TXN_KEY)).thenReturn(5)
        val operationResponses1 =
            commands.map {
                OperationResponseForTransactions(
                    it.id,
                    Status.FAILURE.value,
                    Error(500, "Internal server error")
                )
            }
        val operationResponses2 = commands.map { OperationResponseForTransactions(it.id, Status.SUCCESS.value, null) }
        val response1 = PushTransactionsCommandsResponse(operationResponses1)
        val response2 = PushTransactionsCommandsResponse(operationResponses2)
        whenever(remoteSource.pushTransactionCommands(any(), eq(businessId))).thenReturn(Single.just(response1))
            .thenReturn(Single.just(response2))
        whenever(localSource.listTransactionsCommandsForCleanCustomers(businessId)).thenReturn(Observable.just(commands))
        whenever(localSource.markTransactionDirty(any(), any())).thenReturn(Completable.complete())
        whenever(localSource.deleteCommands(any())).thenReturn(Completable.complete())
        whenever(localSource.replaceTransactionId(any(), any())).thenReturn(Completable.complete())

        val testObserver = syncTransactionsCommands.execute(businessId).test()

        testObserver.assertComplete()
        verify(remoteSource, times(2)).pushTransactionCommands(any(), eq(businessId))
    }

    @Test
    fun `execute() given commands when failure response always should call tracker`() {
        whenever(firebaseRemoteConfig.getLong(MAX_COUNT_PER_REQUEST_FOR_SYNC_TXN_KEY)).thenReturn(5)
        val maxCount = firebaseRemoteConfig.getLong(MAX_COUNT_PER_REQUEST_FOR_SYNC_TXN_KEY).toInt()
        val businessId = "business-id"
        val commands: List<Command> = listOf(
            CreateTransaction("cust_id", "txn_id", Transaction.Type.CREDIT, 1200),
            UpdateTransactionNote("txn_id2", "Noted"),
            DeleteTransaction("txn_id3")
        )
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
        whenever(firebaseRemoteConfig.getLong(MAX_COUNT_PER_REQUEST_FOR_SYNC_TXN_KEY)).thenReturn(5)
        whenever(firebaseRemoteConfig.getLong(MAX_RETRY_COUNT_OF_SYNC_TRANSACTION_KEY)).thenReturn(5)
        whenever(
            localSource.listTransactionsCommandsForCleanCustomers(
                any(),
                eq(businessId)
            )
        ).thenReturn(Observable.just(commands))
        val operationResponses =
            commands.map {
                OperationResponseForTransactions(
                    it.id,
                    Status.FAILURE.value,
                    Error(500, "Internal server error")
                )
            }
        whenever(remoteSource.pushTransactionCommands(any(), eq(businessId)))
            .thenReturn(Single.just(PushTransactionsCommandsResponse(operationResponses)))
        whenever(localSource.listTransactionsCommandsForCleanCustomers(businessId)).thenReturn(Observable.just(commands))
        whenever(localSource.markTransactionDirty(any(), any())).thenReturn(Completable.complete())
        whenever(localSource.deleteCommands(any())).thenReturn(Completable.complete())
        whenever(localSource.replaceTransactionId(any(), any())).thenReturn(Completable.complete())

        val testObserver = syncTransactionsCommands.execute(businessId).test()

        testObserver.assertComplete()
        verify(remoteSource, times(maxCount)).pushTransactionCommands(any(), eq(businessId))
        verify(tracker, times(1)).trackDebug(eq(CoreTracker.DebugType.SYNC_COMMANDS_MAX_RETRIES_EXCEEDED), anyOrNull())
    }
}
