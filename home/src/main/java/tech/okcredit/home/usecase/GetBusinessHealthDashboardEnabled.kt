package tech.okcredit.home.usecase

import dagger.Lazy
import dagger.Reusable
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.home.ui.activity.HomeActivityContract.Companion.FEATURE_BUSINESS_HEALTH_DASHBOARD
import tech.okcredit.home.ui.activity.HomeActivityContract.Companion.FEATURE_HOME_DASHBOARD
import javax.inject.Inject

@Reusable
class GetBusinessHealthDashboardEnabled @Inject constructor(
    private val ab: Lazy<AbRepository>,
) {
    fun execute(): Observable<Boolean> {
        return Observable.combineLatest(
            ab.get().isFeatureEnabled(FEATURE_BUSINESS_HEALTH_DASHBOARD),
            ab.get().isFeatureEnabled(FEATURE_HOME_DASHBOARD),
            { businessHealthDashboardEnabled, homeDashboardEnabled ->
                businessHealthDashboardEnabled && !homeDashboardEnabled
            }
        )
    }
}
