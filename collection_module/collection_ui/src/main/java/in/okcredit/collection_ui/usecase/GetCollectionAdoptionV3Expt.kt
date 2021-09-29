package `in`.okcredit.collection_ui.usecase

import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class GetCollectionAdoptionV3Expt @Inject constructor(
    private val abRepository: Lazy<AbRepository>
) {

    companion object {
        const val EXPT_NAME = "postlogin_android-all-collection_adoption_v3"

        const val V2 = "v2"
        const val V3 = "v3"
    }

    fun execute(): Observable<Boolean> {
        return isExptEnabled().flatMap {
            getVaraint().map {
                when (it) {
                    V3 -> true
                    else -> false
                }
            }
        }
    }

    private fun isExptEnabled() = abRepository.get().isExperimentEnabled(EXPT_NAME).filter { it }

    private fun getVaraint() = abRepository.get().getExperimentVariant(EXPT_NAME)
}
