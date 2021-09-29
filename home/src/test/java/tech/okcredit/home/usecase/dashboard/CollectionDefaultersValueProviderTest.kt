package tech.okcredit.home.usecase.dashboard

import `in`.okcredit.backend._offline.usecase.GetDefaulterCustomerList
import `in`.okcredit.backend.contract.Customer
import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Observable
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.home.R
import tech.okcredit.home.usecase.dashboard.CollectionDefaultersValueProvider.CollectionDefaultersDashboardValue
import tech.okcredit.home.usecase.dashboard.CollectionDefaultersValueProvider.Companion.DEFAULT_NUMBER_OF_DEFAULTERS
import tech.okcredit.home.utils.UriUtils
import tech.okcredit.home.utils.UriUtils.replaceLastSegmentWithValue
import kotlin.math.min

class CollectionDefaultersValueProviderTest {

    private val context: Context = mock()
    private val getDefaulterCustomerList: GetDefaulterCustomerList = mock()
    private val collectionDefaultersValueProvider = CollectionDefaultersValueProvider(
        Lazy { context }, Lazy { getDefaulterCustomerList }
    )

    @Before
    fun setUp() {
        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit

        mockkObject(UriUtils)
        val deepLinkWithPlaceholder = "http://test/{placeholder}"
        val deepLink = "http://test/abc123"
        whenever(context.getString(R.string.customer_profile_dialog_deeplink)).thenReturn(deepLinkWithPlaceholder)
        every { deepLinkWithPlaceholder.replaceLastSegmentWithValue(any()) } returns deepLink
    }

    @Test
    fun `getValue() given more than 4 defaulters should return 4 defaulters with required fields`() {
        val customer1 = mock<Customer>().apply {
            whenever(id).thenReturn("customer_id1")
            whenever(description).thenReturn("Sheldon")
            whenever(balanceV2).thenReturn(1000)
        }
        val customer2 = mock<Customer>().apply {
            whenever(id).thenReturn("customer_id2")
            whenever(description).thenReturn("Raj")
            whenever(balanceV2).thenReturn(2000)
        }
        val customer3 = mock<Customer>().apply {
            whenever(id).thenReturn("customer_id3")
            whenever(description).thenReturn("Leonard")
            whenever(balanceV2).thenReturn(3000)
        }
        val customer4 = mock<Customer>().apply {
            whenever(id).thenReturn("customer_id4")
            whenever(description).thenReturn("Penny")
            whenever(balanceV2).thenReturn(4000)
        }
        val customer5 = mock<Customer>().apply {
            whenever(id).thenReturn("customer_id5")
            whenever(description).thenReturn("Amy")
            whenever(balanceV2).thenReturn(5000)
        }
        val customerList = listOf(customer1, customer2, customer3, customer4, customer5)
        whenever(getDefaulterCustomerList.execute()).thenReturn(Observable.just(customerList))
        val appendString = " (${customerList.size})"
        whenever(context.getString(R.string.dashboard_collection_defaulters_title_append, customerList.size))
            .thenReturn(appendString)

        val testObserver = collectionDefaultersValueProvider.getValue(null).test()

        val result = testObserver.values().first() as CollectionDefaultersDashboardValue
        assertTrue(result.defaulters?.size == min(DEFAULT_NUMBER_OF_DEFAULTERS, customerList.size))
        assertTrue(result.string == appendString)
    }

    @Test
    fun `getValue() given more than 4 defaulters should return 3 defaulters with required fields`() {
        val itemCount = 3
        val customer1 = mock<Customer>().apply {
            whenever(id).thenReturn("customer_id1")
            whenever(description).thenReturn("Sheldon")
            whenever(balanceV2).thenReturn(1000)
        }
        val customer2 = mock<Customer>().apply {
            whenever(id).thenReturn("customer_id2")
            whenever(description).thenReturn("Raj")
            whenever(balanceV2).thenReturn(2000)
        }
        val customer3 = mock<Customer>().apply {
            whenever(id).thenReturn("customer_id3")
            whenever(description).thenReturn("Leonard")
            whenever(balanceV2).thenReturn(3000)
        }
        val customer4 = mock<Customer>().apply {
            whenever(id).thenReturn("customer_id4")
            whenever(description).thenReturn("Penny")
            whenever(balanceV2).thenReturn(4000)
        }
        val customer5 = mock<Customer>().apply {
            whenever(id).thenReturn("customer_id5")
            whenever(description).thenReturn("Amy")
            whenever(balanceV2).thenReturn(5000)
        }
        val customerList = listOf(customer1, customer2, customer3, customer4, customer5)
        whenever(getDefaulterCustomerList.execute()).thenReturn(Observable.just(customerList))
        val appendString = " (${customerList.size})"
        whenever(context.getString(R.string.dashboard_collection_defaulters_title_append, customerList.size))
            .thenReturn(appendString)

        val testObserver = collectionDefaultersValueProvider.getValue(DashboardValueProvider.Request(itemCount)).test()

        val result = testObserver.values().first() as CollectionDefaultersDashboardValue
        assertTrue(result.defaulters?.size == min(itemCount, customerList.size))
        assertTrue(result.string == appendString)
    }

    @Test
    fun `getValue() given less than 4 defaulters should return all defaulters with required fields`() {
        val customer1 = mock<Customer>().apply {
            whenever(id).thenReturn("customer_id1")
            whenever(description).thenReturn("Sheldon")
            whenever(balanceV2).thenReturn(1000)
        }
        val customer2 = mock<Customer>().apply {
            whenever(id).thenReturn("customer_id2")
            whenever(description).thenReturn("Raj")
            whenever(balanceV2).thenReturn(2000)
        }
        val customer3 = mock<Customer>().apply {
            whenever(id).thenReturn("customer_id3")
            whenever(description).thenReturn("Leonard")
            whenever(balanceV2).thenReturn(3000)
        }
        val customerList = listOf(customer1, customer2, customer3)
        whenever(getDefaulterCustomerList.execute()).thenReturn(Observable.just(customerList))
        val appendString = " (${customerList.size})"
        whenever(context.getString(R.string.dashboard_collection_defaulters_title_append, customerList.size))
            .thenReturn(appendString)

        val testObserver = collectionDefaultersValueProvider.getValue(null).test()

        val result = testObserver.values().first() as CollectionDefaultersDashboardValue
        assertTrue(result.defaulters?.size == min(DEFAULT_NUMBER_OF_DEFAULTERS, customerList.size))
        assertTrue(result.string == appendString)
    }

    @Test
    fun `getValue() given 0 defaulters should return no defaulters`() {
        val customerList = listOf<Customer>()
        whenever(getDefaulterCustomerList.execute()).thenReturn(Observable.just(customerList))
        val appendString = " (${customerList.size})"
        whenever(context.getString(R.string.dashboard_collection_defaulters_title_append, customerList.size))
            .thenReturn(appendString)

        val testObserver = collectionDefaultersValueProvider.getValue(null).test()

        val result = testObserver.values().first() as CollectionDefaultersDashboardValue
        assertTrue(result.defaulters?.size == min(DEFAULT_NUMBER_OF_DEFAULTERS, customerList.size))
        assertTrue(result.string == appendString)
    }
}
