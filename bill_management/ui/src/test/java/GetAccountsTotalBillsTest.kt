import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.Supplier
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Before
import org.junit.Test
import tech.okcredit.bills.BillRepository
import tech.okcredit.bills.IGetAccountsTotalBills
import tech.okcredit.use_case.GetAccountsTotalBills
import tech.okcredit.use_case.GetTxnStartTime

class GetAccountsTotalBillsTest {
    private val billRepository: BillRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private lateinit var getAccountsTotalBills: GetAccountsTotalBills

    companion object {
        private var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        private var dt: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")

        val request = GetTxnStartTime.Request("AccountId", "Customer")
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
        val businessId = "business-id"
    }

    @Before
    fun setUp() {
        getAccountsTotalBills = GetAccountsTotalBills(
            { billRepository },
            { getActiveBusinessId }
        )

        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `when role type is customer get the billcount`() {
        // given
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(billRepository.getTotalBillCount("accountId", businessId)).thenReturn(
            Observable.just(10)
        )
        whenever(
            billRepository.getUnreadBillCount("accountId", businessId)
        ).thenReturn(
            Observable.just(5)
        )

        // when
        val result = getAccountsTotalBills.execute("accountId").test()

        // then
        result.assertValue {
            it == IGetAccountsTotalBills.Response(10, 5)
        }
    }
}
