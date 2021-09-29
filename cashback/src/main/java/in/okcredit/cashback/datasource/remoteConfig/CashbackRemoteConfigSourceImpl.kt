package `in`.okcredit.cashback.datasource.remoteConfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import javax.inject.Inject

class CashbackRemoteConfigSourceImpl @Inject constructor(
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
) : CashbackRemoteConfigSource {

    companion object {
        private const val CASHBACK_MESSAGE_DETAILS_CACHE_TTL_KEY = "cashback_message_details_cache_ttl_hours"
        private const val CASHBACK_REWARD_REQUEST_RETRY_INTERVAL = "cashback_reward_request_retry_interval_millis"
        private const val CASHBACK_REWARD_REQUEST_TIMELIMIT = "cashback_reward_request_timelimit_millis"
    }

    override fun getTtlForLocalCache() = firebaseRemoteConfig.get().getLong(CASHBACK_MESSAGE_DETAILS_CACHE_TTL_KEY)

    override fun getCashbackRewardRequestRetryInterval() =
        firebaseRemoteConfig.get().getLong(CASHBACK_REWARD_REQUEST_RETRY_INTERVAL)

    override fun getCashbackRewardRequestTimeLimit() =
        firebaseRemoteConfig.get().getLong(CASHBACK_REWARD_REQUEST_TIMELIMIT)
}
