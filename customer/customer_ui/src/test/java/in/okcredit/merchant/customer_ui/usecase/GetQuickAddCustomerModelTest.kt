package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.accounting_core.contract.QuickAddCustomerModel
import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import androidx.room.EmptyResultSetException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException

class GetQuickAddCustomerModelTest {

    private val customerRepo: CustomerRepo = mock()
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private lateinit var getQuickAddCustomerModel: GetQuickAddCustomerModel

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
        getQuickAddCustomerModel = GetQuickAddCustomerModel(
            customerRepo = { customerRepo },
            supplierCreditRepository = { supplierCreditRepository },
            getActiveBusinessId = { getActiveBusinessId }
        )

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
    }

    @Test
    fun `if mobile empty return as it is`() {
        val observer = getQuickAddCustomerModel.execute("Test", "", null).test()

        observer.assertValue { it == QuickAddCustomerModel(name = "Test", mobile = "") }
    }

    @Test
    fun `if customer already present and active return the customer`() {
        val name = TestData.CUSTOMER.description
        val mobile = TestData.CUSTOMER.mobile
        whenever(customerRepo.findCustomerByMobile(mobile, TestData.BUSINESS_ID)).thenReturn(Single.just(TestData.CUSTOMER.copy(state = Customer.State.ACTIVE)))
        val observer = getQuickAddCustomerModel.execute(name, mobile ?: "", null).test()

        observer.assertValue {
            it == QuickAddCustomerModel(
                name = TestData.CUSTOMER.description,
                mobile = TestData.CUSTOMER.mobile,
                customerId = TestData.CUSTOMER.id
            )
        }
    }

    @Test
    fun `if customer already present and not active return the customer and reactivate`() {
        val name = TestData.CUSTOMER.description
        val mobile = TestData.CUSTOMER.mobile
        whenever(customerRepo.findCustomerByMobile(mobile, TestData.BUSINESS_ID)).thenReturn(Single.just(TestData.CUSTOMER.copy(status = Customer.State.BLOCKED.value)))
        val observer = getQuickAddCustomerModel.execute(name, mobile ?: "", null).test()

        observer.assertValue {
            it == QuickAddCustomerModel(
                name = TestData.CUSTOMER.description,
                mobile = TestData.CUSTOMER.mobile,
                customerId = TestData.CUSTOMER.id,
                shouldReactivate = true,
            )
        }
    }

    @Test
    fun `if customer not present and look in supplier for deleted reactivate`() {
        val name = TestData.SUPPLIER.name
        val mobile = TestData.SUPPLIER.mobile ?: "999"
        whenever(customerRepo.findCustomerByMobile(mobile, TestData.BUSINESS_ID)).thenReturn(Single.error(NoSuchElementException()))
        whenever(supplierCreditRepository.getSupplierByMobile(mobile, TestData.BUSINESS_ID)).thenReturn(
            Single.just(
                TestData.SUPPLIER.copy(
                    deleted = true
                )
            )
        )
        val observer = getQuickAddCustomerModel.execute(name, mobile, null).test()

        observer.assertValue {
            it == QuickAddCustomerModel(
                name = TestData.SUPPLIER.name,
                mobile = TestData.SUPPLIER.mobile,
                supplierId = TestData.SUPPLIER.id,
                shouldReactivate = true,
            )
        }
    }

    @Test
    fun `if customer not present and look in supplier`() {
        val name = TestData.SUPPLIER.name
        val mobile = TestData.SUPPLIER.mobile ?: "999"
        whenever(customerRepo.findCustomerByMobile(mobile, TestData.BUSINESS_ID)).thenReturn(Single.error(NoSuchElementException()))
        whenever(supplierCreditRepository.getSupplierByMobile(mobile, TestData.BUSINESS_ID)).thenReturn(
            Single.just(
                TestData.SUPPLIER.copy(
                    deleted = false
                )
            )
        )
        val observer = getQuickAddCustomerModel.execute(name, mobile, null).test()

        observer.assertValue {
            it == QuickAddCustomerModel(
                name = TestData.SUPPLIER.name,
                mobile = TestData.SUPPLIER.mobile,
                supplierId = TestData.SUPPLIER.id,
            )
        }
    }

    @Test
    fun `if customer and supplier not present return it is`() {
        val name = "test_name"
        val mobile = "999"
        whenever(customerRepo.findCustomerByMobile(mobile, TestData.BUSINESS_ID)).thenReturn(Single.error(NoSuchElementException()))
        whenever(supplierCreditRepository.getSupplierByMobile(mobile, TestData.BUSINESS_ID)).thenReturn(Single.error(EmptyResultSetException("")))
        val observer = getQuickAddCustomerModel.execute(name, mobile, null).test()

        observer.assertValue {
            it == QuickAddCustomerModel(
                name = name,
                mobile = mobile,
            )
        }
    }
}
