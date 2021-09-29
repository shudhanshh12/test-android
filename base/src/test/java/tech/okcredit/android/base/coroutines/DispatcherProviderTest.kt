package tech.okcredit.android.base.coroutines

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import org.junit.Test

class DispatcherProviderTest {

    @Test
    fun `should return main dispatcher`() {
        val dispatcherProvider = DispatcherProvider()

        val result = dispatcherProvider.main()

        assertThat(result).isEqualTo(Dispatchers.Main)
    }

    @Test
    fun `should return io dispatcher`() {
        val dispatcherProvider = DispatcherProvider()

        val result = dispatcherProvider.io()

        assertThat(result).isEqualTo(Dispatchers.IO)
    }
}
