package tech.okcredit.home.usecase

import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class IsMenuOnBottomNavigationEnabled @Inject constructor(
    private val ab: Lazy<AbRepository>
) {

    companion object {
        const val BOTTOM_MENU_EXPERIMENT = "activation_android-all-bottom_menu"

        enum class BottomMenuExperimentVarient(val value: String) {
            BOTTOM_MENU("bottom_menu"),
            CONTROL("control")
        }
    }

    fun execute(): Observable<BottomMenuExperimentVarient> {
        return ab.get().isExperimentEnabled(BOTTOM_MENU_EXPERIMENT).flatMap { isExperimentEnabled ->
            if (isExperimentEnabled) {
                getVariant()
            } else {
                Observable.just(BottomMenuExperimentVarient.CONTROL)
            }
        }
    }

    fun getVariant(): Observable<BottomMenuExperimentVarient> {
        return ab.get().getExperimentVariant(BOTTOM_MENU_EXPERIMENT).map {
            return@map when (it) {
                BottomMenuExperimentVarient.BOTTOM_MENU.value -> BottomMenuExperimentVarient.BOTTOM_MENU
                BottomMenuExperimentVarient.CONTROL.value -> BottomMenuExperimentVarient.CONTROL
                else -> BottomMenuExperimentVarient.CONTROL
            }
        }
    }
}
