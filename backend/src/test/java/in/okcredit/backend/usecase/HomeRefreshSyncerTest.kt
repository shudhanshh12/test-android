package `in`.okcredit.backend.usecase

import `in`.okcredit.backend._offline.usecase.DueInfoSyncer
import `in`.okcredit.backend._offline.usecase.ServerActionableChecker
import `in`.okcredit.backend._offline.usecase.SyncCustomerTxnAlert
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncCustomersImpl
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.backend.contract.CheckAuth
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.communication_inappnotification.contract.InAppNotificationRepository
import `in`.okcredit.individual.contract.SyncIndividual
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import merchant.okcredit.dynamicview.contract.DynamicViewRepository
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.contacts.contract.ContactsRepository

class HomeRefreshSyncerTest {

    private val checkAuth: CheckAuth = mock()
    private val businessApi: BusinessRepository = mock()
    private val collectionSyncer: CollectionSyncer = mock()
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val ab: AbRepository = mock()
    private val syncCustomersImpl: SyncCustomersImpl = mock()
    private val syncTransactionsImpl: SyncTransactionsImpl = mock()
    private val dueInfoSyncer: DueInfoSyncer = mock()
    private val serverActionableChecker: ServerActionableChecker = mock()
    private val syncCustomerTxnAlert: SyncCustomerTxnAlert = mock()
    private val dynamicViewRepository: DynamicViewRepository = mock()
    private val inAppNotificationRepository: InAppNotificationRepository = mock()
    private val syncIndividual: SyncIndividual = mock()
    private val contactsRepository: ContactsRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val homeRefreshSyncer = HomeRefreshSyncer(
        { checkAuth },
        { businessApi },
        { collectionSyncer },
        { supplierCreditRepository },
        { ab },
        { syncCustomersImpl },
        { syncTransactionsImpl },
        { dueInfoSyncer },
        { serverActionableChecker },
        { syncCustomerTxnAlert },
        { dynamicViewRepository },
        { inAppNotificationRepository },
        { syncIndividual },
        { getActiveBusinessId },
        { contactsRepository },
    )

    @Test
    fun `when user is logged in should schedule and execute all sync calls`() {
        runBlocking {
            val businessId = "business-id"
            // given
            mockkStatic(Dispatchers::class)
            every { Dispatchers.Default } returns Dispatchers.Unconfined
            val syncIndividualResponse = mock<SyncIndividual.Response>()
            whenever(syncIndividualResponse.businessIdList).thenReturn(listOf(businessId))
            whenever(checkAuth.execute()).thenReturn(Observable.just(true))
            whenever(businessApi.executeSyncBusiness(businessId)).thenReturn(Completable.complete())
            whenever(syncTransactionsImpl.execute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(
                Completable.complete()
            )
            whenever(syncCustomersImpl.execute(businessId)).thenReturn(Completable.complete())
            doNothing().whenever(collectionSyncer).scheduleSyncEverything(
                "home_refresh",
                businessId
            )
            whenever(supplierCreditRepository.syncSupplierEnabledCustomerIds(businessId)).thenReturn(Completable.complete())
            whenever(supplierCreditRepository.syncEverything(businessId)).thenReturn(Completable.complete())
            whenever(ab.sync(businessId, "home_refresh")).thenReturn(Completable.complete())
            whenever(dueInfoSyncer.schedule(businessId)).thenReturn(Completable.complete())
            whenever(serverActionableChecker.schedule(businessId)).thenReturn(Completable.complete())
            whenever(syncCustomerTxnAlert.schedule(businessId)).thenReturn(Completable.complete())
            whenever(dynamicViewRepository.scheduleSyncCustomizations(businessId)).thenReturn(Completable.complete())
            whenever(inAppNotificationRepository.scheduleSyncCompletable(businessId)).thenReturn(Completable.complete())
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(contactsRepository.scheduleUploadContactsWorker(true)).thenReturn(Completable.complete())
            whenever(contactsRepository.scheduleCheckForContactsInOkcNetwork(true)).thenReturn(Completable.complete())

            // when
            val testObserver = homeRefreshSyncer.execute("home_refresh").test()

            // then
            verify(checkAuth).execute()
            verify(businessApi).executeSyncBusiness(businessId)
            verify(syncIndividual).syncIndividualAndNewBusinessesIfPresent()
            verify(syncTransactionsImpl).execute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            verify(syncCustomersImpl).execute(businessId)
            verify(collectionSyncer).scheduleSyncEverything("home_refresh", businessId)
            verify(supplierCreditRepository).syncSupplierEnabledCustomerIds(businessId)
            verify(supplierCreditRepository).syncEverything(businessId)
            verify(ab).sync(businessId, "home_refresh")
            verify(dueInfoSyncer).schedule(businessId)
            verify(serverActionableChecker).schedule(businessId)
            verify(syncCustomerTxnAlert).schedule(businessId)
            verify(dynamicViewRepository).scheduleSyncCustomizations(businessId)
            verify(inAppNotificationRepository).scheduleSyncCompletable(businessId)
            verify(contactsRepository).scheduleUploadContactsWorker(true)
            verify(contactsRepository).scheduleCheckForContactsInOkcNetwork(true)
            testObserver.assertComplete()
        }
    }

    @Test
    fun `when user is not logged in should not schedule and execute any sync calls`() {
        runBlocking {
            val businessId = "business-id"
            // given
            whenever(checkAuth.execute()).thenReturn(Observable.just(false))

            // when
            val testObserver = homeRefreshSyncer.execute("home_refresh").test()

            // then
            verify(checkAuth).execute()
            verify(businessApi, times(0)).executeSyncBusiness(businessId)
            verify(syncIndividual, times(0)).syncIndividualAndNewBusinessesIfPresent()
            verify(syncTransactionsImpl, times(0)).execute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            verify(syncCustomersImpl, times(0)).execute()
            verify(collectionSyncer, times(0)).scheduleSyncEverything("home_refresh", businessId)
            verify(supplierCreditRepository, times(0)).syncSupplierEnabledCustomerIds(businessId)
            verify(supplierCreditRepository, times(0)).syncEverything(businessId)
            verify(ab, times(0)).sync(null, "home_refresh")
            verify(dueInfoSyncer, times(0)).schedule(businessId)
            verify(serverActionableChecker, times(0)).schedule(businessId)
            verify(syncCustomerTxnAlert, times(0)).schedule(businessId)
            verify(dynamicViewRepository, times(0)).scheduleSyncCustomizations(businessId)
            verify(inAppNotificationRepository, times(0)).scheduleSyncCompletable(businessId)
            verify(contactsRepository, times(0)).scheduleUploadContactsWorker(true)
            verify(contactsRepository, times(0)).scheduleCheckForContactsInOkcNetwork(true)
            testObserver.assertComplete()
        }
    }
}
