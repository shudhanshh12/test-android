package `in`.okcredit.backend.usecase

import `in`.okcredit.backend._offline.usecase.LinkDevice
import `in`.okcredit.backend._offline.usecase.SuggestedCustomersForAddTransactionShortcutSyncer
import `in`.okcredit.backend._offline.usecase.SyncContactsWithAccount
import `in`.okcredit.backend._offline.usecase.SyncCustomerTxnAlert
import `in`.okcredit.backend.contract.CheckAuth
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.collection.contract.CollectionSyncer.Companion.SYNC_ALL
import `in`.okcredit.communication_inappnotification.contract.InAppNotificationRepository
import `in`.okcredit.installedpackges.InstalledPackagesRepository
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.rewards.contract.RewardsSyncer
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.bills.BillRepository
import tech.okcredit.contacts.contract.ContactsRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * [PeriodicDataSyncer] includes data sync calls for non-critical data, data not expected to change frequently.
 *
 * Trigger : home screen open
 * Rate limit : once in 24 hours
 */
class PeriodicDataSyncer @Inject constructor(
    private val checkAuth: Lazy<CheckAuth>,
    private val businessApi: Lazy<BusinessRepository>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
    private val rewardsSyncer: Lazy<RewardsSyncer>,
    private val ab: Lazy<AbRepository>,
    private val referralRepository: Lazy<ReferralRepository>,
    private val contactsRepository: Lazy<ContactsRepository>,
    private val syncContactsWithAccount: Lazy<SyncContactsWithAccount>,
    private val syncCustomerTxnAlert: Lazy<SyncCustomerTxnAlert>,
    private val installedPackagesRepository: Lazy<InstalledPackagesRepository>,
    private val inAppNotificationRepository: Lazy<InAppNotificationRepository>,
    private val suggestedCustomersForAddTransactionShortcutSyncer: Lazy<SuggestedCustomersForAddTransactionShortcutSyncer>,
    private val billRepository: Lazy<BillRepository>,
    private val linkDevice: Lazy<LinkDevice>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(businessId: String? = null): Completable {
        return isUserLoggedIn()
            .flatMapCompletable { isUserLoggedIn ->
                if (isUserLoggedIn) {
                    getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMapCompletable { _businessId ->
                        sync(_businessId)
                    }
                } else {
                    Completable.complete()
                }
            }.doOnComplete {
                Timber.d("<<<PeriodicDataSyncer completed")
            }.doOnError {
                RecordException.recordException(it)
                Timber.e("<<<<Worker Error PeriodicDataSyncer error ${it.message}")
            }
    }

    private fun sync(businessId: String): Completable {
        return Completable.mergeArray(
            rewardsSyncer.get().scheduleEverything(businessId),
            Completable.fromAction {
                collectionSyncer.get().scheduleSyncCollections(
                    SYNC_ALL,
                    source = CollectionSyncer.Source.PERIODIC_SYNCER,
                    businessId
                )
            },
            Completable.fromAction {
                collectionSyncer.get().scheduleCollectionProfile(CollectionSyncer.Source.PERIODIC_SYNCER, businessId)
            },
            ab.get().sync(businessId, "periodic").onErrorComplete(),
            syncReferralData(businessId),
            contactsRepository.get().scheduleUploadContactsWorker(),
            contactsRepository.get().scheduleCheckForContactsInOkcNetwork(),
            syncContactsWithAccount.get().schedule(businessId),
            syncCustomerTxnAlert.get().schedule(businessId),
            installedPackagesRepository.get().syncInstalledPackagesToServer(businessId),
            inAppNotificationRepository.get().scheduleSyncCompletable(businessId),
            billRepository.get().scheduleBillSync(businessId),
            suggestedCustomersForAddTransactionShortcutSyncer.get().schedule(businessId),
            syncBusiness(businessId),
            linkDevice.get().schedule(),
            supplierCreditRepository.get().syncNotificationReminder(businessId).onErrorComplete(),
        )
    }

    private fun isUserLoggedIn() = checkAuth.get().execute().firstOrError()

    private fun syncReferralData(businessId: String): Completable {
        return referralRepository.get().syncReferralLink(businessId)
            // TODO (Saket) : Optimise this
            .andThen(referralRepository.get().sync(businessId).onErrorComplete())
    }

    private fun syncBusiness(businessId: String): Completable {
        return businessApi.get().executeSyncBusiness(businessId)
    }
}
