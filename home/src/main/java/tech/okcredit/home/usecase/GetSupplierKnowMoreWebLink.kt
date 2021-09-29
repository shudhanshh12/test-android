package tech.okcredit.home.usecase

import `in`.okcredit.backend.contract.Version
import `in`.okcredit.shared.service.keyval.KeyValService
import com.google.gson.Gson
import io.reactivex.Observable
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_KEY_SERVER_VERSION
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class GetSupplierKnowMoreWebLink @Inject constructor(
    private val keyValService: KeyValService,
    private val localeManager: LocaleManager,
) {
    fun execute(): Observable<String> {
        return keyValService.contains(PREF_INDIVIDUAL_KEY_SERVER_VERSION, Scope.Individual)
            .filter { it }
            .flatMapObservable {
                keyValService[PREF_INDIVIDUAL_KEY_SERVER_VERSION, Scope.Individual]
                    .map {
                        val version = Gson().fromJson(it, Version::class.java)
                        version.supplierLearnMoreWebLink + "?lng=" + localeManager.getLanguage()
                    }
            }.distinctUntilChanged()
    }
}
