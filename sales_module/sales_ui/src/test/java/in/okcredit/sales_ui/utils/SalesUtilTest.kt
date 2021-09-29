package `in`.okcredit.sales_ui.utils

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Test

class SalesUtilTest {

    private class Util {
        fun currencyDisplayFormat(value: Double) {
            SalesUtil.currencyDisplayFormat(value)
        }

        fun displayDecimalNumber(value: Double) {
            SalesUtil.displayDecimalNumber(value)
        }

        fun isDecimal(value: Double) {
            SalesUtil.isDecimal(value)
        }

        fun eval(value: String) {
            SalesUtil.eval(value)
        }
    }

    @Test
    fun `display currency format`() {
        val testValue = 1000.0
        val expectedResult = "1,000"
        val util = Util()
        mockkObject(SalesUtil)
        mockkObject(SalesUtil.Companion)

        every { SalesUtil.currencyDisplayFormat(testValue) } returns expectedResult
        util.currencyDisplayFormat(testValue)
        verify { SalesUtil.currencyDisplayFormat(testValue) }
        assertEquals(expectedResult, SalesUtil.currencyDisplayFormat(testValue))
    }

    @Test
    fun `display decimal format`() {
        val testValue = 1000.0435
        val expectedResult = "1000.04"
        val util = Util()
        mockkObject(SalesUtil)
        mockkObject(SalesUtil.Companion)

        every { SalesUtil.displayDecimalNumber(testValue) } returns expectedResult
        util.displayDecimalNumber(testValue)
        verify { SalesUtil.displayDecimalNumber(testValue) }
        assertEquals(expectedResult, SalesUtil.displayDecimalNumber(testValue))
    }

    @Test
    fun `is decimal valid`() {
        val testValue = 1000.02
        val expectedResult = true
        val util = Util()
        mockkObject(SalesUtil)
        mockkObject(SalesUtil.Companion)

        every { SalesUtil.isDecimal(testValue) } returns expectedResult
        util.isDecimal(testValue)
        verify { SalesUtil.isDecimal(testValue) }
        assertEquals(expectedResult, SalesUtil.isDecimal(testValue))
    }

    @Test
    fun `eval calculation test`() {
        val testValue = "2X3"
        val expectedResult: Long = 600
        val util = Util()
        mockkObject(SalesUtil)
        mockkObject(SalesUtil.Companion)

        every { SalesUtil.eval(testValue) } returns expectedResult
        util.eval(testValue)
        verify { SalesUtil.eval(testValue) }
        assertEquals(expectedResult, SalesUtil.eval(testValue))
    }
}
