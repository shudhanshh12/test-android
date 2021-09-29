package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.backend.contract.Version
import `in`.okcredit.collection.contract.CollectionServerErrors
import `in`.okcredit.shared.service.keyval.KeyValService
import com.google.gson.Gson
import io.reactivex.Observable
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_KEY_SERVER_VERSION
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class GetCollectionAdoptionPopupOnBoardingVideo @Inject constructor(private val keyValService: KeyValService) {

    fun execute(): Observable<String> {
        return keyValService.contains(PREF_INDIVIDUAL_KEY_SERVER_VERSION, Scope.Individual)
            .filter {
                it
            }
            .flatMapObservable {
                keyValService[PREF_INDIVIDUAL_KEY_SERVER_VERSION, Scope.Individual]
                    .flatMap {
                        val version = Gson().fromJson(it, Version::class.java)
                        if (version.caEducationVideo1.isNullOrBlank().not()) {
                            Observable.just(version.caEducationVideo1)
                        } else {
                            throw CollectionServerErrors.VideoNotFoundException()
                        }
                    }
            }
    }
}
