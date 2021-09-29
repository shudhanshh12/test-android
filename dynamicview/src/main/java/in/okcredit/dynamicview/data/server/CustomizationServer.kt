package `in`.okcredit.dynamicview.data.server

import `in`.okcredit.dynamicview.BuildConfig
import `in`.okcredit.dynamicview.data.model.Customization
import dagger.Lazy
import tech.okcredit.android.base.language.LocaleManager
import javax.inject.Inject

class CustomizationServer @Inject constructor(
    private val localeManager: Lazy<LocaleManager>,
    private val apiService: Lazy<CustomizationApiService>,
) {

    suspend fun getCustomizations(businessId: String): List<Customization> {
        return apiService.get().listCustomizations(
            GetCustomizationRequest(
                BuildConfig.VERSION_CODE,
                localeManager.get().getLanguage()
            ),
            businessId
        )
    }
}
