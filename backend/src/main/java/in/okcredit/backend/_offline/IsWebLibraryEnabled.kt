package `in`.okcredit.backend._offline

import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.utils.debug
import javax.inject.Inject

class IsWebLibraryEnabled @Inject constructor(
    private val ab: Lazy<AbRepository>
) {

    companion object {
        const val FEATURE_NAME = "web_library"
    }

    fun execute(): Observable<Boolean> {
        debug {
            return Observable.just(true)
        }
        return ab.get().isFeatureEnabled(FEATURE_NAME)
    }
}
