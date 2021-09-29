package `in`.okcredit.cashback.datasource.remoteConfig

interface CashbackRemoteConfigSource {

    fun getTtlForLocalCache(): Long

    fun getCashbackRewardRequestRetryInterval(): Long

    fun getCashbackRewardRequestTimeLimit(): Long
}
