package tech.okcredit.home.ui.sidemenu.usecacse

import dagger.Lazy
import dagger.Reusable
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

@Reusable
class ShouldShowCallCustomerCare @Inject constructor(
    private val abRepository: Lazy<AbRepository>,
) {

    companion object {
        private const val FEATURE_CALL_SUPPORT = "phone_support"
    }

    fun execute(): Observable<Boolean> = abRepository.get().isFeatureEnabled(FEATURE_CALL_SUPPORT)
}
