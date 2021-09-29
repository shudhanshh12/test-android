package tech.okcredit.web.web_interfaces

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.Lazy
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import merchant.okcredit.dynamicview.contract.SyncDynamicComponent
import org.json.JSONObject
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.auth.AccessTokenProvider
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.web.BuildConfig
import javax.inject.Inject

class WebViewDataLayerCallbackListenerImpl @Inject constructor(
    private val ab: Lazy<AbRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val tokenProvider: Lazy<AccessTokenProvider>,
    private val contactsRepository: Lazy<ContactsRepository>,
    private val context: Lazy<Context>,
    private val mixpanelApi: Lazy<MixpanelAPI>,
    private val syncDynamicComponent: Lazy<SyncDynamicComponent>,
    private val schedulerProvider: Lazy<SchedulerProvider>,
) : WebViewDataLayerCallbackListener {

    override fun isFeatureEnabled(feature: String): Boolean {
        return ab.get().isFeatureEnabled(feature)
            .onErrorReturnItem(false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(ThreadUtils.newThread())
            .blockingFirst()
    }

    override fun isExperimentEnabled(experiment: String): Boolean {
        return ab.get().isExperimentEnabled(experiment)
            .onErrorReturnItem(false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(ThreadUtils.newThread())
            .blockingFirst()
    }

    override fun getExperimentVariant(experiment: String): String {
        return ab.get().getExperimentVariant(experiment)
            .onErrorReturnItem("")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(ThreadUtils.newThread())
            .blockingFirst().toString()
    }

    override fun getVariantConfigurations(experiment: String): String {
        return ab.get().getVariantConfigurations(experiment)
            .onErrorReturnItem(mapOf())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(ThreadUtils.newThread())
            .blockingFirst().toString()
    }

    override fun getMerchantId(): String {
        return getActiveBusinessId.get().execute()
            .onErrorReturnItem("")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(ThreadUtils.newThread())
            .blockingGet()
    }

    override fun getAuthToken(): String {
        return Single.fromCallable { tokenProvider.get().getAccessToken() }
            .onErrorReturnItem("")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(ThreadUtils.newThread())
            .blockingGet() ?: ""
    }

    override fun getAndroidVersionCode() = BuildConfig.VERSION_CODE

    override fun getContacts(): String {
        return contactsRepository.get()
            .getContacts()
            .onErrorReturnItem(listOf())
            .subscribeOn(ThreadUtils.database())
            .observeOn(AndroidSchedulers.mainThread())
            .blockingFirst()
            .toString()
    }

    override fun getLanguage(): String {
        return LocaleManager.getLanguage(context.get())
    }

    override fun getMixpanelProps(): String {
        val userProperties: JSONObject = mixpanelApi.get().superProperties
        for ((k, v) in mixpanelApi.get().deviceInfo) {
            userProperties.put(k, v)
        }
        return userProperties.toString()
    }

    override fun syncDynamicComponent() {
        syncDynamicComponent.get().execute().subscribeOn(schedulerProvider.get().io()).subscribe()
    }
}
