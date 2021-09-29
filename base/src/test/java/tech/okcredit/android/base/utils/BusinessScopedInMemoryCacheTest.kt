package tech.okcredit.android.base.utils

import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BusinessScopedInMemoryCacheTest {

    @Test
    fun `given same business id isValidForBusinessId should return true`() {
        val businessId1 = "businessId1"
        val cache = BusinessScopedInMemoryCache<String>(mock(), businessId1)

        assertTrue(cache.isValidForBusinessId(businessId1))
    }

    @Test
    fun `given not same business id isValidForBusinessId should return false`() {
        val businessId1 = "businessId1"
        val businessId2 = "businessId2"
        val cache = BusinessScopedInMemoryCache<String>(mock(), businessId1)

        assertFalse(cache.isValidForBusinessId(businessId2))
    }
}
