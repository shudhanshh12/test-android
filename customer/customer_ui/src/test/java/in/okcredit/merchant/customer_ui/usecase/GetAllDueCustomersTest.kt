package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

class GetAllDueCustomersTest {

    private val customerRepo: CustomerRepo = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private lateinit var getAllDueCustomers: GetAllDueCustomers

    @Before
    fun setup() {
        getAllDueCustomers = GetAllDueCustomers(customerRepo) { getActiveBusinessId }
    }

    @Test
    fun `should return customer contains due amount is between 10 and 100000`() {
        val CUSTOMER = Customer(
            "1234",
            0,
            Transaction.CREDIT,
            "9999999999",
            "Bob dylan",
            DateTime(2018, 10, 2, 0, 0, 0),
            100L,
            -20_00,
            0,
            DateTime(2018, 10, 2, 0, 0, 0),
            DateTime(2018, 10, 2, 0, 0, 0),
            "http://okcredit.in",
            null,
            null,
            null,
            0L,
            DateTime(2018, 10, 2, 0, 0, 0),
            true,
            DateTime(2018, 10, 2, 0, 0, 0),
            false,
            "en",
            "sms",
            false,
            false,
            null,
            false,
            false, false, DateTime(2018, 10, 2, 0, 0, 0), false, 0, 0,
            lastReminderSendTime = DateTime(0)
        )

        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(
            customerRepo.listCustomers(businessId)
        ).thenReturn(Observable.just(listOf(CUSTOMER)))

        val testObserver = getAllDueCustomers.execute(GetAllDueCustomers.Request("")).test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(
                listOf(CUSTOMER)
            )
        )

        testObserver.dispose()
    }

    @Test
    fun `should not return if customers due amount less than 10`() {
        val CUSTOMER = Customer(
            "1234",
            0,
            Transaction.CREDIT,
            "9999999999",
            "Bob dylan",
            DateTime(2018, 10, 2, 0, 0, 0),
            100L,
            -2_00,
            0,
            DateTime(2018, 10, 2, 0, 0, 0),
            DateTime(2018, 10, 2, 0, 0, 0),
            "http://okcredit.in",
            null,
            null,
            null,
            0L,
            DateTime(2018, 10, 2, 0, 0, 0),
            true,
            DateTime(2018, 10, 2, 0, 0, 0),
            false,
            "en",
            "sms",
            false,
            false,
            null,
            false,
            false,
            false,
            DateTime(2018, 10, 2, 0, 0, 0),
            false,
            0,
            0,
            lastReminderSendTime = DateTime(0),
        )

        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(
            customerRepo.listCustomers(businessId)
        ).thenReturn(Observable.just(listOf(CUSTOMER)))

        val testObserver = getAllDueCustomers.execute(GetAllDueCustomers.Request("")).test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(
                listOf()
            )
        )

        testObserver.dispose()
    }

    @Test
    fun `should not return if customers due amount is grater than 10,000,0`() {
        val CUSTOMER = Customer(
            "1234",
            0,
            Transaction.CREDIT,
            "9999999999",
            "Bob dylan",
            DateTime(2018, 10, 2, 0, 0, 0),
            100L,
            -2000000_00,
            0,
            DateTime(2018, 10, 2, 0, 0, 0),
            DateTime(2018, 10, 2, 0, 0, 0),
            "http://okcredit.in",
            null,
            null,
            null,
            0L,
            DateTime(2018, 10, 2, 0, 0, 0),
            true,
            DateTime(2018, 10, 2, 0, 0, 0),
            false,
            "en",
            "sms",
            false,
            false,
            null,
            false,
            false,
            false,
            DateTime(2018, 10, 2, 0, 0, 0),
            false,
            0,
            0,
            lastReminderSendTime = DateTime(0)
        )

        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(
            customerRepo.listCustomers(businessId)
        ).thenReturn(Observable.just(listOf(CUSTOMER)))

        val testObserver = getAllDueCustomers.execute(GetAllDueCustomers.Request("")).test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(
                listOf()
            )
        )

        testObserver.dispose()
    }

    @Test
    fun `should not return if mobile is not present`() {
        val CUSTOMER = Customer(
            "1234",
            0,
            Transaction.CREDIT,
            "",
            "Bob dylan",
            DateTime(2018, 10, 2, 0, 0, 0),
            100L,
            -200_00,
            0,
            DateTime(2018, 10, 2, 0, 0, 0),
            DateTime(2018, 10, 2, 0, 0, 0),
            "http://okcredit.in",
            null,
            null,
            null,
            0L,
            DateTime(2018, 10, 2, 0, 0, 0),
            true,
            DateTime(2018, 10, 2, 0, 0, 0),
            false,
            "en",
            "sms",
            false,
            false,
            null,
            false,
            false,
            false,
            DateTime(2018, 10, 2, 0, 0, 0),
            false,
            0,
            0,
            lastReminderSendTime = DateTime(0)
        )

        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(
            customerRepo.listCustomers(businessId)
        ).thenReturn(Observable.just(listOf(CUSTOMER)))

        val testObserver = getAllDueCustomers.execute(GetAllDueCustomers.Request("")).test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(
                listOf()
            )
        )

        testObserver.dispose()
    }

    @Test
    fun `should not return for payment transaction`() {
        val CUSTOMER = Customer(
            "1234",
            0,
            Transaction.PAYMENT,
            "98989898",
            "Bob dylan",
            DateTime(2018, 10, 2, 0, 0, 0),
            100L,
            -200_00,
            0,
            DateTime(2018, 10, 2, 0, 0, 0),
            DateTime(2018, 10, 2, 0, 0, 0),
            "http://okcredit.in",
            null,
            null,
            null,
            0L,
            DateTime(2018, 10, 2, 0, 0, 0),
            true,
            DateTime(2018, 10, 2, 0, 0, 0),
            false,
            "en",
            "sms",
            false,
            false,
            null,
            false,
            false,
            false,
            DateTime(2018, 10, 2, 0, 0, 0),
            false,
            0,
            0,
            lastReminderSendTime = DateTime(0)
        )

        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(
            customerRepo.listCustomers(businessId)
        ).thenReturn(Observable.just(listOf(CUSTOMER)))

        val testObserver = getAllDueCustomers.execute(GetAllDueCustomers.Request("")).test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(
                listOf()
            )
        )

        testObserver.dispose()
    }

    @Test
    fun `should not return if search name doesn't contain searchQuery`() {
        val CUSTOMER = Customer(
            "1234",
            0,
            Transaction.CREDIT,
            "9999999999",
            "Bob dylan",
            DateTime(2018, 10, 2, 0, 0, 0),
            100L,
            -20_00,
            0,
            DateTime(2018, 10, 2, 0, 0, 0),
            DateTime(2018, 10, 2, 0, 0, 0),
            "http://okcredit.in",
            null,
            null,
            null,
            0L,
            DateTime(2018, 10, 2, 0, 0, 0),
            true,
            DateTime(2018, 10, 2, 0, 0, 0),
            false,
            "en",
            "sms",
            false,
            false,
            null,
            false,
            false,
            false, DateTime(2018, 10, 2, 0, 0, 0),
            false, 0, 0,
            lastReminderSendTime = DateTime(0)
        )

        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(
            customerRepo.listCustomers(businessId)
        ).thenReturn(Observable.just(listOf(CUSTOMER)))

        val testObserver = getAllDueCustomers.execute(GetAllDueCustomers.Request("Wind")).test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(
                listOf()
            )
        )

        testObserver.dispose()
    }

    @Test
    fun `should return if search name doesn't contain searchQuery`() {
        val CUSTOMER = Customer(
            "1234",
            0,
            Transaction.CREDIT,
            "9999999999",
            "Bob dylan",
            DateTime(2018, 10, 2, 0, 0, 0),
            100L,
            -20_00,
            0,
            DateTime(2018, 10, 2, 0, 0, 0),
            DateTime(2018, 10, 2, 0, 0, 0),
            "http://okcredit.in",
            null,
            null,
            null,
            0L,
            DateTime(2018, 10, 2, 0, 0, 0),
            true,
            DateTime(2018, 10, 2, 0, 0, 0),
            false,
            "en",
            "sms",
            false,
            false,
            null,
            false,
            false,
            false,
            DateTime(2018, 10, 2, 0, 0, 0),
            false,
            0,
            0,
            lastReminderSendTime = DateTime(0)
        )

        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(
            customerRepo.listCustomers(businessId)
        ).thenReturn(Observable.just(listOf(CUSTOMER)))

        val testObserver = getAllDueCustomers.execute(GetAllDueCustomers.Request("dylan")).test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(
                listOf(CUSTOMER)
            )
        )

        testObserver.dispose()
    }
}
