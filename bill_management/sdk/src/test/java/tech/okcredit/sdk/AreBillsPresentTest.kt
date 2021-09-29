package tech.okcredit.sdk

import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Test
import tech.okcredit.sdk.store.BillLocalSource
import tech.okcredit.sdk.usecase.AreBillsPresent

class AreBillsPresentTest {
    private val billLocalSource: BillLocalSource = mock()
    private val areBillsPresent = AreBillsPresent(billLocalSource)

    companion object {
        val request = AreBillsPresent.Request(accountId = "AccountId")
    }
    @Test
    fun testWhenBillsArePresentReturnTrue() {
        // given
        whenever(billLocalSource.areBillsPresent(request.accountId)).thenReturn(Observable.just(true))

        // when
        val result = areBillsPresent.execute(request).test()

        // then
        result.assertValues(
            Result.Progress(),
            Result.Success(true)
        )
    }

    @Test
    fun testWhenBillsAreNotPresentReturnFalse() {
        // given
        whenever(billLocalSource.areBillsPresent(request.accountId)).thenReturn(Observable.just(false))

        // when
        val result = areBillsPresent.execute(request).test()

        // then
        result.assertValues(
            Result.Progress(),
            Result.Success(false)
        )
    }
}
