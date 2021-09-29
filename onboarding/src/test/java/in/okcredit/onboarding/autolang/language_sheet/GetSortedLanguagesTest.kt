package `in`.okcredit.onboarding.autolang.language_sheet

import `in`.okcredit.onboarding.contract.OnboardingRepo
import `in`.okcredit.onboarding.contract.autolang.Language
import `in`.okcredit.onboarding.language.usecase.GetLanguages
import `in`.okcredit.onboarding.language.usecase.GetSortedLanguages
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class GetSortedLanguagesTest {

    private val mockGetLanguages: GetLanguages = mock(stubOnly = true)
    private val mockOnboardingRepo: OnboardingRepo = mock()

    private val getSortedLanguages = GetSortedLanguages(
        Lazy { mockGetLanguages },
        Lazy { mockOnboardingRepo }
    )

    /* Tests
    *
    * when sorting languages where selected and english are not in list, then returns same order
    * when sorting languages where english is not at 0, reorders it at 0, and selected at 1
    * when sorting languages where english is missing, reorders selected at 0
    *
    * when sorting languages where ip based state is AP and selected in not an AP language then sorts accordingly
    * when sorting languages where ip based state is AP and selected is an AP language then sorts accordingly
    * when sorting languages where ip based state is not found in supported regions then sorts accordingly
    *
    * when sorting languages where fetching ip based state takes longer than timeout, doesnt sort by regional languages
    *
    * */

    @Test
    fun `when sorting languages where selected and english are not in list, then returns same order`() {
        runBlocking {
            val fakeLanguagesList = listOf(fakeHindi, fakeMarathi, fakeHinglish)
            whenever(mockGetLanguages.execute()).thenReturn(fakeLanguagesList)
            whenever(mockOnboardingRepo.getIpBasedStateCode()).thenReturn("")

            val result = getSortedLanguages.execute(fakeGujarati.languageCode)

            Assert.assertEquals(fakeLanguagesList, result)
        }
    }

    @Test
    fun `when sorting languages where english is not at 0, reorders it at 0, and selected at 1`() {
        runBlocking {
            val fakeLanguagesList = listOf(fakeHindi, fakeMarathi, fakeHinglish, fakeEnglish)
            whenever(mockGetLanguages.execute()).thenReturn(fakeLanguagesList)
            whenever(mockOnboardingRepo.getIpBasedStateCode()).thenReturn("")

            val result = getSortedLanguages.execute(fakeMarathi.languageCode)

            Assert.assertEquals(listOf(fakeEnglish, fakeMarathi, fakeHindi, fakeHinglish), result)
        }
    }

    @Test
    fun `when sorting languages where english is missing, reorders selected at 0`() {
        runBlocking {
            val fakeLanguagesList = listOf(fakeHindi, fakeMarathi, fakeHinglish)
            whenever(mockGetLanguages.execute()).thenReturn(fakeLanguagesList)
            whenever(mockOnboardingRepo.getIpBasedStateCode()).thenReturn("")

            val result = getSortedLanguages.execute(fakeMarathi.languageCode)

            Assert.assertEquals(listOf(fakeMarathi, fakeHindi, fakeHinglish), result)
        }
    }

    @Test
    fun `when sorting languages where ip based state is AP and selected in not an AP language then sorts accordingly`() {
        runBlocking {
            val fakeLanguagesList = listOf(fakeEnglish, fakeHindi, fakeMarathi, fakeTamil, fakeTelugu)
            whenever(mockGetLanguages.execute()).thenReturn(fakeLanguagesList)
            whenever(mockOnboardingRepo.getIpBasedStateCode()).thenReturn(AP_STATE_CODE)

            val result = getSortedLanguages.execute(fakeMarathi.languageCode)

            Assert.assertEquals(listOf(fakeEnglish, fakeMarathi, fakeTelugu, fakeTamil, fakeHindi), result)
        }
    }

    @Test
    fun `when sorting languages where ip based state is AP and selected is an AP language then sorts accordingly`() {
        runBlocking {
            val fakeLanguagesList = listOf(fakeEnglish, fakeHindi, fakeMarathi, fakeTamil, fakeTelugu)
            whenever(mockGetLanguages.execute()).thenReturn(fakeLanguagesList)
            whenever(mockOnboardingRepo.getIpBasedStateCode()).thenReturn(AP_STATE_CODE)

            val result = getSortedLanguages.execute(fakeTamil.languageCode)

            Assert.assertEquals(listOf(fakeEnglish, fakeTamil, fakeTelugu, fakeHindi, fakeMarathi), result)
        }
    }

    @Test
    fun `when sorting languages where ip based state is not found in supported regions then sorts accordingly`() {
        runBlocking {
            val fakeLanguagesList = listOf(fakeEnglish, fakeHindi, fakeMarathi, fakeTamil, fakeTelugu)
            whenever(mockGetLanguages.execute()).thenReturn(fakeLanguagesList)
            whenever(mockOnboardingRepo.getIpBasedStateCode()).thenReturn(INVALID_STATE_CODE)

            val result = getSortedLanguages.execute(fakeMarathi.languageCode)

            Assert.assertEquals(listOf(fakeEnglish, fakeMarathi, fakeHindi, fakeTamil, fakeTelugu), result)
        }
    }

    @Test
    fun `when sorting languages where fetching ip based state takes longer than timeout, doesnt sort by regional languages`() {
//        // Todo : Upgrade test lib to support coroutine testing with timeouts. Blocker: Kotlin version to 1.4.x
//        runBlocking {
//            val fakeLanguagesList = listOf(fakeEnglish, fakeHindi, fakeMarathi, fakeTamil, fakeTelugu)
//            whenever(mockGetLanguages.build()).thenReturn(fakeLanguagesList)
//            // Todo : Mock timeout
//            whenever(mockOnboardingRepo.getIpBasedStateCode()).thenThrow(CancellationException())
//
//            val result = getSortedLanguages.getSortedLanguages(fakeMarathi.languageCode)
//
//            Assert.assertEquals(listOf(fakeEnglish, fakeMarathi, fakeHindi, fakeTamil, fakeTelugu), result)
//        }
    }

    companion object {
        private const val AP_STATE_CODE = "AP"
        private const val INVALID_STATE_CODE = "KO"

//        private const val LONGER_THAN_IP_REGION_FETCH_TIMEOUT = IP_REGION_FETCH_TIMEOUT + 500

        private val fakeEnglish = fakeLanguage("en")
        private val fakeHindi = fakeLanguage("hi")
        private val fakeMarathi = fakeLanguage("mr")
        private val fakeHinglish = fakeLanguage("afh")
        private val fakeGujarati = fakeLanguage("gu")
        private val fakeTamil = fakeLanguage("ta")
        private val fakeTelugu = fakeLanguage("te")

        private fun fakeLanguage(code: String) = Language(languageCode = code)
    }
}
