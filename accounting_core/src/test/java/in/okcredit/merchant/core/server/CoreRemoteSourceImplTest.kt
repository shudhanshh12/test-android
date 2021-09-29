package `in`.okcredit.merchant.core.server

import `in`.okcredit.merchant.core.Command
import `in`.okcredit.merchant.core.model.Customer
import `in`.okcredit.merchant.core.model.Transaction
import `in`.okcredit.merchant.core.model.TransactionAmountHistory
import `in`.okcredit.merchant.core.server.internal.CoreApiClient
import `in`.okcredit.merchant.core.server.internal.CoreApiMessages
import `in`.okcredit.merchant.core.server.internal.bulk_search_transactions.BulkSearchTransactionsRequest
import `in`.okcredit.merchant.core.server.internal.bulk_search_transactions.BulkSearchTransactionsResponse
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddTransactionRequest
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddTransactionResponse
import `in`.okcredit.shared.utils.AbFeatures
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.utils.ThreadUtils

class CoreRemoteSourceImplTest {

    private val apiClient: CoreApiClient = mock()
    private val ab: AbRepository = mock()
    private val coreServer = CoreRemoteSourceImpl { apiClient }

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkStatic("in.okcredit.merchant.core.server.CoreRemoteSourceImplKt")
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `pushCommands() when api call successful then return response body`() {
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST)).thenReturn(Observable.just(false))
        val mockRequestTransactions: CoreApiMessages.PushTransactionsCommandsRequest = mock()
        val mockResponseTransactions: CoreApiMessages.PushTransactionsCommandsResponse = mock()
        val businessId = "business-id"
        whenever(apiClient.pushTransactionCommands(any(), eq(businessId))).thenReturn(Single.just(Response.success(mockResponseTransactions)))

        val testObserver = coreServer.pushTransactionCommands(mockRequestTransactions, businessId).test()

        testObserver.assertValue(mockResponseTransactions)
        verify(apiClient, times(1)).pushTransactionCommands(mockRequestTransactions, businessId)
    }

    @Test
    fun `pushCommands() when api call unsuccessful then return error`() {
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST)).thenReturn(Observable.just(false))
        val mockError = Exception()
        val businessId = "business-id"
        val pushTransactionsCommandsRequest: CoreApiMessages.PushTransactionsCommandsRequest = mock()
        val mockResponseTransactions: Response<CoreApiMessages.PushTransactionsCommandsResponse?> = mock()
        whenever(apiClient.pushTransactionCommands(any(), eq(businessId))).thenReturn(Single.error(mockError))
        whenever(mockResponseTransactions.isSuccessful).thenReturn(false)

        val testObserver = coreServer.pushTransactionCommands(pushTransactionsCommandsRequest, businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).pushTransactionCommands(pushTransactionsCommandsRequest, businessId)
    }

    @Test
    fun `pushCommands() when response body is null then return error`() {
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST)).thenReturn(Observable.just(false))
        val mockError = Exception()
        val businessId = "business-id"
        val pushTransactionsCommandsRequest: CoreApiMessages.PushTransactionsCommandsRequest = mock()
        val mockResponseTransactions: Response<CoreApiMessages.PushTransactionsCommandsResponse?> = mock()
        whenever(apiClient.pushTransactionCommands(any(), eq(businessId))).thenReturn(Single.error(mockError))
        whenever(mockResponseTransactions.isSuccessful).thenReturn(true)
        whenever(mockResponseTransactions.body()).thenReturn(null)

        val testObserver = coreServer.pushTransactionCommands(pushTransactionsCommandsRequest, businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).pushTransactionCommands(pushTransactionsCommandsRequest, businessId)
    }

    @Test
    fun `getTransactions() when api call successful then return response body`() {
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST)).thenReturn(Observable.just(false))
        val mockResponse: CoreApiMessages.GetTransactionsResponse = mock()
        val source = "test"
        val businessId = "business-id"
        val timestamp = 1593001740000L
        whenever(apiClient.getTransactions(any(), eq("core_module_$source"), eq(businessId)))
            .thenReturn(Single.just(Response.success(mockResponse)))

        val testObserver = coreServer.getTransactions(timestamp, source, businessId).test()

        testObserver.assertValue(mockResponse)
        verify(apiClient, times(1)).getTransactions(any(), eq("core_module_$source"), eq(businessId))
    }

    @Test
    fun `getTransactions() when api call unsuccessful then return error`() {
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST)).thenReturn(Observable.just(false))
        val mockError = Exception()
        val mockResponse: Response<CoreApiMessages.GetTransactionsResponse?> = mock()
        val source = "test"
        val businessId = "business-id"
        val timestamp = 1593001740000L
        whenever(apiClient.getTransactions(any(), eq("core_module_$source"), eq(businessId))).thenReturn(
            Single.error(
                mockError
            )
        )
        whenever(mockResponse.isSuccessful).thenReturn(false)

        val testObserver = coreServer.getTransactions(timestamp, source, businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).getTransactions(any(), eq("core_module_$source"), eq(businessId))
    }

    @Test
    fun `getTransactions() when response body is null then return error`() {
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST)).thenReturn(Observable.just(false))
        val mockError = Exception()

        val mockResponse: Response<CoreApiMessages.GetTransactionsResponse?> = mock()
        val source = "test"
        val businessId = "business-id"
        val timestamp = 1593001740000L
        whenever(apiClient.getTransactions(any(), eq("core_module_$source"), eq(businessId))).thenReturn(
            Single.error(
                mockError
            )
        )
        whenever(mockResponse.isSuccessful).thenReturn(true)
        whenever(mockResponse.body()).thenReturn(null)

        val testObserver = coreServer.getTransactions(timestamp, source, businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).getTransactions(any(), eq("core_module_$source"), eq(businessId))
    }

    @Test
    fun `getTransactionFile() when api call successful then return response body`() {
        val mockResponse: CoreApiMessages.GetTransactionFileResponse = mock()
        val id = "test"
        val businessId = "business-id"
        whenever(apiClient.getTransactionFile(any(), eq(businessId))).thenReturn(Single.just(Response.success(mockResponse)))

        val testObserver = coreServer.getTransactionFile(id, businessId).test()

        testObserver.assertValue(mockResponse)
        verify(apiClient, times(1)).getTransactionFile(any(), eq(businessId))
    }

    @Test
    fun `getTransactionFile() when api call unsuccessful then return error`() {
        val mockError = mock<Error>()
        val mockResponse: Response<CoreApiMessages.GetTransactionFileResponse?> = mock()
        val id = "test"
        val businessId = "business-id"
        whenever(apiClient.getTransactionFile(any(), eq(businessId))).thenReturn(Single.error(mockError))
        whenever(mockResponse.isSuccessful).thenReturn(false)

        val testObserver = coreServer.getTransactionFile(id, businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).getTransactionFile(any(), eq(businessId))
    }

    @Test
    fun `getTransactionFile() when response body is null then return error`() {
        val mockError: Error = mock()
        val mockResponse: Response<CoreApiMessages.GetTransactionFileResponse?> = mock()
        val id = "test"
        val businessId = "business-id"
        whenever(apiClient.getTransactionFile(any(), eq(businessId))).thenReturn(Single.error(mockError))
        whenever(mockResponse.isSuccessful).thenReturn(true)
        whenever(mockResponse.body()).thenReturn(null)

        val testObserver = coreServer.getTransactionFile(id, businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).getTransactionFile(any(), eq(businessId))
    }

    @Test
    fun `getTransaction() when api call successful then return response body`() {
        val mockResponse: CoreApiMessages.Transaction = mock()
        val mockTransaction: Transaction = mock()
        val id = "test"
        val businessId = "business-id"
        whenever(apiClient.getTransaction(id, businessId)).thenReturn(Single.just(Response.success(mockResponse)))
        every { mockResponse.toTransaction() } returns mockTransaction

        val testObserver = coreServer.getTransaction(id, businessId).test()

        testObserver.assertValue(mockTransaction)
        verify(apiClient, times(1)).getTransaction(id, businessId)
    }

    @Test
    fun `getTransaction() when api call unsuccessful then return error`() {
        val mockError: Error = mock()
        val mockResponse: Response<CoreApiMessages.Transaction> = mock()
        val id = "test"
        val businessId = "business-id"
        whenever(apiClient.getTransaction(id, businessId)).thenReturn(Single.error(mockError))
        whenever(mockResponse.isSuccessful).thenReturn(false)

        val testObserver = coreServer.getTransaction(id, businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).getTransaction(id, businessId)
    }

    @Test
    fun `getTransaction() when response body is null then return error`() {
        val mockError: Error = mock()
        val mockResponse: Response<CoreApiMessages.Transaction> = mock()
        val id = "test"
        val businessId = "business-id"
        whenever(apiClient.getTransaction(id, businessId)).thenReturn(Single.error(mockError))
        whenever(mockResponse.isSuccessful).thenReturn(true)
        whenever(mockResponse.body()).thenReturn(null)

        val testObserver = coreServer.getTransaction(id, businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).getTransaction(id, businessId)
    }

    @Test
    fun `getCustomer() when api call successful then return response body`() {
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST)).thenReturn(Observable.just(false))
        val mockError: Error = mock()

        val mockResponse: CoreApiMessages.ApiCustomer = mock()
        val mockCustomer: Customer = mock()
        val id = "test"
        val businessId = "business-id"
        whenever(apiClient.getCustomer(id, businessId)).thenReturn(Single.just(Response.success(mockResponse)))
        every { mockResponse.toCustomer() } returns mockCustomer

        val testObserver = coreServer.getCustomer(id, businessId).test()

        testObserver.assertValue(mockCustomer)
        verify(apiClient, times(1)).getCustomer(id, businessId)
    }

    @Test
    fun `getCustomer() when api call unsuccessful then return error`() {
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST)).thenReturn(Observable.just(false))
        val mockError: Error = mock()
        val mockResponse: Response<CoreApiMessages.ApiCustomer?> = mock()
        val id = "test"
        val businessId = "business-id"
        whenever(apiClient.getCustomer(id, businessId)).thenReturn(Single.error(mockError))
        whenever(mockResponse.isSuccessful).thenReturn(false)

        val testObserver = coreServer.getCustomer(id, businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).getCustomer(id, businessId)
    }

    @Test
    fun `listCustomers() when api call successful then return response body`() {
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST)).thenReturn(Observable.just(false))
        val mockResponse: List<CoreApiMessages.ApiCustomer> = mock()
        val mockCustomerList: List<Customer> = mock()
        val mobile = "9876543210"
        val deleted = true
        val businessId = "business-id"
        whenever(apiClient.listCustomers(mobile, deleted, businessId)).thenReturn(Single.just(Response.success(mockResponse)))
        every { mockResponse.toCustomerList() } returns mockCustomerList

        val testObserver = coreServer.listCustomers(mobile, businessId).test()

        testObserver.assertValue(mockCustomerList)
        verify(apiClient, times(1)).listCustomers(mobile, deleted, businessId)
    }

    @Test
    fun `listCustomers() when api call unsuccessful then return error`() {
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST)).thenReturn(Observable.just(false))
        val mockError: Error = mock()
        val mockResponse: Response<List<CoreApiMessages.ApiCustomer>> = mock()
        val mobile = "9876543210"
        val deleted = true
        val businessId = "business-id"
        whenever(apiClient.listCustomers(mobile, deleted, businessId)).thenReturn(Single.error(mockError))
        whenever(mockResponse.isSuccessful).thenReturn(false)

        val testObserver = coreServer.listCustomers(mobile, businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).listCustomers(mobile, deleted, businessId)
    }

    @Test
    fun `deleteCustomer() when api call successful then return response body`() {
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST)).thenReturn(Observable.just(false))
        val mockResponse: Void = mock()
        val customerId = "customer_id_1"
        val businessId = "business-id"
        whenever(apiClient.deleteCustomer(customerId, businessId)).thenReturn(Single.just(Response.success(mockResponse)))

        val testObserver = coreServer.deleteCustomer(customerId, businessId).test()

        testObserver.assertComplete()
        verify(apiClient, times(1)).deleteCustomer(customerId, businessId)
    }

    @Test
    fun `deleteCustomer() when api call unsuccessful then return error`() {
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST)).thenReturn(Observable.just(false))
        val mockError: Error = mock()
        val customerId = "customer_id_1"
        val businessId = "business-id"
        whenever(apiClient.deleteCustomer(customerId, businessId)).thenReturn(Single.error(mockError))

        val testObserver = coreServer.deleteCustomer(customerId, businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).deleteCustomer(customerId, businessId)
    }

    @Test
    fun `bulkSearchTransactions() when api call successful then return response body`() {
        val mockResponse: BulkSearchTransactionsResponse = mock()
        val actionId = "action_id_1"
        val transactionIds = mock<List<String>>()
        val businessId = "business-id"
        val request = BulkSearchTransactionsRequest(actionId, transactionIds)
        whenever(apiClient.bulkSearchTransactions(businessId = businessId, request = request)).thenReturn(
            Single.just(
                Response.success(
                    mockResponse
                )
            )
        )

        val testObserver = coreServer.bulkSearchTransactions(actionId, transactionIds, businessId).test()

        testObserver.assertValue(mockResponse)
        verify(apiClient, times(1)).bulkSearchTransactions(businessId = businessId, request = request)
    }

    @Test
    fun `bulkSearchTransactions() when api call unsuccessful then return error`() {
        val mockError: Error = mock()
        val actionId = "action_id_1"
        val transactionIds = mock<List<String>>()
        val businessId = "business-id"
        val request = BulkSearchTransactionsRequest(actionId, transactionIds)
        whenever(apiClient.bulkSearchTransactions(businessId = businessId, request = request)).thenReturn(Single.error(mockError))

        val testObserver = coreServer.bulkSearchTransactions(actionId, transactionIds, businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).bulkSearchTransactions(businessId = businessId, request = request)
    }

    @Test
    fun `getTxnAmountHistory() when api call successful then return response body`() {
        val mockResponse: CoreApiMessages.GetTransactionAmountHistoryResponse = mock()
        val transactionId = "transaction_id_1"
        val businessId = "business-id"
        val response: TransactionAmountHistory = mock()
        val mockRequestObject = CoreApiMessages.GetTransactionAmountHistoryRequest(transactionId)
        whenever(apiClient.getTxnAmountHistory(mockRequestObject, businessId)).thenReturn(Single.just(Response.success(mockResponse)))
        every { mockResponse.toTransactionAmountHistory() } returns response

        val testObserver = coreServer.getTxnAmountHistory(transactionId, businessId).test()

        testObserver.assertValue(response)
        verify(apiClient, times(1)).getTxnAmountHistory(mockRequestObject, businessId)
    }

    @Test
    fun `getTxnAmountHistory() when api call unsuccessful then return error`() {
        val mockError = Exception()
        val mockResponse: Response<CoreApiMessages.GetTransactionsResponse?> = mock()
        val transactionId = "transaction_id_1"
        val businessId = "business-id"
        val mockRequestObject = CoreApiMessages.GetTransactionAmountHistoryRequest(transactionId)
        whenever(apiClient.getTxnAmountHistory(mockRequestObject, businessId)).thenReturn(Single.error(mockError))
        whenever(mockResponse.isSuccessful).thenReturn(false)

        val testObserver = coreServer.getTxnAmountHistory(transactionId, businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).getTxnAmountHistory(mockRequestObject, businessId)
    }

    @Test
    fun `quickAddTransaction() should call quickAddTransaction in api client`() {
        val quickAddTransactionRequest = mock<QuickAddTransactionRequest>()
        val quickAddTransactionResponse = mock<QuickAddTransactionResponse>()
        val businessId = "business-id"
        whenever(apiClient.quickAddTransaction(quickAddTransactionRequest, businessId)).thenReturn(
            Single.just(quickAddTransactionResponse)
        )

        val testObserver = coreServer.quickAddTransaction(quickAddTransactionRequest, businessId).test()

        testObserver.assertValue(quickAddTransactionResponse)
        verify(apiClient).quickAddTransaction(quickAddTransactionRequest, businessId)
    }

    @Test
    fun `getSuggestedCustomerIdsForAddTransaction() when api call successful then return response body`() {
        val mockResponse: List<String> = listOf("account_id1", "account_id2", "account_id3")
        val mockApiResponse: CoreApiMessages.SuggestedCustomerIdsForAddTransactionResponse =
            CoreApiMessages.SuggestedCustomerIdsForAddTransactionResponse(mockResponse)
        val businessId = "business-id"
        whenever(apiClient.getSuggestedCustomerIdsForAddTransaction(businessId))
            .thenReturn(Single.just(Response.success(mockApiResponse)))

        val testObserver = coreServer.getSuggestedCustomerIdsForAddTransaction(businessId).test()

        testObserver.assertValue(mockResponse)
        verify(apiClient).getSuggestedCustomerIdsForAddTransaction(businessId)
    }

    @Test
    fun `getSuggestedCustomerIdsForAddTransaction() when api call unsuccessful then return error`() {
        val mockError = Exception()
        val mockResponse: Response<CoreApiMessages.GetTransactionsResponse?> = mock()
        val businessId = "business-id"
        whenever(apiClient.getSuggestedCustomerIdsForAddTransaction(businessId)).thenReturn(Single.error(mockError))
        whenever(mockResponse.isSuccessful).thenReturn(false)

        val testObserver = coreServer.getSuggestedCustomerIdsForAddTransaction(businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient).getSuggestedCustomerIdsForAddTransaction(businessId)
    }

    @Test
    fun `toApiCommandList() with create transaction should return expected list`() {
        val createTransaction = Command.CreateTransaction(
            customerId = "customer-id",
            transactionId = "transaction_id",
            type = Transaction.Type.PAYMENT,
            amount = 10000,
            imagesUriList = listOf(),
            note = null,
            billDate = null,
            inputType = null,
            voiceId = null
        )
        val commandList = listOf(
            createTransaction
        )
        val transaction = CoreApiMessages.Transaction(
            id = createTransaction.transactionId,
            account_id = createTransaction.customerId,
            type = createTransaction.type.code,
            amount = createTransaction.amount,
            creator_role = CoreApiMessages.ROLE_SELLER,
            create_time_ms = createTransaction.timestamp.epoch,
            note = createTransaction.note,
            images = createTransaction.transactionImages?.map {
                CoreApiMessages.TransactionImage(
                    id = it.id,
                    transaction_id = createTransaction.transactionId,
                    url = it.url,
                    create_time = it.createdAt.epoch
                )
            },
            bill_date_ms = createTransaction.billDate?.epoch ?: createTransaction.timestamp.epoch,
            alert_sent_by_creator = false,
            meta = CoreApiMessages.Meta(createTransaction.inputType, createTransaction.voiceId),
            transaction_state = Transaction.State.CREATED.code
        )
        val expected = listOf(
            CoreApiMessages.ApiTransactionCommand(
                id = createTransaction.id,
                type = ApiCommandType.CREATE_TRANSACTION.type.value,
                path = ApiCommandType.CREATE_TRANSACTION.path,
                transaction = transaction,
                image = null,
                timestamp = createTransaction.timestamp.epoch,
                mask = null,
                transaction_id = createTransaction.transactionId,
                image_id = null
            )
        )

        val commandApiList = commandList.toApiTransactionCommandList()

        assertEquals(expected, commandApiList)
    }

    @Test
    fun `toApiCommandList() with delete transaction should return expected list`() {
        val deleteTransaction = Command.DeleteTransaction(transactionId = "txn-id")
        val commandList = listOf(
            deleteTransaction
        )
        val transaction = CoreApiMessages.Transaction(
            transaction_state = Transaction.State.DELETED.code,
            deleter_role = CoreApiMessages.ROLE_SELLER
        )
        val expected = listOf(
            CoreApiMessages.ApiTransactionCommand(
                id = deleteTransaction.id,
                type = ApiCommandType.DELETE_TRANSACTION.type.value,
                path = ApiCommandType.DELETE_TRANSACTION.path,
                transaction = transaction,
                image = null,
                timestamp = deleteTransaction.timestamp.epoch,
                mask = ApiCommandType.DELETE_TRANSACTION.mask,
                transaction_id = deleteTransaction.transactionId,
                image_id = null
            )
        )

        val commandApiList = commandList.toApiTransactionCommandList()

        assertEquals(expected, commandApiList)
    }
}
