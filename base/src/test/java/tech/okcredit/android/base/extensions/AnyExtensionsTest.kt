package tech.okcredit.android.base.extensions

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test

class AnyExtensionsTest {

    @Test
    fun `ifLet() when all 3 arguments non-null should execute block`() {
        val value1 = "lorem"
        val value2 = 3
        val value3: List<Int> = emptyList()
        var isLambdaCalled = false
        val lambda = { arg1: String, arg2: Int, arg3: List<Int> ->
            isLambdaCalled = true
            assertEquals(value1, arg1)
            assertEquals(value2, arg2)
            assertEquals(value3, arg3)
        }

        ifLet(value1, value2, value3, lambda)

        assertEquals(true, isLambdaCalled)
    }

    @Test
    fun `ifLet() when any of 3 arguments is null should not execute block`() {
        val value1 = "lorem"
        val value2: Int? = null
        val value3: List<Int> = emptyList()
        var isLambdaCalled = false
        val lambda = { arg1: String, arg2: Int, arg3: List<Int> ->
            isLambdaCalled = true
            assertEquals(value1, arg1)
            assertEquals(value2, arg2)
            assertEquals(value3, arg3)
        }

        ifLet(value1, value2, value3, lambda)

        assertEquals(false, isLambdaCalled)
    }

    @Test
    fun `ifLet() when both arguments non-null should execute block`() {
        val value1 = "lorem"
        val value2 = 3
        var isLambdaCalled = false
        val lambda = { arg1: String, arg2: Int ->
            isLambdaCalled = true
            assertEquals(value1, arg1)
            assertEquals(value2, arg2)
        }

        ifLet(value1, value2, lambda)

        assertEquals(true, isLambdaCalled)
    }

    @Test
    fun `ifLet() when any of 2 arguments is null should not execute block`() {
        val value1 = "lorem"
        val value2: Int? = null
        var isLambdaCalled = false
        val lambda = { arg1: String, arg2: Int ->
            isLambdaCalled = true
            assertEquals(value1, arg1)
            assertEquals(value2, arg2)
        }

        ifLet(value1, value2, lambda)

        assertEquals(false, isLambdaCalled)
    }

    @Test
    fun `should return array list`() {
        val list = listOf(1, 2, 3)
        val arrayList = ArrayList(list)

        assertEquals(arrayList, list.toArrayList())
    }

    @Test
    fun `should return simple name of class 1`() {
        val name = "String"
        val obj = "lorem"

        assertEquals(name, obj.classType)
    }

    @Test
    fun `should return simple name of class 2`() {
        val name = "Long"
        val obj = 3L

        assertEquals(name, obj.classType)
    }

    @Test
    fun `should return string starting with simple name of class 1`() {
        val name = "String"
        val obj = "lorem"

        assert(obj.className.startsWith(name))
    }

    @Test
    fun `should return string starting with simple name of class 2`() {
        val name = "Long"
        val obj = 3L

        assert(obj.className.startsWith(name))
    }

    @Test
    fun `should return same value if text is not null or blank`() {
        val given = "Everybody Lies!"
        val result = given.ifNullOrBlank { "Default" }

        assertThat(result).isEqualTo(given)
    }

    @Test
    fun `should return same value if text is null`() {
        val given = null
        val result = given.ifNullOrBlank { "Default" }

        assertThat(result).isEqualTo("Default")
    }

    @Test
    fun `should return same value if text is blank`() {
        val given = "    " // blank
        val result = given.ifNullOrBlank { "Default" }

        assertThat(result).isEqualTo("Default")
    }
}
