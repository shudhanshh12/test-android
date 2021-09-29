package `in`.okcredit

import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.DeleteCustomer
import `in`.okcredit.backend._offline.usecase.DueInfoParticularCustomerSyncer
import `in`.okcredit.backend._offline.usecase.ServerActionableChecker
import `in`.okcredit.backend._offline.usecase.SyncContactsWithAccount
import `in`.okcredit.backend._offline.usecase.SyncCustomerTxnAlert
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncCustomer
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.backend.contract.Signout
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection.contract.CollectionStatus
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.collection.contract.OnlineCollectionNotification
import `in`.okcredit.collection_ui.ui.passbook.payments.views.OnlinePaymentsView
import `in`.okcredit.individual.contract.SyncIndividual
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.rewards.contract.RewardsSyncer
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import merchant.okcredit.user_stories.contract.UserStoryRepository
import org.joda.time.DateTime
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.communication.NotificationData
import tech.okcredit.android.communication.workers.CommunicationProcessSyncNotificationWorker.Companion.SyncNotificationType
import tech.okcredit.bills.BillRepository
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.contacts.worker.CheckForContactsInOkcNetworkWorker.Companion.WORKER_NAME_FCM
import timber.log.Timber
import javax.inject.Inject

class SyncNotificationHandler @Inject constructor(
    private val dueInfoSyncer: Lazy<DueInfoParticularCustomerSyncer>,
    private val signout: Lazy<Signout>,
    private val syncCustomer: Lazy<SyncCustomer>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val deleteCustomer: Lazy<DeleteCustomer>,
    private val businessApi: Lazy<BusinessRepository>,
    private val rewardsSyncer: Lazy<RewardsSyncer>,
    private val syncTransactionsImpl: Lazy<SyncTransactionsImpl>,
    private val tracker: Lazy<Tracker>,
    private val syncContactsWithAccount: Lazy<SyncContactsWithAccount>,
    private val syncCustomerTxnAlert: Lazy<SyncCustomerTxnAlert>,
    private val ab: Lazy<AbRepository>,
    private val serverActionableChecker: Lazy<ServerActionableChecker>,
    private val billRepository: Lazy<BillRepository>,
    private val userStoryRepository: Lazy<UserStoryRepository>,
    private val getBusinessIdList: Lazy<GetBusinessIdList>,
    private val syncIndividual: Lazy<SyncIndividual>,
    private val contactsRepository: Lazy<ContactsRepository>,
) {

    fun execute(notificationData: NotificationData, businessId: String): Completable {
        Timber.i("<<<<Notification Sync Notification Handler Type: %s", notificationData.toString())
        return isSyncNotificationOfExistingMerchant(businessId).flatMapCompletable { result ->
            if (result.not() && notificationData.type != SyncNotificationType.INDIVIDUAL.value) {
                Completable.complete()
            } else {
                handleSyncNotification(notificationData, businessId)
                    .doOnComplete { Timber.d("SyncNotificationHandler: Completed") }
                    .doOnError { Timber.e("SyncNotificationHandler - Error: ${it.stackTraceToString()}") }
            }
        }
    }

    private fun handleSyncNotification(notificationData: NotificationData, businessId: String): Completable {
        return if (notificationData.type == SyncNotificationType.SUPPLIER_V2.value &&
            notificationData.supplierId.isNullOrEmpty().not()
        ) {
            supplierCreditRepository.get().syncSpecificSupplier(notificationData.supplierId!!, businessId)
        } else if (notificationData.type == SyncNotificationType.SUPPLIER_TXNS.value) {
            supplierCreditRepository.get().syncAllTransactions(null, businessId = businessId)
                .doOnComplete {
                    tracker.get().trackSyncSupplierTxnsSuccessful(notificationData.transactionId)
                }
        } else if (notificationData.type == SyncNotificationType.CUSTOMER_V2.value &&
            notificationData.customerId.isNullOrEmpty().not()
        ) {
            syncCustomer.get().execute(notificationData.customerId!!, businessId)
        } else if (notificationData.type == SyncNotificationType.CUSTOMER_TXNS.value) {
            if (notificationData.transactionId.isNullOrEmpty().not())
                tracker.get().trackSyncTxnsFcmNotificationReceived(notificationData.transactionId!!)
            syncTransactionsImpl.get().execute("sync_notification", null, false, businessId)
                .doOnComplete {
                    if (notificationData.transactionId.isNullOrEmpty().not())
                        tracker.get().trackSyncTxnsSuccessful(notificationData.transactionId!!)
                }
                .doOnError { t ->
                    trackCustomerTransactionSyncError(t, notificationData.transactionId ?: "")
                    RecordException.recordException(t)
                }
        } else if (notificationData.type == SyncNotificationType.COLLECTION.value) {
            if (!notificationData.collectionId.isNullOrEmpty() &&
                !notificationData.createTime.isNullOrEmpty() &&
                notificationData.createTime != "0" &&
                !notificationData.updateTime.isNullOrEmpty() &&
                notificationData.updateTime != "0" &&
                !notificationData.status.isNullOrEmpty() &&
                notificationData.status != "0"
            ) {
                notificationData.amount?.let {
                    if (notificationData.status?.toIntOrNull() == CollectionStatus.PAID) {
                        OnlineCollectionNotification.send(
                            OnlineCollectionNotification.OnlinePayment(
                                customerId = notificationData.customerId,
                                amount = it.toLong(),
                                createTime = notificationData.createTime?.toLongOrNull()!!.times(1000L)
                            )
                        )
                    }
                }
                rxCompletable {
                    collectionSyncer.get().syncCollectionFromNotification(
                        Collection(
                            id = notificationData.collectionId!!,
                            customer_id = notificationData.customerId!!,
                            paymentId = notificationData.paymentId,
                            status = notificationData.status?.toIntOrNull() ?: 1,
                            amount_collected = notificationData.amount?.toLongOrNull(),
                            amount_requested = notificationData.amount?.toLongOrNull(),
                            create_time = DateTime(notificationData.createTime?.toLongOrNull()!!.times(1000L)),
                            update_time = DateTime(notificationData.updateTime?.toLongOrNull()!!.times(1000L)),
                            errorCode = notificationData.errorCode ?: "",
                        ),
                        businessId
                    )
                }
            } else {
                rxCompletable {
                    collectionSyncer.get().scheduleSyncEverything(SyncNotificationType.COLLECTION.value, businessId)
                }
            }
        } else if (notificationData.type == SyncNotificationType.MERCHANT.value) {
            businessApi.get().scheduleSyncBusiness(businessId)
        } else if (notificationData.type == SyncNotificationType.REWARD.value) {
            rewardsSyncer.get().scheduleEverything(businessId)
        } else if (notificationData.type == SyncNotificationType.LOGOUT.value) {
            signout.get().execute(null)
        } else if (notificationData.type == SyncNotificationType.CUSTOMER_DELETED.value &&
            notificationData.customerId.isNullOrEmpty().not()
        ) {
            syncCustomer.get().execute(notificationData.customerId!!, businessId)
        } else if (notificationData.type == SyncNotificationType.DELETE_CUSTOMER.value &&
            notificationData.customerId.isNullOrEmpty().not()
        ) {
            deleteCustomer.get().execute(notificationData.customerId!!, businessId)
        } else if (notificationData.type == SyncNotificationType.DUE_INFO.value &&
            notificationData.customerId.isNullOrEmpty().not()
        ) {
            dueInfoSyncer.get().schedule(notificationData.customerId!!, businessId)
        } else if (notificationData.type == SyncNotificationType.SYNC_CONTACT.value) {
            syncContactsWithAccount.get().schedule(businessId)
        } else if (notificationData.type == SyncNotificationType.SYNC_ACCOUNTS_FEATURE.value) {
            syncCustomerTxnAlert.get().schedule(businessId)
        } else if (notificationData.type == SyncNotificationType.SYNC_PROFILE.value) {
            ab.get().scheduleSync(businessId, "sync_notification")
        } else if (notificationData.type == SyncNotificationType.RECOVERY_ACTION.value) {
            serverActionableChecker.get().schedule(businessId)
        } else if (notificationData.type == SyncNotificationType.COLLECTION_DESTINATION.value) {
            rxCompletable {
                collectionSyncer.get()
                    .scheduleCollectionProfile(SyncNotificationType.COLLECTION_DESTINATION.value, businessId)
            }
        } else if (notificationData.type == SyncNotificationType.CUSTOMER_TXN_RESTRICTION.value) {
            supplierCreditRepository.get().syncSupplierEnabledCustomerIds(businessId)
        } else if (notificationData.type == SyncNotificationType.SYNC_BILLS.value) {
            billRepository.get().scheduleBillSync(businessId)
        } else if (notificationData.type == SyncNotificationType.COLLECTION_KYC.value) {
            rxCompletable { collectionSyncer.get().executeSyncKyc(businessId = businessId) }
        } else if (notificationData.type == SyncNotificationType.NOTIFICATION_REMINDER.value) {
            supplierCreditRepository.get().syncNotificationReminder(businessId)
        } else if (notificationData.type == SyncNotificationType.ONLINE_COLLETION.value) {
            if (!notificationData.collectionId.isNullOrEmpty() &&
                !notificationData.paymentId.isNullOrEmpty() &&
                !notificationData.createTime.isNullOrEmpty() &&
                notificationData.createTime != "0" &&
                !notificationData.updateTime.isNullOrEmpty() &&
                notificationData.updateTime != "0" &&
                !notificationData.status.isNullOrEmpty() &&
                notificationData.status != "0"
            ) {
                notificationData.amount?.let {
                    if (notificationData.status?.toIntOrNull() == CollectionStatus.PAID) {
                        OnlineCollectionNotification.send(
                            OnlineCollectionNotification.OnlinePayment(
                                customerId = notificationData.customerId,
                                amount = it.toLong(),
                                createTime = notificationData.createTime?.toLongOrNull()!!.times(1000L)
                            )
                        )
                    }
                }
                rxCompletable {
                    collectionSyncer.get().syncOnlinePaymentsFromNotification(
                        CollectionOnlinePayment(
                            id = notificationData.collectionId!!,
                            accountId = notificationData.customerId ?: "",
                            paymentId = notificationData.paymentId!!,
                            status = notificationData.status?.toIntOrNull() ?: 1,
                            amount = notificationData.amount?.toDouble() ?: 0.0,
                            createdTime = DateTime(notificationData.createTime!!.toLongOrNull()!!.times(1000L)),
                            updatedTime = DateTime(notificationData.updateTime!!.toLongOrNull()!!.times(1000L)),
                            errorCode = notificationData.errorCode ?: "",
                            errorDescription = notificationData.errorCode ?: "",
                            type = notificationData.paymentType ?: OnlinePaymentsView.TYPE_QR,
                            payoutId = notificationData.payoutId,
                        ),
                        businessId
                    )
                }
            } else {
                rxCompletable { collectionSyncer.get().executeSyncOnlinePayments(businessId) }
            }
        } else if (notificationData.type == SyncNotificationType.PAYMENT_SYNC.value) {
            // syncing everything no api provided by backend
            rxCompletable {
                collectionSyncer.get().scheduleSyncEverything(SyncNotificationType.PAYMENT_SYNC.value, businessId)
            }
        } else if (notificationData.type == SyncNotificationType.USER_STORIES.value) {
            Completable.mergeArray(
                userStoryRepository.get().syncAddStory(businessId),
                userStoryRepository.get().syncMyStory(businessId),
                userStoryRepository.get().syncOtherStory(businessId)
            )
        } else if (notificationData.type == SyncNotificationType.INDIVIDUAL.value) {
            rxCompletable { syncIndividual.get().syncIndividualAndNewBusinessesIfPresent() }
        } else if (notificationData.type == SyncNotificationType.CONTACT_NETWORK.value) {
            contactsRepository.get().scheduleCheckForContactsInOkcNetwork(workerName = WORKER_NAME_FCM)
        } else {
            Completable.complete()
        }
    }

    private fun isSyncNotificationOfExistingMerchant(businessId: String): Single<Boolean> {
        if (businessId.isEmpty().not()) {
            return getBusinessIdList.get().execute().asObservable().firstOrError().flatMap {
                if (it.contains(businessId)) {
                    return@flatMap Single.just(true)
                } else {
                    return@flatMap Single.just(false)
                }
            }
        } else {
            return Single.just(true)
        }
    }

    private fun trackCustomerTransactionSyncError(t: Throwable, transactionId: String) {
        val reason = t.message ?: ""
        tracker.get().trackDebug(
            "SyncNotificationHandler:Error ${SyncNotificationType.CUSTOMER_TXNS}",
            mapOf(PropertyKey.TXN_ID to transactionId, PropertyKey.REASON to reason)
        )
    }
}
