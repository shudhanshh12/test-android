import `in`.okcredit.expense.models.Models
import `in`.okcredit.expense.server.ExpenseApiClient
import `in`.okcredit.expense.server.ExpenseRemoteSourceImpl
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import tech.okcredit.android.base.utils.ThreadUtils
import java.util.concurrent.TimeUnit

class ExpenseRemoteSourceImplTest {

    private val expenseApiClient: ExpenseApiClient = mock()
    private val serverImpl: ExpenseRemoteSourceImpl = ExpenseRemoteSourceImpl(expenseApiClient)

    companion object {
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
        private val expenseListResponse =
            Response.success(Models.ExpenseListResponse(listOf(), 12000.toDouble(), dt, dt))
        private val userExpenseTypes = Response.success(Models.UserExpenseTypes(listOf("expenseTypes")))
        private val expenseRequestModel = Models.ExpenseRequestModel(requestId, expense)
        private val addExpenseResponse =
            Response.success(Models.AddExpenseResponse(id, requestId, amountInt, expenseType, createdAt))
    }

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `getExpenses should return ExpenseListResponse`() {
        // given
        whenever(
            expenseApiClient.getExpenses(
                businessId,
                startTimeInMilliSec,
                endTimeInMilliSec,
                businessId
            )
        ).thenReturn(Single.just(expenseListResponse))
        // when
        val result = serverImpl.getExpenses(startTimeInMilliSec, endTimeInMilliSec, businessId).test()

        result.assertValue(expenseListResponse.body())
    }

    @Test
    fun `getUserExpenseTypes should return UserExpenseTypes`() {
        // given
        whenever(expenseApiClient.getUserExpenseTypes(businessId, businessId)).thenReturn(Single.just(userExpenseTypes))
        // when
        val result = serverImpl.getUserExpenseTypes(businessId).test()

        result.assertValue(userExpenseTypes.body())
    }

    @Test
    fun `submit should return AddExpenseResponse`() {
        // given
        whenever(expenseApiClient.submitExpense(expenseRequestModel, businessId)).thenReturn(
            Single.just(
                addExpenseResponse
            )
        )
        // when
        val result = serverImpl.submit(expenseRequestModel, businessId).test()

        result.assertValue(addExpenseResponse.body())
    }
}
