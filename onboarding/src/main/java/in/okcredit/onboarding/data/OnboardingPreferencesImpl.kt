package `in`.okcredit.onboarding.data

import `in`.okcredit.onboarding.contract.OnboardingPreferences
import `in`.okcredit.onboarding.contract.OnboardingPreferences.Companion.KEY_APP_LOCK_ENABLED
import `in`.okcredit.onboarding.contract.OnboardingPreferences.Companion.KEY_INAPP_APP_LOCK_CANCELLED
import `in`.okcredit.onboarding.contract.OnboardingPreferences.Companion.KEY_NEW_USER
import android.content.Context
import com.google.gson.Gson
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.extensions.fromJson
import tech.okcredit.android.base.extensions.itOrBlank
import tech.okcredit.android.base.preferences.*
import javax.inject.Inject

@Reusable
class OnboardingPreferencesImpl @Inject constructor(
    context: Context,
    private val defaultPreferences: Lazy<DefaultPreferences>,
    migrations: Lazy<Migrations>,
    sharedPreferencesMigrationLogger: Lazy<SharedPreferencesMigrationHandler.Logger>,
) : OkcSharedPreferences(
    context = context,
    prefName = SHARED_PREF_NAME,
    version = SHARED_PREF_VERSION,
    migrationList = migrations.get().getList(),
    sharedPreferencesMigrationLogger = sharedPreferencesMigrationLogger,
),
    OnboardingPreferences {

    companion object {
        private const val SHARED_PREF_NAME = "onboarding"
        private const val SHARED_PREF_VERSION = 1

        const val PREF_INDIVIDUAL_KEY_APP_RESUME_SESSION = "KEY_APP_RESUME_SESSION"
        const val PREF_INDIVIDUAL_KEY_EXITING_USER_ENABLED_APP_LOCK = "KEY_EXITING_USER_ENABLED_APP_LOCK"

        const val PREF_INDIVIDUAL_KEY_NAME_SKIPPED = "KEY_NAME_SKIPPED"
        const val PREF_INDIVIDUAL_KEY_NAME_ENTERED = "KEY_NAME_ENTERED"

        const val PREF_INDIVIDUAL_USER_SELECTED_LANGUAGE = "user_selected_language"

        const val PREF_INDIVIDUAL_FRESH_LOGIN = "fresh_login"

        const val PREF_INDIVIDUAL_PAYABLES_ONBOARDING_SUSPECT_USER_IS_SUPPLIER =
            "payables_onboarding_suspect_user_is_supplier"
        const val PREF_INDIVIDUAL_PAYABLES_ONBOARDING_VARIANT = "payables_onboarding_variant"

        const val PREF_INDIVIDUAL_APPSFLYER_MARKETING_IS_SIGN_UP = "appsflyer_marketing_is_sign_up"
        const val PREF_INDIVIDUAL_APPSFLYER_MARKETING_AUTH_TIME = "appsflyer_marketing_auth_time"

        const val PREF_PRE_NETWORK_ONBOARDING_NUDGE = "pre_network_onboarding_nudge"
        const val PREF_IS_RELATIONSHIP_ADDED = "is_relationship_added"
        const val PREF_LIST_PRE_NETWORK_RELATIONSHIPS = "pre_network_relationships"
        const val PREF_PRE_NETWORK_CUSTOMER_COUNT = "pre_network_customer_count"
        const val PREF_PRE_NETWORK_SUPPLIER_COUNT = "pre_network_supplier_count"
    }

    class Migrations @Inject constructor() {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration.emptyMigration(0, 1)
    }

    private val gson = Gson()

    // TODO move it to AppLockHelper
    override fun isNewUser() =
        blockingGetBoolean(
            KEY_NEW_USER,
            Scope.Individual,
            defaultPreferences.get().blockingGetBoolean(KEY_NEW_USER, Scope.Individual)
        )

    // TODO move it to AppLockHelper
    fun existingUserEnabledAppLock() =
        blockingGetBoolean(
            PREF_INDIVIDUAL_KEY_EXITING_USER_ENABLED_APP_LOCK,
            Scope.Individual,
            defaultPreferences.get()
                .blockingGetBoolean(PREF_INDIVIDUAL_KEY_EXITING_USER_ENABLED_APP_LOCK, Scope.Individual)
        )

    // TODO move it to OnboardingHelper
    fun hasSkippedNameScreen() =
        blockingGetBoolean(
            PREF_INDIVIDUAL_KEY_NAME_SKIPPED,
            Scope.Individual,
            defaultPreferences.get().blockingGetBoolean(PREF_INDIVIDUAL_KEY_NAME_SKIPPED, Scope.Individual)
        )

    // TODO move it to OnboardingHelper
    suspend fun setSkippedNameScreen(value: Boolean) = set(PREF_INDIVIDUAL_KEY_NAME_SKIPPED, value, Scope.Individual)

    suspend fun setNameEntered() = set(PREF_INDIVIDUAL_KEY_NAME_ENTERED, true, Scope.Individual)

    fun hasNameEntered() = blockingGetBoolean(PREF_INDIVIDUAL_KEY_NAME_ENTERED, Scope.Individual)

    // TODO move it to AppLockHelper
    override suspend fun setInAppLockCancelled(value: Boolean) {
        set(KEY_INAPP_APP_LOCK_CANCELLED, value, Scope.Individual)
    }

    // TODO move it to AppLockHelper
    override fun isAppLockEnabled() = blockingGetBoolean(
        KEY_APP_LOCK_ENABLED,
        Scope.Individual,
        defaultPreferences.get().blockingGetBoolean(KEY_APP_LOCK_ENABLED, Scope.Individual)
    )

    fun wasAppInBackgroundFor20Minutes() = blockingGetBoolean(
        PREF_INDIVIDUAL_KEY_APP_RESUME_SESSION,
        Scope.Individual,
        defaultPreferences.get().blockingGetBoolean(PREF_INDIVIDUAL_KEY_APP_RESUME_SESSION, Scope.Individual)
    )

    fun setAppWasInBackgroundFor20Minutes(value: Boolean) {
        blockingSet(
            PREF_INDIVIDUAL_KEY_APP_RESUME_SESSION,
            defaultPreferences.get()
                .blockingGetBoolean(PREF_INDIVIDUAL_KEY_APP_RESUME_SESSION, Scope.Individual, value),
            Scope.Individual,
        )
    }

    override fun setUserSelectedLanguage(value: String) =
        blockingSet(PREF_INDIVIDUAL_USER_SELECTED_LANGUAGE, value, Scope.Individual)

    override fun getUserSelectedLanguage() =
        blockingGetString(PREF_INDIVIDUAL_USER_SELECTED_LANGUAGE, Scope.Individual).itOrBlank()

    suspend fun getIsFreshLogin(): Boolean {
        return getBoolean(PREF_INDIVIDUAL_FRESH_LOGIN, Scope.Individual).first()
    }

    override fun setIsFreshLogin(value: Boolean) {
        blockingGetBoolean(PREF_INDIVIDUAL_FRESH_LOGIN, Scope.Individual, value)
    }

    override suspend fun getPayablesOnboardingVariant(): String {
        return getString(PREF_INDIVIDUAL_PAYABLES_ONBOARDING_VARIANT, Scope.Individual).first()
    }

    override suspend fun setPayablesOnboardingVariant(value: String) {
        set(PREF_INDIVIDUAL_PAYABLES_ONBOARDING_VARIANT, value, Scope.Individual)
    }

    override suspend fun getVisibilityPreNetworkOnboardingNudge(): Boolean {
        return getBoolean(PREF_PRE_NETWORK_ONBOARDING_NUDGE, Scope.Individual, false).first()
    }

    override suspend fun setVisibilityPreNetworkOnboardingNudge(value: Boolean) {
        set(PREF_PRE_NETWORK_ONBOARDING_NUDGE, value, Scope.Individual)
    }

    suspend fun setSuspectUserIsSupplier(value: Boolean) {
        set(PREF_INDIVIDUAL_PAYABLES_ONBOARDING_SUSPECT_USER_IS_SUPPLIER, value, Scope.Individual)
    }

    suspend fun getSuspectUserIsSupplier(): Boolean {
        return getBoolean(PREF_INDIVIDUAL_PAYABLES_ONBOARDING_SUSPECT_USER_IS_SUPPLIER, Scope.Individual).first()
    }

    suspend fun getMarketingIsSignUp() =
        getBoolean(PREF_INDIVIDUAL_APPSFLYER_MARKETING_IS_SIGN_UP, Scope.Individual).first()

    suspend fun setMarketingIsSignUp(isSignUp: Boolean) =
        set(PREF_INDIVIDUAL_APPSFLYER_MARKETING_IS_SIGN_UP, isSignUp, Scope.Individual)

    suspend fun containsMarketingIsSignUp() = contains(PREF_INDIVIDUAL_APPSFLYER_MARKETING_IS_SIGN_UP, Scope.Individual)
    suspend fun removeMarketingIsSignUp() = remove(PREF_INDIVIDUAL_APPSFLYER_MARKETING_IS_SIGN_UP, Scope.Individual)

    suspend fun getMarketingAuthTime() =
        getLong(PREF_INDIVIDUAL_APPSFLYER_MARKETING_AUTH_TIME, Scope.Individual).first()

    suspend fun setMarketingAuthTime(authTime: Long) =
        set(PREF_INDIVIDUAL_APPSFLYER_MARKETING_AUTH_TIME, authTime, Scope.Individual)

    suspend fun containsMarketingAuthTime() = contains(PREF_INDIVIDUAL_APPSFLYER_MARKETING_AUTH_TIME, Scope.Individual)
    suspend fun removeMarketingAuthTime() = remove(PREF_INDIVIDUAL_APPSFLYER_MARKETING_AUTH_TIME, Scope.Individual)

    suspend fun clearData() {
        val userSelectedLanguage = getUserSelectedLanguage()

        // Clear all preferences
        super.clear()

        // Save these again
        setUserSelectedLanguage(userSelectedLanguage)
    }

    fun getIsRelationshipAddedAfterOnboarding(): Observable<Boolean> {
        return getBoolean(PREF_IS_RELATIONSHIP_ADDED, Scope.Individual).asObservable()
    }

    suspend fun setRelationshipAddedAfterOnboarding(value: Boolean) {
        set(PREF_IS_RELATIONSHIP_ADDED, value, Scope.Individual)
    }

    suspend fun getPreNetworkRelationships(): List<String> = withContext(Dispatchers.IO) {
        val json = getString(PREF_LIST_PRE_NETWORK_RELATIONSHIPS, Scope.Individual).first()
        gson.fromJson<List<String>>(json) ?: emptyList()
    }

    suspend fun savePreNetworkRelationships(preNetworkRelationships: List<String>) = withContext(Dispatchers.IO) {
        set(PREF_LIST_PRE_NETWORK_RELATIONSHIPS, gson.toJson(preNetworkRelationships), Scope.Individual)
    }

    suspend fun getPreNetworkSupplierCount(): String {
        return getString(PREF_PRE_NETWORK_SUPPLIER_COUNT, Scope.Individual, "").first()
    }

    suspend fun setPreNetworkSupplierCount(value: Long) {
        set(PREF_PRE_NETWORK_SUPPLIER_COUNT, value.toString(), Scope.Individual)
    }

    suspend fun setPreNetworkCustomerCount(value: Long) {
        set(PREF_PRE_NETWORK_CUSTOMER_COUNT, value.toString(), Scope.Individual)
    }

    suspend fun getPreNetworkCustomerCount(): String {
        return getString(PREF_PRE_NETWORK_CUSTOMER_COUNT, Scope.Individual, "").first()
    }
}
