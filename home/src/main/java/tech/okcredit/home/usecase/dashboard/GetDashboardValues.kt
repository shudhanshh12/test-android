package tech.okcredit.home.usecase.dashboard

import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.home.ui.analytics.HomeEventTracker
import tech.okcredit.home.usecase.dashboard.DashboardValueProvider.Request
import tech.okcredit.home.usecase.dashboard.DashboardValueProvider.Response
import timber.log.Timber
import javax.inject.Inject

class GetDashboardValues @Inject constructor(
    private val valueProviders: Map<String, @JvmSuppressWildcards DashboardValueProvider>,
    private val tracker: Lazy<HomeEventTracker>
) {

    companion object {
        const val TAG = "GetDashboardValues"
    }

    fun execute(
        req: Map<String, Request?>
    ): Observable<HashMap<String, Response>> {
        val observableList = mutableListOf<Observable<Response>>()
        val keys = mutableListOf<String>()

        req.forEach {
            valueProviders[it.key]?.let { provider ->
                keys.add(it.key)
                observableList.add(provider.getValue(it.value))
            } ?: recordException(it.key)
        }

        return if (observableList.isNotEmpty()) {
            Observable.combineLatest(observableList) {
                hashMapOf<String, Response>().apply {
                    it.forEachIndexed { index, value ->
                        val dashboardValue = value as Response
                        if (dashboardValue.exclude.not()) {
                            this[keys[index]] = dashboardValue
                        }
                    }
                }
            }
        } else {
            Observable.create { it.onNext(hashMapOf()) }
        }
    }

    private fun recordException(requestKey: String) {
        val exception = IllegalArgumentException("$TAG: No value provider found for: $requestKey")
        Timber.e(exception)
        RecordException.recordException(exception)
        tracker.get().trackDebug(TAG, "No value provider found for: $requestKey")
    }
}
