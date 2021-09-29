package `in`.okcredit.onboarding.sdk

import `in`.okcredit.merchant.device.DeviceRepository
import `in`.okcredit.onboarding.contract.OnboardingRepo
import `in`.okcredit.onboarding.data.OnboardingPreferencesImpl
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class OnboardingRepoImpl @Inject constructor(
    private val onboardingPreferences: Lazy<OnboardingPreferencesImpl>,
    private val deviceRepository: Lazy<DeviceRepository>,
) : OnboardingRepo {

    override suspend fun clearPreferences() {
        onboardingPreferences.get().clearData()
    }

    override fun setIsFreshLogin(value: Boolean) {
        onboardingPreferences.get().setIsFreshLogin(value)
    }

    override suspend fun getIsFreshLogin(): Boolean {
        return onboardingPreferences.get().getIsFreshLogin()
    }

    override fun getIsNewUser(): Boolean {
        return onboardingPreferences.get().isNewUser()
    }

    override suspend fun getIpBasedStateCode(): String {
        return deviceRepository.get().getIpRegion()
    }

    override suspend fun setSuspectUserIsSupplier(value: Boolean) {
        onboardingPreferences.get().setSuspectUserIsSupplier(value)
    }

    override suspend fun getSuspectUserIsSupplier(): Boolean {
        return onboardingPreferences.get().getSuspectUserIsSupplier()
    }

    override suspend fun getPayablesOnboardingVariant(): String {
        return onboardingPreferences.get().getPayablesOnboardingVariant()
    }

    override suspend fun setPayablesOnboardingVariant(value: String) {
        onboardingPreferences.get().setPayablesOnboardingVariant(value)
    }

    override suspend fun getVisibilityPreNetworkOnboardingNudge(): Boolean {
        return onboardingPreferences.get().getVisibilityPreNetworkOnboardingNudge()
    }

    override suspend fun setVisibilityPreNetworkOnboardingNudge(value: Boolean) {
        onboardingPreferences.get().setVisibilityPreNetworkOnboardingNudge(value)
    }

    override suspend fun setRelationshipAddedAfterOnboarding(value: Boolean) {
        onboardingPreferences.get().setRelationshipAddedAfterOnboarding(value)
    }

    override fun getIsRelationshipAddedAfterOnboarding(): Observable<Boolean> {
        return onboardingPreferences.get().getIsRelationshipAddedAfterOnboarding()
    }

    override suspend fun savePreNetworkRelationships(preNetworkRelationships: List<String>) {
        onboardingPreferences.get().savePreNetworkRelationships(preNetworkRelationships)
    }

    override suspend fun getPreNetworkRelationships(): List<String> {
        return onboardingPreferences.get().getPreNetworkRelationships()
    }

    override suspend fun setPreNetworkSupplierCount(value: Long) {
        return onboardingPreferences.get().setPreNetworkSupplierCount(value)
    }

    override suspend fun getPreNetworkSupplierCount(): String {
        return onboardingPreferences.get().getPreNetworkSupplierCount()
    }

    override suspend fun setPreNetworkCustomerCount(value: Long) {
        return onboardingPreferences.get().setPreNetworkCustomerCount(value)
    }

    override suspend fun getPreNetworkCustomerCount(): String {
        return onboardingPreferences.get().getPreNetworkCustomerCount()
    }
}
