package tech.okcredit.android.base.workmanager

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import java.util.concurrent.TimeUnit

data class WorkerConfig(
    val trackingEnabled: Boolean = true,
    val allowUnlimitedRun: Boolean = false,
    val maxAttemptCount: Int = FirebaseRemoteConfig.getInstance().getLong(WORKER_MAXIMUM_ATTEMPT_KEY).toInt(),
) {

    companion object {
        const val WORKER_MAXIMUM_ATTEMPT_KEY = "worker_maximum_attempt"
    }
}

/**
 * [RateLimit] can be used to limit the number of times a worker is scheduled
 *
 * Eg. If the worker should run once in 3 days, the rate limit should be : RateLimit(3, TimeUnit.DAYS)
 */
data class RateLimit(
    val limit: Long,
    val unit: TimeUnit,
) {
    companion object {
        const val KEY_POSTFIX_LAST_TRIGGERED = "_last_triggered"
        const val FRC_KEY_NON_CRITICAL_DATA_WORKER_RATE_LIMIT_HOURS = "non_critical_data_worker_rate_limit_hours"
    }

    val millis
        get() = TimeUnit.MILLISECONDS.convert(limit, unit)
}
