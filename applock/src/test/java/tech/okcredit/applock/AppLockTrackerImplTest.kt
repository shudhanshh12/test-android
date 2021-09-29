package tech.okcredit.applock

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import tech.okcredit.applock.analytics.AppLockEventTracker

class AppLockTrackerImplTest {
    private val appLockEventTracker: AppLockEventTracker = mock()
    private val appLockTrackerImpl = AppLockTrackerImpl({ appLockEventTracker })

    @Test
    fun trackEvents() {
        // given
        val eventName = "eventName"
        val source = "source"
        val screen = "screen"

        // when
        appLockTrackerImpl.trackEvents(eventName, source, screen)

        // then
        val argumentCaptor = argumentCaptor<String>()
        verify(appLockEventTracker).trackEvents(
            argumentCaptor.capture(),
            argumentCaptor.capture(),
            argumentCaptor.capture(),
            argumentCaptor.capture(),
        )
        assertThat(argumentCaptor.firstValue).isEqualTo(eventName)
        assertThat(argumentCaptor.secondValue).isEqualTo(screen)
        assertThat(argumentCaptor.thirdValue).isEqualTo(source)
    }
}
