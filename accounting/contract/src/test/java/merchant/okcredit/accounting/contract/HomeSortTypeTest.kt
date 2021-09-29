package merchant.okcredit.accounting.contract

import org.junit.Assert.*
import org.junit.Test

class HomeSortTypeTest {

    @Test
    fun `from value should return NAME when value is 0`() {
        val result = HomeSortType.fromValue(0)

        assertEquals(result, HomeSortType.NAME)
    }

    @Test
    fun `from value should return AMOUNT when value is 1`() {
        val result = HomeSortType.fromValue(1)

        assertEquals(result, HomeSortType.AMOUNT)
    }

    @Test
    fun `from value should return ACTIVITY when value is 2`() {
        val result = HomeSortType.fromValue(2)

        assertEquals(result, HomeSortType.ACTIVITY)
    }

    @Test
    fun `from value should return NONE when value is 3`() {
        val result = HomeSortType.fromValue(3)

        assertEquals(result, HomeSortType.NONE)
    }

    @Test
    fun `from value should return NONE when value is invalid`() {
        val result = HomeSortType.fromValue(-1)

        assertEquals(result, HomeSortType.NONE)
    }
}
