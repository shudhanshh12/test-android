import `in`.okcredit.expense.ExpenseRepositoryImpl
import `in`.okcredit.expense.models.Models
import `in`.okcredit.expense.server.ExpenseRemoteSource
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.ThreadUtils
import java.util.concurrent.TimeUnit

class ExpenseRepositoryImplTest {
    private val expenseRemoteSource: ExpenseRemoteSource = mock()
    private val expenseApiImpl: ExpenseRepositoryImpl = ExpenseRepositoryImpl(expenseRemoteSource)

    companion object {
        private const val merchantId = "merchantId"
        private const val businessId = "businessId"
        private const val requestId = "requestId"
        private const val userId = "userId"
        private const val amount = 12000.toDouble()
        private const val amountInt = 12000
        private const val expenseType = "expenseType"
        private const val id = "id"
        private const val createdAt = 123

        var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        var dt: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")
        private val expenseDate = dt
        private val expense = Models.AddedExpense(userId, amount, expenseType, expenseDate)

        private val startTimeInMilliSec = TimeUnit.MILLISECONDS.toSeconds(DateTime.now().millis)
        private val endTimeInMilliSec = TimeUnit.MILLISECONDS.toSeconds(DateTime.now().millis)
        private val expenseListResponse = Models.ExpenseListResponse(listOf(), 12000.toDouble(), dt, dt)
        private val userExpenseTypes = Models.UserExpenseTypes(listOf("expenseTypes"))
        private val expenseRequestModel = Models.ExpenseRequestModel(requestId, expense)
        private val addExpenseResponse = Models.AddExpenseResponse(id, requestId, amountInt, expenseType, createdAt)
    }

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `getExpense should return Models ExpenseListResponse`() {
        // given
        whenever(
            expenseRemoteSource.getExpenses(
                startTimeInMilliSec,
                endTimeInMilliSec,
                businessId
            )
        ).thenReturn(Single.just(expenseListResponse))

        // when
        val result = expenseApiImpl.getExpenses(startTimeInMilliSec, endTimeInMilliSec, businessId).test()

        // then
        result.assertValue(expenseListResponse)
    }

    @Test
    fun `getUserExpenseTypes should return Models UserExpenseTypes`() {
        // given
        whenever(expenseRemoteSource.getUserExpenseTypes(merchantId)).thenReturn(Single.just(userExpenseTypes))

        // when
        val result = expenseApiImpl.getUserExpenseTypes(merchantId).test()

        // then
        result.assertValue(userExpenseTypes)
    }

    @Test
    fun `submitExpense should return Models AddExpenseResponse`() {
        // given
        whenever(expenseRemoteSource.submit(expenseRequestModel, businessId))
            .thenReturn(Single.just(addExpenseResponse))

        // when
        val result = expenseApiImpl.submitExpense(expenseRequestModel, businessId).test()

        // then
        result.assertValue(addExpenseResponse)
    }

    @Test
    fun `deleteExpense should return Completable`() {
        // given
        whenever(expenseRemoteSource.deleteExpense(id, businessId)).thenReturn(Completable.complete())

        // when
        val result = expenseApiImpl.deleteExpense(id, businessId).test()

        // then
        result.assertComplete()
    }
}
