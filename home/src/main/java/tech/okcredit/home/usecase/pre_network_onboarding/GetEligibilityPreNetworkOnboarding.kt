package tech.okcredit.home.usecase.pre_network_onboarding

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.onboarding.contract.OnboardingRepo
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class GetEligibilityPreNetworkOnboarding @Inject constructor(
    private val supplierCreditRepo: Lazy<SupplierCreditRepository>,
    private val customerRepo: Lazy<CustomerRepo>,
    private val onboardingRepo: Lazy<OnboardingRepo>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val ab: Lazy<AbRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    companion object {
        const val EXPERIMENT_NAME = "activation_android-all-prenetwork_onboarding"
        const val VARIANT_NAME = "prenetwork"
        const val DELAY_PRE_NETWORK_TOOL_TIP_SHOWN = "delay_in_pre_network_tool_tip"
    }

    suspend fun execute(hideBigButton: Boolean): PreNetworkOnboardingState = withContext(Dispatchers.IO) {
        if (isFreshLogin() &&
            isUserFallInExperiment()
        ) {
            checkSupplierTransactionIsPresent()
        } else {
            PreNetworkOnboardingState(
                hideBigButtonAndNudge = hideBigButton
            )
        }
    }

    private suspend fun isUserFallInExperiment(): Boolean {
        return Single.zip(
            ab.get().isExperimentEnabled(EXPERIMENT_NAME).firstOrError(),
            ab.get().getExperimentVariant(EXPERIMENT_NAME).firstOrError(),
            { experimentEnabled, experimentVariant ->
                experimentEnabled && experimentVariant.equals(VARIANT_NAME, true)
            }
        ).await()
    }

    data class PreNetworkOnboardingState(
        val eligibleForNudges: Boolean = false,
        val isPreNetworkUser: Boolean = false,
        val hideBigButtonAndNudge: Boolean,
        val delayInToolTipShown: Long = 0,
    )

    private suspend fun checkSupplierTransactionIsPresent() = withContext(Dispatchers.IO) {

        val delayInToolTipShown = firebaseRemoteConfig.get().getLong(DELAY_PRE_NETWORK_TOOL_TIP_SHOWN) * 1000
        val businessId = getActiveBusinessId.get().execute().await()

        val isPreNetworkCustomerCount = getPreNetworkCustomerCount(businessId)
        val isPreNetworkSupplierCount = getPreNetworkSupplierCount(businessId)

        val checkAnyRelationshipAdded = checkAnyRelationshipIsAdded()

        // getting if nudge is already shown
        val preNetworkOnboardingNudgeShown = onboardingRepo.get().getVisibilityPreNetworkOnboardingNudge()

        val isPreNetworkUser = (isPreNetworkCustomerCount > 0 || isPreNetworkSupplierCount > 0) &&
            !checkAnyRelationshipAdded

        val ifAlreadyPreNetworkRelationshipSaved = onboardingRepo.get().getPreNetworkRelationships().isNotEmpty()

        // saving PreNetworkRelationships for the first time visit home
        if (isPreNetworkUser &&
            !ifAlreadyPreNetworkRelationshipSaved
        ) {
            savePreNetworkRelationships(businessId)
        }

        // canShowNudges for preNetworkOnboarding Nudges
        val eligibleForPreNetworkOnboardingNudges = isPreNetworkSupplierCount > 0 &&
            !preNetworkOnboardingNudgeShown && !checkAnyRelationshipAdded

        PreNetworkOnboardingState(
            eligibleForPreNetworkOnboardingNudges,
            isPreNetworkUser = isPreNetworkUser,
            hideBigButtonAndNudge = checkAnyRelationshipAdded,
            delayInToolTipShown = delayInToolTipShown
        )
    }

    private suspend fun getPreNetworkCustomerCount(businessId: String): Long {
        return if (onboardingRepo.get().getPreNetworkCustomerCount().isEmpty()) {
            val customerCount = customerRepo.get()
                .getActiveCustomerCount(businessId)
                .firstOrError()
                .await()
            onboardingRepo.get().setPreNetworkCustomerCount(customerCount)
            customerCount
        } else {
            onboardingRepo.get().getPreNetworkCustomerCount().toLong()
        }
    }

    private suspend fun getPreNetworkSupplierCount(businessId: String): Long {
        return if (onboardingRepo.get().getPreNetworkSupplierCount().isEmpty()) {
            val supplierCount = supplierCreditRepo.get()
                .getActiveSuppliersCount(businessId)
                .first()
            onboardingRepo.get().setPreNetworkSupplierCount(supplierCount)
            supplierCount
        } else {
            onboardingRepo.get().getPreNetworkSupplierCount().toLong()
        }
    }

    private suspend fun savePreNetworkRelationships(businessId: String) {
        val preNetworkActiveSupplierIds = supplierCreditRepo.get()
            .listActiveSuppliersIds(businessId)
            .firstOrError()
            .await()

        val preNetworkActiveCustomerIds = customerRepo.get()
            .listActiveCustomersIds(businessId)
            .firstOrError()
            .await()

        val preNetworkRelationships = mutableListOf<String>().apply {
            addAll(preNetworkActiveSupplierIds)
            addAll(preNetworkActiveCustomerIds)
        }
        onboardingRepo.get().savePreNetworkRelationships(preNetworkRelationships)
    }

    private fun isFreshLogin() = onboardingRepo.get().getIsNewUser()

    private suspend fun checkAnyRelationshipIsAdded(): Boolean {
        return onboardingRepo.get().getIsRelationshipAddedAfterOnboarding().firstOrError().await()
    }
}
