package `in`.okcredit.onboarding.language.usecase

import `in`.okcredit.onboarding.contract.OnboardingRepo
import `in`.okcredit.onboarding.contract.autolang.Language
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.*
import javax.inject.Inject

class GetSortedLanguages @Inject constructor(
    private val getLanguages: Lazy<GetLanguages>,
    private val onboardingRepo: Lazy<OnboardingRepo>
) {

    suspend fun execute(currentLanguageCode: String): List<Language> = withContext(Dispatchers.IO) {
        getLanguages.get().execute()
            .let { reorderLanguages(currentLanguageCode, it) }
    }

    private suspend fun reorderLanguages(currentLanguageCode: String, languages: List<Language>): List<Language> {

        val stateCode = withTimeoutOrNull(IP_REGION_FETCH_TIMEOUT) { onboardingRepo.get().getIpBasedStateCode() } ?: ""
        val prominentRegionalLanguageCodes = STATE_TO_PROMINENT_LANGUAGES_MAP[stateCode] ?: emptyList()
        val languageMap = languages.associateBy { it.languageCode }

        // NOTE: Optimized for readability than algorithmic complexity
        return LinkedList(languages).apply {
            // Extract all prominent languages
            val prominentLanguages = prominentRegionalLanguageCodes.mapNotNull { languageMap[it] }
            removeAll(prominentLanguages)

            // Move English to first position if found and not already at 0
            indexOfFirst { it.languageCode == ENGLISH_LANGUAGE_CODE }
                .takeIf { it > 0 }
                ?.also { add(0, removeAt(it)) }

            // Insert all prominent regional languages after English, if found
            val insertionIndex = if (get(0)?.languageCode == ENGLISH_LANGUAGE_CODE) 1 else 0
            addAll(insertionIndex, prominentLanguages)

            // skip if language is english, ie. index 0, or index 1 as add n insert on same index
            indexOfFirst { it.languageCode == currentLanguageCode }
                .takeIf { it > insertionIndex }
                ?.also { add(insertionIndex, removeAt(it)) }
        }
    }

    companion object {

        private const val IP_REGION_FETCH_TIMEOUT = 1000L
        private const val ENGLISH_LANGUAGE_CODE = "en"

        private val STATE_TO_PROMINENT_LANGUAGES_MAP = mapOf(
            "AP" to listOf("te", "ta", "afh"),
            "AR" to listOf("bn", "afh", "hi"),
            "AS" to listOf("bn", "afh", "hi"),
            "BR" to listOf("hi", "afh", "bn"),
            "CT" to listOf("hi", "afh", "bn"),
            "GA" to listOf("mr", "afh", "hi"),
            "GJ" to listOf("gu", "afh", "hi"),
            "HR" to listOf("hi", "afh", "pa", "gu"),
            "HP" to listOf("hi", "afh", "pa"),
            "JK" to listOf("hi", "afh", "pa"),
            "JH" to listOf("hi", "afh", "bn"),
            "KA" to listOf("kn", "te", "afh"),
            "KL" to listOf("ml", "ta", "afh"),
            "MP" to listOf("hi", "afh", "mr"),
            "MH" to listOf("mr", "afh", "hi"),
            "MN" to listOf("bn", "afh", "hi"),
            "ML" to listOf("bn", "afh", "hi"),
            "MZ" to listOf("bn", "afh", "hi"),
            "NL" to listOf("bn", "afh", "hi"),
            "OR" to listOf("afh", "hi", "bn"),
            "PB" to listOf("pa", "afh", "hi"),
            "RJ" to listOf("hi", "afh", "gu"),
            "SK" to listOf("bn", "afh", "hi"),
            "TN" to listOf("ta", "afh", "hi"),
            "TG" to listOf("te", "afh", "hi"),
            "TR" to listOf("bn", "afh", "hi"),
            "UT" to listOf("hi", "afh", "bn"),
            "UP" to listOf("hi", "afh", "pa"),
            "WB" to listOf("bn", "afh", "hi"),
            "AN" to listOf("ta", "bn", "afh"),
            "CH" to listOf("pa", "hi", "afh"),
            "DL" to listOf("hi", "afh", "pa"),
            "DN" to listOf("gu", "afh", "hi"),
            "DD" to listOf("gu", "afh", "hi"),
            "LD" to listOf("afh", "hi", "gu"),
            "PY" to listOf("ta", "bn", "afh")
        )
    }
}
