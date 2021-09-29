package `in`.okcredit.onboarding.contract

import io.reactivex.Observable

interface OnboardingRepo {
    suspend fun clearPreferences()

    fun setIsFreshLogin(value: Boolean)
    suspend fun getIsFreshLogin(): Boolean

    fun getIsNewUser(): Boolean

    suspend fun getIpBasedStateCode(): String

    suspend fun setSuspectUserIsSupplier(value: Boolean)
    suspend fun getSuspectUserIsSupplier(): Boolean

    suspend fun getPayablesOnboardingVariant(): String
    suspend fun setPayablesOnboardingVariant(value: String)

    suspend fun getVisibilityPreNetworkOnboardingNudge(): Boolean
    suspend fun setVisibilityPreNetworkOnboardingNudge(value: Boolean)

    fun getIsRelationshipAddedAfterOnboarding(): Observable<Boolean>
    suspend fun setRelationshipAddedAfterOnboarding(value: Boolean)

    suspend fun savePreNetworkRelationships(preNetworkRelationships: List<String>)
    suspend fun getPreNetworkRelationships(): List<String>

    suspend fun setPreNetworkSupplierCount(value: Long)
    suspend fun getPreNetworkSupplierCount(): String

    suspend fun setPreNetworkCustomerCount(value: Long)
    suspend fun getPreNetworkCustomerCount(): String
}
