import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Before
import org.junit.Test
import tech.okcredit.use_case.GetTxnStartTime

class GetTxnStartTimeTest {
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val customerRepo: CustomerRepo = mock()
    private lateinit var getTxnStartTime: GetTxnStartTime

    companion object {
        private var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        private var dt: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")

        val request = GetTxnStartTime.Request("AccountId", "Customer")
        val businessId = "business-id"
        val customer = Customer(
            id = "customerId",
            status = 1,
            mobile = "mobile",
            description = "description",
            createdAt = dt,
            txnStartTime = System.currentTimeMillis(),
            balanceV2 = 100L,
            transactionCount = 5,
            lastActivity = dt,
            lastPayment = dt,
            accountUrl = "accountUrl",
            profileImage = "profileImage",
            address = "address",
            email = "email",
            newActivityCount = 101L,
            lastViewTime = dt,
            registered = true,
            lastBillDate = dt,
            txnAlertEnabled = true,
            lang = "lang",
            reminderMode = "reminderMode",
            isLiveSales = true,
            addTransactionPermissionDenied = true,
            state = Customer.State.ACTIVE,
            blockedByCustomer = true,
            restrictContactSync = true
        )

        val supplier = Supplier(
            id = "supplierId",
            registered = true,
            deleted = false,
            createTime = dt,
            txnStartTime = System.currentTimeMillis(),
            name = "John Lennon",
            mobile = "9999999999",
            address = "",
            profileImage = "",
            balance = 0,
            newActivityCount = 0,
            lastActivityTime = dt,
            lastViewTime = dt,
            txnAlertEnabled = true,
            lang = "",
            syncing = true,
            lastSyncTime = null,
            addTransactionRestricted = true,
            state = 0,
            blockedBySupplier = false,
            restrictContactSync = false
        )
    }

    @Before
    fun setUp() {
        getTxnStartTime = GetTxnStartTime(supplierCreditRepository, customerRepo, { getActiveBusinessId })
    }

    @Test
    fun `test the txnstartime for customer`() {
        // given
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.getCustomer(request.accountId, businessId)).thenReturn(Observable.just(customer))
        whenever(supplierCreditRepository.getSupplier(request.accountId, businessId)).thenReturn(Observable.just(supplier))

        // when
        val result = getTxnStartTime.execute(request).test()

        // then
        result.assertValues(
            Result.Progress(),
            Result.Success(customer.txnStartTime!!.times(1000))
        )
    }

    @Test
    fun `test the txnstartime for Supplier`() {
        // given
        val supplierRequest = GetTxnStartTime.Request("AccountId", "Supplier")
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.getCustomer(request.accountId, businessId)).thenReturn(Observable.just(customer))
        whenever(supplierCreditRepository.getSupplier(request.accountId, businessId)).thenReturn(Observable.just(supplier))

        // when
        val result = getTxnStartTime.execute(supplierRequest).test()

        // then
        result.assertValues(
            Result.Progress(),
            Result.Success(supplier.txnStartTime.times(1000))
        )
    }
}
