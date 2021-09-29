package tech.okcredit.home.usecase.home

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.home.usecase.IsMenuOnBottomNavigationEnabled
import tech.okcredit.home.usecase.IsMenuOnBottomNavigationEnabled.Companion.BOTTOM_MENU_EXPERIMENT

class IsBottomMenuEnabledTest {
    private val ab: AbRepository = mock()
    private val isBottomMenuEnabledTest = IsMenuOnBottomNavigationEnabled({ ab })

    @Test
    fun `when experiment Enabled and bottomMenu as varient`() {
        // given
        whenever(ab.isExperimentEnabled(BOTTOM_MENU_EXPERIMENT)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(BOTTOM_MENU_EXPERIMENT)).thenReturn(Observable.just("bottom_menu"))

        // when
        val result = isBottomMenuEnabledTest.execute().test()

        // then
        result.assertValue {
            it == IsMenuOnBottomNavigationEnabled.Companion.BottomMenuExperimentVarient.BOTTOM_MENU
        }
    }

    @Test
    fun `when experiment enabled and Control as varient`() {
        // given
        whenever(ab.isExperimentEnabled(BOTTOM_MENU_EXPERIMENT)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(BOTTOM_MENU_EXPERIMENT)).thenReturn(Observable.just("control"))

        // when
        val result = isBottomMenuEnabledTest.execute().test()

        // then
        result.assertValue {
            it == IsMenuOnBottomNavigationEnabled.Companion.BottomMenuExperimentVarient.CONTROL
        }
    }

    @Test
    fun `when experiment not enabled should return control`() {
        // given
        whenever(ab.isExperimentEnabled(BOTTOM_MENU_EXPERIMENT)).thenReturn(Observable.just(false))

        // when
        val result = isBottomMenuEnabledTest.execute().test()

        // then
        result.assertValue {
            it == IsMenuOnBottomNavigationEnabled.Companion.BottomMenuExperimentVarient.CONTROL
        }
    }
}
