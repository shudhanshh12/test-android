package `in`.okcredit.merchant.customer_ui.onboarding.usecase

import `in`.okcredit.backend.contract.Customer.CustomerSyncStatus.*
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.core.model.Customer
import `in`.okcredit.merchant.customer_ui.TestData
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.junit.Test

class CustomerImmutableHelperTest {
    private val coreSdk: CoreSdk = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val customerImmutableHelper = CustomerImmutableHelper(
        { coreSdk },
        { getActiveBusinessId }
    )

    @Test
    fun `deleteImmutableAccount should call deleteImmutableAccount method from repository`() {
        runBlocking {
            val fakeCustomerId = "fakeCustomerId"
            val businessId = "business-id"
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            customerImmutableHelper.deleteImmutableAccount(fakeCustomerId)

            verify(coreSdk, times(1)).deleteImmutableAccount(fakeCustomerId)
        }
    }

    @Test
    fun `getCleanCustomerDescriptionIfImmutable should return pair of customer to null if customer is not immutable`() {
        runBlocking {
            val fakeBackendCustomer = TestData.CUSTOMER
            val fakeCoreCustomer: Customer = TestData.CUSTOMER_CORE
            val businessId = "business-id"
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(coreSdk.getCustomersByMobile(fakeBackendCustomer.mobile!!, businessId)).thenReturn(listOf(fakeCoreCustomer))
            val result = customerImmutableHelper.getCleanCustomerDescriptionIfImmutable(fakeBackendCustomer)

            Truth.assertThat(result).isEqualTo(fakeBackendCustomer to null)
        }
    }

    @Test
    fun `getCleanCustomerDescriptionIfImmutable should return pair of customer to customer description if customer is immutable`() {
        runBlocking {
            val fakeBackendCustomer = TestData.CUSTOMER.copy(customerSyncStatus = IMMUTABLE.code)
            val fakeCoreCustomer: Customer = TestData.CUSTOMER_CORE
            val businessId = "business-id"
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(coreSdk.getCustomersByMobile(fakeBackendCustomer.mobile!!, businessId)).thenReturn(listOf(fakeCoreCustomer))
            val result = customerImmutableHelper.getCleanCustomerDescriptionIfImmutable(fakeBackendCustomer)

            Truth.assertThat(result).isEqualTo(fakeBackendCustomer to fakeCoreCustomer.description)
        }
    }
}
