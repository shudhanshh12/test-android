package tech.okcredit.home.ui.payables_onboarding

import dagger.Lazy
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.home.ui.payables_onboarding.helpers.TabOrderingHelper
import javax.inject.Inject

class GetCarouselVisibility @Inject constructor(
    private val tabOrderingHelper: Lazy<TabOrderingHelper>,
) {
    fun execute() = rxSingle {
        tabOrderingHelper.get().isExperimentEnabled() ?: false
    }
}
