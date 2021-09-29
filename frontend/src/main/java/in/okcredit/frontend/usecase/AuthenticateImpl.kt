package `in`.okcredit.frontend.usecase

import `in`.okcredit.accounting_core.contract.SyncState
import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.SuperProperties
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.contract.Authenticate
import `in`.okcredit.collection.contract.EnablePaymentAddress
import `in`.okcredit.individual.contract.SyncIndividual
import `in`.okcredit.merchant.contract.SyncBusiness
import `in`.okcredit.merchant.device.DeviceHelper
import `in`.okcredit.merchant.device.usecase.GetAcquisitionData
import `in`.okcredit.merchant.device.usecase.IsCollectionCampaign
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import `in`.okcredit.onboarding.contract.marketing.AppsFlyerHelper
import `in`.okcredit.onboarding.data.OnboardingPreferencesImpl
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.Credential
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.app_contract.AppConstants
import timber.log.Timber
import javax.inject.Inject

// This will return a Pair of boolean, boolean <newlyRegisteredUser, exitingUserAlreadyEnabledAppLock>
class AuthenticateImpl @Inject constructor(
    private val authService: Lazy<AuthService>,
    private val syncIndividual: Lazy<SyncIndividual>,
    private val tracker: Lazy<Tracker>,
    private val analyticsProvider: Lazy<AnalyticsProvider>,
    private val deviceHelper: Lazy<DeviceHelper>,
    private val loginDataSyncer: Lazy<LoginDataSyncerImpl>,
    private val isCollectionCampaign: Lazy<IsCollectionCampaign>,
    private val enablePaymentAddress: Lazy<EnablePaymentAddress>,
    private val getAcquisitionData: Lazy<GetAcquisitionData>,
    private val localeManager: Lazy<LocaleManager>,
    private val onboardingPreferences: Lazy<OnboardingPreferencesImpl>,
    private val appsFlyerHelper: Lazy<AppsFlyerHelper>,
    private val syncBusiness: Lazy<SyncBusiness>,
) : Authenticate {

    companion object {
        const val TAG = "<<<<AuthenticateUseCase"
    }

    override fun execute(req: Credential): Observable<Result<Pair<Boolean, Boolean>>> {
        return UseCase.wrapSingle(
            Single
                .fromCallable {
                    try {
                        authService.get().authenticate(req)
                    } catch (e: UndeliverableException) {
                        throw RuntimeException(e)
                    }
                }
                // Syncing Auth Scope during authentication for newUser for skipping sync screen.
                .flatMap { pair ->
                    val newUser = pair.first
                    val appLock = pair.second

                    Timber.d("$TAG Auth service return value $newUser and $appLock")
                    rxCompletable {
                        with(onboardingPreferences.get()) {
                            set(OnboardingPreferences.KEY_NEW_USER, newUser, Scope.Individual)
                            set(
                                OnboardingPreferencesImpl.PREF_INDIVIDUAL_KEY_EXITING_USER_ENABLED_APP_LOCK,
                                appLock,
                                Scope.Individual
                            )
                            setIsFreshLogin(true)
                        }
                    }.andThen(
                        if (newUser) {
                            checkCampaignBeforeSync(newUser, pair)
                                .onErrorResumeNext { startAuthSyc(newUser, pair) }
                        } else {
                            linkAnalytics(newUser).andThen(Single.just(pair))
                        }
                    )
                }
                .subscribeOn(ThreadUtils.api())
        )
    }

    private fun checkCampaignBeforeSync(
        newUser: Boolean,
        pair: Pair<Boolean, Boolean>,
    ): Single<Pair<Boolean, Boolean>> {
        return isCollectionCampaign.get().execute()
            .flatMap { fromCollectionCampaign ->
                if (fromCollectionCampaign) {
                    tracker.get().setSuperProperties(
                        PropertyKey.COLLECTION_CAMPAIGN,
                        AppConstants.PAYMENT_INSTALL_LINK_UTM_CAMPAIGN
                    )
                    startAuthSyncAfterEnablingCustomerPayment(newUser, pair)
                } else {
                    startAuthSyc(newUser, pair)
                }
            }
    }

    private fun startAuthSyncAfterEnablingCustomerPayment(
        newUser: Boolean,
        pair: Pair<Boolean, Boolean>,
    ): Single<Pair<Boolean, Boolean>> {
        return enablePaymentAddress.get().execute()
            .andThen(startAuthSyc(newUser, pair))
    }

    private fun startAuthSyc(
        newUser: Boolean,
        it: Pair<Boolean, Boolean>,
    ): Single<Pair<Boolean, Boolean>> {
        return linkAnalytics(newUser).andThen(
            loginDataSyncer.get().execute(BehaviorSubject.createDefault(SyncState.WAITING)).filter {
                Timber.d("$TAG Wrapper return value $it")
                it is Result.Success || it is Result.Failure
            }.flatMapCompletable {
                Timber.d("$TAG Wrapper return value $it")
                Completable.complete()
            }
        ).andThen(Single.just(it))
    }

    private fun linkAnalytics(isSignup: Boolean): Completable {
        return rxSingle { syncIndividual.get().execute() }
            .flatMapCompletable { (individualId, businessIdList) ->
                rxCompletable {
                    businessIdList.forEach { businessId ->
                        val business = syncBusiness.get().execute(businessId).await()
                        if (business.isFirst) {
                            val businessName = if (business.isNameSet()) business.name else null
                            setupAnalyticsData(isSignup, individualId, businessId, businessName)
                            return@rxCompletable
                        }
                    }

                    // fail-safe
                    setupAnalyticsData(isSignup, individualId, businessIdList.first())
                }
            }
            .andThen(setAcquisitionUserProperties(isSignup))
    }

    private suspend fun setupAnalyticsData(
        isSignup: Boolean,
        individualId: String,
        businessId: String,
        businessName: String? = null,
    ) {
        appsFlyerHelper.get().setAuthSuccess(isSignup)

        Timber.d("linkAnalytics $individualId")
        tracker.get().setIdentity(businessId, isSignup)

        tracker.get().setUserProperties(
            businessId,
            businessName,
            localeManager.get().getLanguage(),
            LocaleManager.getDeviceLanguage()
        )

        tracker.get().setSuperProperties(SuperProperties.MERCHANT_ID, businessId)

        tracker.get().setSuperPropertiesForIndividual(
            individualId,
            localeManager.get().getLanguage()
        )
    }

    private fun setAcquisitionUserProperties(newUser: Boolean): Completable {
        return if (newUser) {
            getAcquisitionData.get().execute()
                .map {
                    val properties = deviceHelper.get().getMappedAppsFlyerData(it as MutableMap<String, String>)
                    if (properties.isNullOrEmpty().not()) {
                        analyticsProvider.get()
                            .setUserProperty(properties)
                    }
                }.ignoreElement().onErrorComplete {
                    RecordException.recordException(it)
                    true
                }
        } else {
            Completable.complete()
        }
    }
}
