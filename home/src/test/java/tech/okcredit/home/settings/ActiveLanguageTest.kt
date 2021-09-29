package tech.okcredit.home.settings

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.home.R
import tech.okcredit.home.ui.settings.usecase.ActiveLanguage

class ActiveLanguageTest {
    private lateinit var activeLanguage: ActiveLanguage
    private val localManager: LocaleManager = mock()

    @Before
    fun setUp() {
        activeLanguage = ActiveLanguage(localeManager = Lazy { localManager })
    }

    @Test
    fun `testExecute`() {
        // given
        whenever(localManager.getLanguage()).thenReturn("ta")
        // when
        val result = activeLanguage.execute().test()
        // then
        result.assertValue(R.string.language_tamil)
        result.dispose()
    }

    @Test
    fun `test execute localManager return empty`() {
        // given
        whenever(localManager.getLanguage()).thenReturn("")

        val result = activeLanguage.execute().test()

        result.assertValue(R.string.language_english)
        result.dispose()
    }
}
