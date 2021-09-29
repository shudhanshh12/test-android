package `in`.okcredit.onboarding.contract.marketing

interface AppsFlyerHelper {

    suspend fun setAuthSuccess(isAuthSignup: Boolean)

    suspend fun setPreProcessedAppsflyerData(appsFlyerData: Map<String, Any?>)

    fun toAppsFlyerData(data: Map<String, Any?>): MutableMap<String, String>?

    fun getInstallRefererData(data: String): MutableMap<String, String>
}
