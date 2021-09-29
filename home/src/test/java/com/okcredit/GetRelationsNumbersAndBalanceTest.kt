package com.okcredit

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.home.TestData
import tech.okcredit.home.usecase.GetRelationsNumbersAndBalanceImpl

class GetRelationsNumbersAndBalanceTest {

    private val customerRepo: CustomerRepo = mock()
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getRelationsNumbers = GetRelationsNumbersAndBalanceImpl({ customerRepo }, { supplierCreditRepository }, { getActiveBusinessId })

    @Test
    fun `Should return mobile numbers list from customer list`() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
        whenever(customerRepo.listActiveCustomers(TestData.BUSINESS_ID)).thenReturn(
            Observable.just(listOf(TestData.CUSTOMER))
        )

        whenever(supplierCreditRepository.listActiveSuppliers(TestData.BUSINESS_ID)).thenReturn(
            Observable.just(listOf())
        )

        val testObserver = getRelationsNumbers.execute().test()

        testObserver.assertValueAt(0, mutableListOf(TestData.CUSTOMER.mobile!!))
        testObserver.dispose()
    }

    @Test
    fun `Should return mobile numbers list from supplier list`() {

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
        whenever(customerRepo.listActiveCustomers(TestData.BUSINESS_ID)).thenReturn(
            Observable.just(listOf())
        )

        whenever(supplierCreditRepository.listActiveSuppliers(TestData.BUSINESS_ID)).thenReturn(
            Observable.just(listOf(TestData.SUPPLIER))
        )

        val testObserver = getRelationsNumbers.execute().test()

        testObserver.assertValueAt(0, mutableListOf(TestData.SUPPLIER.mobile!!))
        testObserver.dispose()
    }

    @Test
    fun `Should return mobile numbers list from supplier and customer list`() {

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
        whenever(customerRepo.listActiveCustomers(TestData.BUSINESS_ID)).thenReturn(
            Observable.just(listOf(TestData.CUSTOMER))
        )

        whenever(supplierCreditRepository.listActiveSuppliers(TestData.BUSINESS_ID)).thenReturn(
            Observable.just(listOf(TestData.SUPPLIER))
        )

        val testObserver = getRelationsNumbers.execute().test()

        testObserver.assertValueAt(
            0,
            mutableListOf(TestData.SUPPLIER.mobile!!, TestData.CUSTOMER.mobile!!)
        )
        testObserver.dispose()
    }
}
