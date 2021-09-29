package tech.okcredit.home.widgets.filter_option.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.home.HomePreferences
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test

class EnableFilterOptionVisibilityTest {
    private val customerRepo: CustomerRepo = mock()
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val homePreferences: HomePreferences = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val enableFilterOptionVisibility =
        EnableFilterOptionVisibility(
            { customerRepo },
            { supplierCreditRepository },
            { homePreferences },
            { getActiveBusinessId }
        )

    @Before
    fun setup() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.Default } returns Dispatchers.Unconfined
    }

    @Test
    fun `usecase should return false when both 2 customer and supplier added`() {
        // Given
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.getCustomersCount(businessId)).thenReturn(Observable.just(2))
        whenever(supplierCreditRepository.getSuppliersCount(businessId)).thenReturn(Observable.just(2))
        whenever(homePreferences.getBoolean(eq("filter enabled pref"), any(), anyOrNull())).thenReturn(flowOf(false))

        // when
        val testObserver =
            enableFilterOptionVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `usecase should return false when 2 customer and 1 supplier added`() {
        // Given
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.getCustomersCount(businessId)).thenReturn(Observable.just(2))
        whenever(supplierCreditRepository.getSuppliersCount(businessId)).thenReturn(Observable.just(1))
        whenever(homePreferences.getBoolean(eq("filter enabled pref"), any(), anyOrNull())).thenReturn(flowOf(false))

        // when
        val testObserver =
            enableFilterOptionVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `usecase should return false when 1 customer and 1 supplier added`() {
        // Given
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.getCustomersCount(businessId)).thenReturn(Observable.just(1))
        whenever(supplierCreditRepository.getSuppliersCount(businessId)).thenReturn(Observable.just(1))
        whenever(homePreferences.getBoolean(eq("filter enabled pref"), any(), anyOrNull())).thenReturn(flowOf(false))

        // when
        val testObserver =
            enableFilterOptionVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `usecase should return true when more than 3 customer and 3 supplier added`() {
        // Given
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.getCustomersCount(businessId)).thenReturn(Observable.just(6))
        whenever(supplierCreditRepository.getSuppliersCount(businessId)).thenReturn(Observable.just(6))
        whenever(homePreferences.getBoolean(eq("filter enabled pref"), any(), anyOrNull())).thenReturn(flowOf(true))

        // when
        val testObserver =
            enableFilterOptionVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false),
            Result.Success(true)
        )

        testObserver.dispose()
    }

    @Test
    fun `usecase should return true when 3 customer and 3 supplier added`() {
        // Given

        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.getCustomersCount(businessId)).thenReturn(Observable.just(3))
        whenever(supplierCreditRepository.getSuppliersCount(businessId)).thenReturn(Observable.just(3))
        whenever(homePreferences.getBoolean(eq("filter enabled pref"), any(), anyOrNull())).thenReturn(flowOf(true))

        // when
        val testObserver =
            enableFilterOptionVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false),
            Result.Success(true)
        )

        testObserver.dispose()
    }

    @Test
    fun `usecase should return true when 3 customer and 2 supplier added`() {
        // Given
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.getCustomersCount(businessId)).thenReturn(Observable.just(3))
        whenever(supplierCreditRepository.getSuppliersCount(businessId)).thenReturn(Observable.just(2))
        whenever(homePreferences.getBoolean(eq("filter enabled pref"), any(), anyOrNull())).thenReturn(flowOf(true))

        // when
        val testObserver =
            enableFilterOptionVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false),
            Result.Success(true)
        )

        testObserver.dispose()
    }

    @Test
    fun `usecase should return true when 2 customer and 3 supplier added`() {
        // Given
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.getCustomersCount(businessId)).thenReturn(Observable.just(2))
        whenever(supplierCreditRepository.getSuppliersCount(businessId)).thenReturn(Observable.just(3))
        whenever(homePreferences.getBoolean(eq("filter enabled pref"), any(), anyOrNull())).thenReturn(flowOf(true))

        // when
        val testObserver =
            enableFilterOptionVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false),
            Result.Success(true)
        )

        testObserver.dispose()
    }

    @Test
    fun `usecase should return false when 0 customer and 0 supplier added`() {
        // Given
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.getCustomersCount(businessId)).thenReturn(Observable.just(0))
        whenever(supplierCreditRepository.getSuppliersCount(businessId)).thenReturn(Observable.just(0))
        whenever(homePreferences.getBoolean(eq("filter enabled pref"), any(), anyOrNull())).thenReturn(flowOf(false))

        // when
        val testObserver =
            enableFilterOptionVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `usecase should return true when 1000 customer and 1000 supplier added`() {
        // Given
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.getCustomersCount(businessId)).thenReturn(Observable.just(1000))
        whenever(supplierCreditRepository.getSuppliersCount(businessId)).thenReturn(Observable.just(1000))
        whenever(homePreferences.getBoolean(eq("filter enabled pref"), any(), anyOrNull())).thenReturn(flowOf(true))

        // when
        val testObserver =
            enableFilterOptionVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false),
            Result.Success(true)
        )

        testObserver.dispose()
    }

    @Test
    fun `usecase should return true when 2000 customer and 2000 supplier added`() {
        // Given
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.getCustomersCount(businessId)).thenReturn(Observable.just(2000))
        whenever(supplierCreditRepository.getSuppliersCount(businessId)).thenReturn(Observable.just(2000))
        whenever(homePreferences.getBoolean(eq("filter enabled pref"), any(), anyOrNull())).thenReturn(flowOf(true))

        // when
        val testObserver =
            enableFilterOptionVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false),
            Result.Success(true)
        )

        testObserver.dispose()
    }
}
