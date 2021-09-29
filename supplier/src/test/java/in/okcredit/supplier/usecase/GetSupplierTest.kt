package `in`.okcredit.supplier.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Test
import java.util.concurrent.TimeUnit

class GetSupplierTest {
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getSupplier = GetSupplier(Lazy { supplierCreditRepository }, { getActiveBusinessId })

    companion object {
        var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        var dt: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")
        val businessId = "business-id"
        private val supplier = Supplier(
            id = "supplierid",
            createTime = dt,
            txnStartTime = TimeUnit.MILLISECONDS.toSeconds(dt.millis),
            name = "supplierName",
            mobile = "SupplierMobile",
            profileImage = "profileImage",
            restrictContactSync = false
        )
    }

    @Test
    fun `execute test`() {
        // given
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(supplierCreditRepository.getSupplier("supplierid", businessId)).thenReturn(Observable.just(supplier))

        val result = getSupplier.executeObservable("supplierid").test()

        result.assertValue(supplier)
    }
}
