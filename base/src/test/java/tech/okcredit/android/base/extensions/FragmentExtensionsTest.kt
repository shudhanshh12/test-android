package tech.okcredit.android.base.extensions

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.launchFragmentInContainer
import com.google.common.truth.Truth
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tech.okcredit.android.base.crashlytics.RecordException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class FragmentExtensionsTest {

    internal class DummyClass {
        fun doSomething() {}
    }

    private lateinit var recordExceptionSlot: CapturingSlot<Exception>
    private val dummyClassMockObject: DummyClass = mockk()

    @Before
    fun setUp() {
        mockkStatic(RecordException::class)
        recordExceptionSlot = slot()
        every { RecordException.recordException(capture(recordExceptionSlot)) } returns Unit

        every { dummyClassMockObject.doSomething() } returns Unit
    }

    @Test
    fun `test executeIfFragmentViewAvailable extension when fragment viewLifecycleOwner is null`() {
        val fragmentArgs = Bundle()
        val factory = FragmentFactory()
        val scenario = launchFragmentInContainer<Fragment>(
            fragmentArgs = fragmentArgs,
            factory = factory
        )
        scenario.onFragment { fragment ->
            fragment.executeIfFragmentViewAvailable { dummyClassMockObject.doSomething() }
            verify(inverse = true) { dummyClassMockObject.doSomething() }
            verify(exactly = 1) { RecordException.recordException(recordExceptionSlot.captured) }
            Truth.assertThat(recordExceptionSlot.captured).isInstanceOf(IllegalStateException::class.java)
            Truth.assertThat(recordExceptionSlot.captured.message).isEqualTo(
                "Can't access the Fragment View's LifecycleOwner when " +
                    "getView() is null i.e., before onCreateView() or after onDestroyView()" +
                    " - for Fragment -> ${fragment.classType}"
            )
        }
    }

    @Test
    fun `test executeIfFragmentViewAvailable extension when fragment viewLifecycleOwner is not null`() {
        val fragmentArgs = Bundle()
        val factory = object : FragmentFactory() {
            override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                return Fragment(android.R.layout.simple_expandable_list_item_1)
            }
        }

        val scenario = launchFragmentInContainer<Fragment>(
            fragmentArgs = fragmentArgs,
            factory = factory
        )
        scenario.onFragment { fragment ->
            fragment.executeIfFragmentViewAvailable { dummyClassMockObject.doSomething() }
            verify(exactly = 1) { dummyClassMockObject.doSomething() }
            verify(inverse = true) { RecordException.recordException(any()) }
        }
    }
}
