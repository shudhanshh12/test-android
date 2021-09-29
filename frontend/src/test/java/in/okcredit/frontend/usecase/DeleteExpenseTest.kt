package `in`.okcredit.frontend.usecase

import `in`.okcredit.expense.ExpenseRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

class DeleteExpenseTest {

    private val expenseRepository = mock<ExpenseRepository>()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val deleteExpense = DeleteExpense(
        { expenseRepository },
        { getActiveBusinessId }
    )

    @Test
    fun `should call delete expense and return progress and success result`() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(expenseRepository.deleteExpense("id", businessId)).thenReturn(Completable.complete())

        val testObserver = deleteExpense.execute("id").test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(Unit)
        )
    }
}
