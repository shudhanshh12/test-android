package tech.okcredit.web.web_interfaces

interface WebViewDataLayerCallbackListener {

    fun isFeatureEnabled(feature: String): Boolean

    fun isExperimentEnabled(experiment: String): Boolean

    fun getExperimentVariant(experiment: String): String

    fun getVariantConfigurations(experiment: String): String

    fun getMerchantId(): String

    fun getAuthToken(): String

    fun getAndroidVersionCode(): String

    fun getContacts(): String

    fun getLanguage(): String

    fun getMixpanelProps(): String

    fun syncDynamicComponent()
}
