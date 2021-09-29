package `in`.okcredit.onboarding.autolang.language_sheet

import `in`.okcredit.onboarding.contract.autolang.Language
import `in`.okcredit.onboarding.language.usecase.GetLanguages
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Test

@Suppress("SpellCheckingInspection", "HardCodedStringLiteral")
class GetLanguagesTest {

    private val mockEnglish: Language = mock(stubOnly = true)
    private val mockHindi: Language = mock(stubOnly = true)
    private val mockMarathi: Language = mock(stubOnly = true)
    private val mockHinglish: Language = mock(stubOnly = true)
    private val mockGujarati: Language = mock(stubOnly = true)
    private val mockTamil: Language = mock(stubOnly = true)
    private val mockTelugu: Language = mock(stubOnly = true)
    private val mockPunjabi: Language = mock(stubOnly = true)
    private val mockMalayalam: Language = mock(stubOnly = true)
    private val mockKannada: Language = mock(stubOnly = true)
    private val mockBangala: Language = mock(stubOnly = true)

    private val mockGetLanguages: GetLanguages = mock()

    private val fakeLanguages = listOf(
        mockEnglish,
        mockHindi,
        mockMarathi,
        mockHinglish,
        mockGujarati,
        mockTamil,
        mockTelugu,
        mockPunjabi,
        mockMalayalam,
        mockKannada,
        mockBangala
    )

    @Test
    fun whenGetLanguagesThenReturnsLanguages() {
        whenever(mockGetLanguages.buildAppLanguageList()).thenReturn(fakeLanguages)
        whenever(mockGetLanguages.execute(Unit)).thenCallRealMethod()

        val result = mockGetLanguages.execute(Unit).test().values().last()
            as? Result.Success<List<Language>>

        Assert.assertEquals(result?.value, fakeLanguages)
    }

    @Test
    fun whenGetLanguagesThenReturnsLanguagesInOrder() {
        whenever(mockGetLanguages.buildEnglish()).thenReturn(mockEnglish)
        whenever(mockGetLanguages.buildHindi()).thenReturn(mockHindi)
        whenever(mockGetLanguages.buildMarathi()).thenReturn(mockMarathi)
        whenever(mockGetLanguages.buildHinglish()).thenReturn(mockHinglish)
        whenever(mockGetLanguages.buildGujarati()).thenReturn(mockGujarati)
        whenever(mockGetLanguages.buildTamil()).thenReturn(mockTamil)
        whenever(mockGetLanguages.buildTelugu()).thenReturn(mockTelugu)
        whenever(mockGetLanguages.buildPunjabi()).thenReturn(mockPunjabi)
        whenever(mockGetLanguages.buildMalayalam()).thenReturn(mockMalayalam)
        whenever(mockGetLanguages.buildKannada()).thenReturn(mockKannada)
        whenever(mockGetLanguages.buildBangala()).thenReturn(mockBangala)
        whenever(mockGetLanguages.buildAppLanguageList())
            .thenCallRealMethod()

        val result = mockGetLanguages.buildAppLanguageList()

        Assert.assertEquals(fakeLanguages, result)
    }
}
