package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.database.DueInfoRepo
import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncDirtyTransactions
import `in`.okcredit.backend._offline.usecase._sync_usecases.TransactionsSyncService
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport
import `in`.okcredit.backend.contract.AppLockManager
import `in`.okcredit.backend.contract.RxSharedPrefValues.FINGERPRINT_LOCK_ENABLED
import `in`.okcredit.backend.contract.RxSharedPrefValues.FINGERPRINT_LOCK_SYNCED
import `in`.okcredit.backend.contract.Signout
import `in`.okcredit.business_health_dashboard.contract.model.usecases.BusinessHealthDashboardLocalDataOperations
import `in`.okcredit.cashback.contract.usecase.CashbackLocalDataOperations
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.communication_inappnotification.contract.InAppNotificationRepository
import `in`.okcredit.customer.contract.CustomerRepository
import `in`.okcredit.home.HomePreferences
import `in`.okcredit.individual.contract.IndividualRepository
import `in`.okcredit.installedpackges.InstalledPackagesRepository
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.device.DeviceRepository
import `in`.okcredit.merchant.rewards.RewardsSyncRepository
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.onboarding.contract.OnboardingRepo
import `in`.okcredit.payment.contract.usecase.ClearPaymentEditAmountLocalData
import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.voice_first.contract.ResetDraftTransactions
import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.Lazy
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxCompletable
import merchant.android.okstream.contract.OkStreamService
import merchant.okcredit.accounting.contract.AccountingRepository
import merchant.okcredit.dynamicview.contract.DynamicViewRepository
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.usecases.VerifyPassword
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_APP_LOCK_SYNCED
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils.api
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.android.base.workmanager.WorkManagerPrefs
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.bills.BillRepository
import tech.okcredit.contacts.contract.ContactsRepository
import timber.log.Timber
import javax.inject.Inject

// Internet (if hard logout)
class SignoutImpl @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val transactionRepo: Lazy<TransactionRepo>,
    private val syncDirtyTransactions: Lazy<SyncDirtyTransactions>,
    private val appLockManager: Lazy<AppLockManager>,
    private val verifyPassword: Lazy<VerifyPassword>,
    private val authService: Lazy<AuthService>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val rewardsRepository: Lazy<RewardsSyncRepository>,
    private val referralRepository: Lazy<ReferralRepository>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val tracker: Lazy<Tracker>,
    private val transactionsSyncService: Lazy<TransactionsSyncService>,
    private val ab: Lazy<AbRepository>,
    private val businessAPI: Lazy<BusinessRepository>,
    private val individualRepository: Lazy<IndividualRepository>,
    private val onboardingRepo: Lazy<OnboardingRepo>,
    private val rxPreference: Lazy<DefaultPreferences>,
    private val deviceRepository: Lazy<DeviceRepository>,
    private val dueInfoRepo: Lazy<DueInfoRepo>,
    private val coreSdk: Lazy<CoreSdk>,
    private val mixpanelApi: Lazy<MixpanelAPI>,
    private val dynamicViewRepository: Lazy<DynamicViewRepository>,
    private val communicationApiLazy: Lazy<CommunicationRepository>,
    private val contactsRepository: Lazy<ContactsRepository>,
    private val installedPackagesRepository: Lazy<InstalledPackagesRepository>,
    private val firestore: Lazy<FirebaseFirestore>,
    private val billRepository: Lazy<BillRepository>,
    private val workManager: Lazy<OkcWorkManager>,
    private val homePreferences: Lazy<HomePreferences>,
    private val inAppNotificationRepository: Lazy<InAppNotificationRepository>,
    private val customerRepository: Lazy<CustomerRepository>,
    private val cashbackLocalDataOperations: Lazy<CashbackLocalDataOperations>,
    private val businessHealthDashboardLocalDataOperations: Lazy<BusinessHealthDashboardLocalDataOperations>,
    private val clearPaymentEditAmountLocalData: Lazy<ClearPaymentEditAmountLocalData>,
    private val okStreamService: Lazy<OkStreamService>,
    private val workManagerPrefs: Lazy<WorkManagerPrefs>,
    private val resetDraftTransactions: Lazy<ResetDraftTransactions>,
    private val context: Lazy<Context>,
    private val accountingRepository: Lazy<AccountingRepository>,
) : Signout {

    override fun isInProgress(): Boolean {
        return isInProgress
    }

    override fun execute(password: String?): Completable {
        if (isInProgress) {
            return Completable.complete()
        }
        Timber.v("<<<<NumberChange Signout password %s", password)

        return syncDirtyTransactions.get().execute()
            .andThen(transactionsSyncService.get().clearLastSyncTime())
            .andThen(Completable.fromAction { appLockManager.get().clearAppLockData() })
            .andThen(logout(password))
            .andThen(rxCompletable { resetDraftTransactions.get().execute() })
            .andThen(transactionRepo.get().clear())
            .andThen(coreSdk.get().clearLocalData())
            .andThen(customerRepo.get().clear())
            .andThen(supplierCreditRepository.get().clearLocalData())
            .andThen(collectionRepository.get().clearLocalData())
            .andThen(rewardsRepository.get().clearLocalData())
            .andThen(cashbackLocalDataOperations.get().executeClearLocalData())
            .andThen(businessHealthDashboardLocalDataOperations.get().executeClearLocalData())
            .andThen(clearPaymentEditAmountLocalData.get().execute())
            .andThen(referralRepository.get().signOut())
            .andThen(ab.get().clearLocalData())
            .andThen(Completable.fromAction { tracker.get().clearIdentity() })
            .andThen(deleteOnboardingPreferences())
            .andThen(dueInfoRepo.get().clear())
            .andThen(deleteFingerPrintLockPreference())
            .andThen(individualRepository.get().clearLocalData())
            .andThen(mixpanelReset())
            .andThen(contactsRepository.get().clearLocalData())
            .andThen(communicationApiLazy.get().clearAllNotifications())
            .andThen(dynamicViewRepository.get().clearLocalData())
            .andThen(cancelReportDownloadWorkers())
            .andThen(Completable.fromAction { firestore.get().clearPersistence() })
            .andThen(rxCompletable { homePreferences.get().clear() })
            .andThen(clearInAppNotifications())
            .andThen(installedPackagesRepository.get().cleanInstalledPkgsLocalData())
            .andThen(customerRepository.get().clearLocalData())
            .andThen(billRepository.get().clearLocalData())
            .andThen(clearWorkManagerPrefs())
            .andThen(accountingRepository.get().clearAccountingData())
            .andThen(businessAPI.get().clearLocalData())
            .doOnComplete {
                okStreamService.get().disconnect(context.get())
            }
            .doOnSubscribe {
                isInProgress = true
            }
            .doFinally {
                isInProgress = false
            }
    }

    private fun clearInAppNotifications() = inAppNotificationRepository.get().clear()

    private fun cancelReportDownloadWorkers(): Completable {
        return Completable.fromAction { workManager.get().cancelAllWorkByTag(DownloadReport.WORK_TAG) }
    }

    private fun mixpanelReset(): Completable {
        return Completable.fromAction { mixpanelApi.get().reset() }
    }

    private fun logout(password: String?): Completable {
        var fromAllDevices = false
        var passwordCheck = Completable.complete()
        if (password != null && password.isNotEmpty()) {
            passwordCheck = verifyPassword.get().execute(password)
            fromAllDevices = true
        }
        val _fromAllDevices = fromAllDevices
        return passwordCheck
            .andThen(
                Completable.fromAction {
                    val deviceId: String = deviceRepository.get().deviceDeprecated.id
                    if (_fromAllDevices) {
                        authService.get().logoutFromAllTheDevices(deviceId)
                    } else {
                        authService.get().logout(
                            deviceId,
                            authService.get().getAuthToken()
                        )
                    }
                }.subscribeOn(api())
            )
    }

    // Todo: Remove this method
    override fun logout(): Completable {
        return syncDirtyTransactions.get()
            .execute()
            .andThen(Completable.fromAction { appLockManager.get().clearAppLockData() })
            .andThen(
                Completable.fromAction {
                    authService.get().logoutFromAllTheDevices(deviceRepository.get().deviceDeprecated.id)
                }.subscribeOn(api())
            )
            .andThen(individualRepository.get().clearLocalData())
            .andThen(rxCompletable { resetDraftTransactions.get().execute() })
            .andThen(transactionRepo.get().clear())
            .andThen(coreSdk.get().clearLocalData())
            .andThen(customerRepo.get().clear())
            .andThen(supplierCreditRepository.get().clearLocalData().doOnComplete { Timber.i("deleteSupplierTable 4") })
            .andThen(collectionRepository.get().clearLocalData())
            .andThen(rewardsRepository.get().clearLocalData())
            .andThen(cashbackLocalDataOperations.get().executeClearLocalData())
            .andThen(businessHealthDashboardLocalDataOperations.get().executeClearLocalData())
            .andThen(clearPaymentEditAmountLocalData.get().execute())
            .andThen(transactionsSyncService.get().clearLastSyncTime())
            .andThen(ab.get().clearLocalData())
            .andThen(businessAPI.get().clearLocalData())
    }

    private fun deleteOnboardingPreferences(): Completable {
        return rxCompletable {
            rxPreference.get().remove(PREF_INDIVIDUAL_APP_LOCK_SYNCED, Scope.Individual)
            onboardingRepo.get().clearPreferences()
        }
    }

    private fun deleteFingerPrintLockPreference(): Completable {
        return rxCompletable {
            rxPreference.get().remove(FINGERPRINT_LOCK_SYNCED, Scope.Individual)
            rxPreference.get().remove(FINGERPRINT_LOCK_ENABLED, Scope.Individual)
        }
    }

    private fun clearWorkManagerPrefs() = rxCompletable { workManagerPrefs.get().clear() }

    companion object {
        private var isInProgress = false
    }
}
