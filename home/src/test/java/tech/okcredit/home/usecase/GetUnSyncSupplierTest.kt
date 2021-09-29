package tech.okcredit.home.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.merchant.suppliercredit.Transaction
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Test

class GetUnSyncSupplierTest {
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getUnSyncSupplierTest = GetUnSyncSupplier(supplierCreditRepository) { getActiveBusinessId }

    companion object {

        var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        var dateTime: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")

        val transaction = Transaction(
            "2",
            "supplier_id_1",
            null,
            true,
            10,
            null,
            null,
            dateTime,
            dateTime,
            false,
            false,
            dateTime,
            false,
            dateTime,
            false,
            dateTime,
            -1
        )

        val listOfTransactions = listOf(transaction)
    }

    @Test
    fun testExecute() {
        // given
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(supplierCreditRepository.listDirtyTransactions(businessId)).thenReturn(Observable.just(listOfTransactions))

        // when
        val testObserver = getUnSyncSupplierTest.execute().subscribeOn(Schedulers.trampoline()).test()

        // then
        testObserver.assertValue(listOf("supplier_id_1"))
        testObserver.dispose()
    }
}
