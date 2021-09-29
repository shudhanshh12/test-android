package `in`.okcredit.backend.usecase

import `in`.okcredit.backend._offline.usecase.DueInfoSyncer
import `in`.okcredit.backend._offline.usecase.ServerActionableChecker
import `in`.okcredit.backend._offline.usecase.SuggestedCustomersForAddTransactionShortcutSyncer
import `in`.okcredit.backend._offline.usecase.SyncContactsWithAccount
import `in`.okcredit.backend._offline.usecase.SyncCustomerTxnAlert
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncCustomersImpl
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.backend.contract.CheckAuth
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.communication_inappnotification.contract.InAppNotificationRepository
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import merchant.okcredit.dynamicview.contract.DynamicViewRepository
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.bills.BillRepository

class NonActiveBusinessesDataSyncerTest {
    private val checkAuth: CheckAuth = mock()
    private val businessApi: BusinessRepository = mock()
    private val collectionSyncer: CollectionSyncer = mock()
    private val ab: AbRepository = mock()
    private val syncContactsWithAccount: SyncContactsWithAccount = mock()
    private val syncCustomerTxnAlert: SyncCustomerTxnAlert = mock()
    private val inAppNotificationRepository: InAppNotificationRepository = mock()
    private val suggestedCustomersForAddTransactionShortcutSyncer: SuggestedCustomersForAddTransactionShortcutSyncer =
        mock()
    private val billRepository: BillRepository = mock()
    private val getBusinessIdList: GetBusinessIdList = mock()
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val syncCustomersImpl: SyncCustomersImpl = mock()
    private val syncTransactionsImpl: SyncTransactionsImpl = mock()
    private val dueInfoSyncer: DueInfoSyncer = mock()
    private val serverActionableChecker: ServerActionableChecker = mock()
    private val dynamicViewRepository: DynamicViewRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val nonActiveBusinessesDataSyncer = NonActiveBusinessesDataSyncer(
        { checkAuth },
        { businessApi },
        { collectionSyncer },
        { ab },
        { syncContactsWithAccount },
        { syncCustomerTxnAlert },
        { inAppNotificationRepository },
        { suggestedCustomersForAddTransactionShortcutSyncer },
        { billRepository },
        { getBusinessIdList },
        { supplierCreditRepository },
        { syncCustomersImpl },
        { syncTransactionsImpl },
        { dueInfoSyncer },
        { serverActionableChecker },
        { dynamicViewRepository },
        { getActiveBusinessId }
    )

    private val businessId = "business-id"
    private val businessId2 = "business-id2"
    private val businessId3 = "business-id3"

    @Test
    fun `when user is logged in should schedule and execute all sync calls`() {
        runBlocking {
            // given
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(getBusinessIdList.execute()).thenReturn(flowOf(listOf(businessId, businessId2, businessId3)))
            whenever(supplierCreditRepository.syncSupplierEnabledCustomerIds(any())).thenReturn(Completable.complete())
            whenever(supplierCreditRepository.syncEverything(any())).thenReturn(Completable.complete())
            whenever(syncCustomersImpl.execute(any())).thenReturn(Completable.complete())
            whenever(syncTransactionsImpl.execute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(
                Completable.complete()
            )
            whenever(dueInfoSyncer.schedule(any())).thenReturn(Completable.complete())
            whenever(serverActionableChecker.schedule(any())).thenReturn(Completable.complete())
            whenever(dynamicViewRepository.scheduleSyncCustomizations(any())).thenReturn(Completable.complete())
            whenever(checkAuth.execute()).thenReturn(Observable.just(true))
            whenever(businessApi.executeSyncBusiness(any())).thenReturn(Completable.complete())
            whenever(ab.sync(sourceType = any(), businessId = anyOrNull())).thenReturn(Completable.complete())
            whenever(syncContactsWithAccount.schedule(any())).thenReturn(Completable.complete())
            whenever(syncCustomerTxnAlert.schedule(any())).thenReturn(Completable.complete())
            whenever(inAppNotificationRepository.scheduleSyncCompletable(any())).thenReturn(Completable.complete())
            whenever(suggestedCustomersForAddTransactionShortcutSyncer.schedule(any())).thenReturn(Completable.complete())
            whenever(billRepository.scheduleBillSync(any())).thenReturn(Completable.complete())

            // when
            val testObserver = nonActiveBusinessesDataSyncer.execute().test()

            // then
            verify(checkAuth).execute()
            verify(getActiveBusinessId).execute()
            listOf(businessId2, businessId3).forEach { businessId ->
                verify(supplierCreditRepository).syncSupplierEnabledCustomerIds(businessId)
                verify(supplierCreditRepository).syncEverything(businessId)
                verify(syncCustomersImpl).execute(businessId)
                verify(syncTransactionsImpl).execute(anyOrNull(), anyOrNull(), anyOrNull(), eq(businessId))
                verify(dueInfoSyncer).schedule(businessId)
                verify(serverActionableChecker).schedule(businessId)
                verify(dynamicViewRepository).scheduleSyncCustomizations(businessId)
                verify(businessApi).executeSyncBusiness(businessId)
                verify(collectionSyncer).scheduleSyncCollections(
                    CollectionSyncer.SYNC_ALL,
                    "non_active_businesses_syncer",
                    businessId
                )
                verify(collectionSyncer).scheduleCollectionProfile("non_active_businesses_syncer", businessId)
                verify(ab).sync(eq(businessId), anyOrNull())
                verify(syncContactsWithAccount).schedule(businessId)
                verify(syncCustomerTxnAlert).schedule(businessId)
                verify(inAppNotificationRepository).scheduleSyncCompletable(businessId)
                verify(suggestedCustomersForAddTransactionShortcutSyncer).schedule(businessId)
                verify(billRepository).scheduleBillSync(businessId)
            }
            testObserver.assertComplete()
        }
    }

    @Test
    fun `when user is not logged in should not schedule and execute any sync calls`() {
        runBlocking {
            // given
            whenever(checkAuth.execute()).thenReturn(Observable.just(false))

            // when
            val testObserver = nonActiveBusinessesDataSyncer.execute().test()

            // then
            verify(checkAuth).execute()
            verify(supplierCreditRepository, times(0)).syncSupplierEnabledCustomerIds(any())
            verify(supplierCreditRepository, times(0)).syncEverything(any())
            verify(syncCustomersImpl, times(0)).execute()
            verify(syncTransactionsImpl, times(0)).execute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            verify(dueInfoSyncer, times(0)).schedule(any())
            verify(serverActionableChecker, times(0)).schedule(any())
            verify(dynamicViewRepository, times(0)).scheduleSyncCustomizations(any())
            verify(businessApi, times(0)).executeSyncBusiness(any())
            verify(collectionSyncer, times(0)).scheduleSyncCollections(anyOrNull(), anyOrNull(), any())
            verify(collectionSyncer, times(0)).scheduleCollectionProfile(anyOrNull(), any())
            verify(ab, times(0)).sync(sourceType = any(), businessId = anyOrNull())
            verify(syncContactsWithAccount, times(0)).schedule(any())
            verify(syncCustomerTxnAlert, times(0)).schedule(any())
            verify(inAppNotificationRepository, times(0)).scheduleSyncCompletable(any())
            verify(suggestedCustomersForAddTransactionShortcutSyncer, times(0)).schedule(any())
            verify(billRepository, times(0)).scheduleBillSync(any())
            verify(getActiveBusinessId, times(0)).execute()
            testObserver.assertComplete()
        }
    }
}
