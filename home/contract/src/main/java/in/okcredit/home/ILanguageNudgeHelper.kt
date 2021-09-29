package `in`.okcredit.home

interface ILanguageNudgeHelper {
    // output :
    // Do you want to change language to
    // Hindi (हिंदी)?  Yes, change now
    fun getEnglishWithAutoLangText(lang: String): String

    // output :
    // क्या आप भाषा को हिंदी में बदलना चाहते हैं| हां, अब बदलो
    fun getAutoLangText(lang: String): String
    fun mappedLang(): Map<String, String>
    fun mappedString(): Map<String, String>
}
