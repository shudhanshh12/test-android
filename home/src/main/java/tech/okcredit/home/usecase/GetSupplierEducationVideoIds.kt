package tech.okcredit.home.usecase

import `in`.okcredit.backend.contract.Constants
import `in`.okcredit.backend.contract.Version
import `in`.okcredit.shared.service.keyval.KeyValService
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_KEY_SERVER_VERSION
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.GsonUtil
import javax.inject.Inject

class GetSupplierEducationVideoIds @Inject constructor(
    private val keyValService: KeyValService
) {
    fun execute(): Observable<Pair<String, String>> {
        return keyValService.contains(PREF_INDIVIDUAL_KEY_SERVER_VERSION, Scope.Individual)
            .filter { it }
            .flatMapObservable { keyValService[PREF_INDIVIDUAL_KEY_SERVER_VERSION, Scope.Individual] }
            .map {
                val version = GsonUtil.getGson().fromJson(it, Version::class.java)
                return@map if (
                    version.scEducationVideo1.isNotNullOrBlank() &&
                    version.scEducationVideo2.isNotNullOrBlank()
                ) {

                    Pair(version.scEducationVideo1, version.scEducationVideo2)
                } else {
                    Pair(
                        Constants.DEFAULT_SUPPLIER_TUTORIAL_INTRO_VIDEO,
                        Constants.DEFAULT_SUPPLIER_TUTORIAL_INTRO_VIDEO
                    )
                }
            }
    }
}
