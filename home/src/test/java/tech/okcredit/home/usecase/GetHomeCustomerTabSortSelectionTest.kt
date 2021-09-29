package tech.okcredit.home.usecase

import `in`.okcredit.home.HomePreferences
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

class GetHomeCustomerTabSortSelectionTest {

    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val homePreferences: HomePreferences = mock()
    private val getHomeCustomerTabSortSelection = GetHomeCustomerTabSortSelection(
        { getActiveBusinessId },
        { homePreferences }
    )
    private val businessId = "business-id"

    @Test
    fun `execute() without error should return sort selection`() {
        // Given
        val sortBy = "name"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(homePreferences.getString(eq("customer_tab_sort"), any(), any())).thenReturn(flowOf(sortBy))

        // When
        val testObserver = getHomeCustomerTabSortSelection.execute().test()

        // Then
        testObserver.assertValue(sortBy)
        verify(getActiveBusinessId).execute()
        verify(homePreferences).getString(eq("customer_tab_sort"), any(), any())
    }
}
