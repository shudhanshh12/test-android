package tech.okcredit.android.base.error

import com.google.common.truth.Truth.assertThat
import io.reactivex.exceptions.CompositeException
import org.junit.Test

class ErrorTest {

    @Test
    fun `check() returns true if throwable matches given type`() {
        val t = RuntimeException("some random error")
        assertThat(t.check<RuntimeException>()).isTrue()
    }

    @Test
    fun `check() returns true if any throwable in the stacktrace matches given type`() {
        val t = RuntimeException(IllegalStateException(RuntimeException("some random error")))
        assertThat(t.check<IllegalStateException>()).isTrue()
    }

    @Test
    fun `check() returns false if throwable is not present in the stacktrace`() {
        val t = RuntimeException(IllegalStateException(RuntimeException("some random error")))
        assertThat(t.check<IllegalArgumentException>()).isFalse()
    }

    @Test
    fun `extract() returns error if it is present in exception list of CompositeException`() {
        val err = IllegalArgumentException("abc")
        val t = CompositeException(
            RuntimeException(IllegalStateException(RuntimeException("some random error"))),
            err
        )
        assertThat(t.extract<IllegalArgumentException>()).hasMessageThat().isEqualTo(err.message)
    }
}
