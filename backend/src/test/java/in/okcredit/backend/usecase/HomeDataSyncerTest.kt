package `in`.okcredit.backend.usecase

import `in`.okcredit.backend._offline.usecase.DueInfoSyncer
import `in`.okcredit.backend._offline.usecase.ServerActionableChecker
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncCustomersImpl
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.backend.contract.CheckAuth
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.individual.contract.SyncIndividual
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import merchant.okcredit.dynamicview.contract.DynamicViewRepository
import org.junit.Test

class HomeDataSyncerTest {

    private val checkAuth: CheckAuth = mock()
    private val collectionSyncer: CollectionSyncer = mockk()
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val syncCustomersImpl: SyncCustomersImpl = mock()
    private val syncTransactionsImpl: SyncTransactionsImpl = mock()
    private val dueInfoSyncer: DueInfoSyncer = mock()
    private val serverActionableChecker: ServerActionableChecker = mock()
    private val dynamicViewRepository: DynamicViewRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val syncIndividual: SyncIndividual = mockk()
    private val businessRepository: BusinessRepository = mock()

    private val homeDataSyncer = HomeDataSyncer(
        { checkAuth },
        { collectionSyncer },
        { supplierCreditRepository },
        { syncCustomersImpl },
        { syncTransactionsImpl },
        { dueInfoSyncer },
        { serverActionableChecker },
        { dynamicViewRepository },
        { getActiveBusinessId },
        { syncIndividual },
        { businessRepository },
    )

    @Test
    fun `when user is logged in should schedule and execute all sync calls`() {
        runBlocking {
            val businessId = "business-id"
            // given
            whenever(checkAuth.execute()).thenReturn(Observable.just(true))
            val syncIndividualResponse = mock<SyncIndividual.Response>()
            whenever(syncIndividualResponse.businessIdList).thenReturn(listOf(businessId))
            coJustRun { (collectionSyncer.executeSyncOnlinePayments(businessId)) }
            coJustRun { (syncIndividual.syncIndividualAndNewBusinessesIfPresent()) }
            whenever(supplierCreditRepository.syncSupplierEnabledCustomerIds(businessId)).thenReturn(Completable.complete())
            whenever(supplierCreditRepository.syncEverything(businessId)).thenReturn(Completable.complete())
            whenever(syncCustomersImpl.execute(businessId)).thenReturn(Completable.complete())
            whenever(syncTransactionsImpl.execute(anyOrNull(), anyOrNull(), anyOrNull(), eq(businessId))).thenReturn(
                Completable.complete()
            )
            whenever(dueInfoSyncer.schedule(businessId)).thenReturn(Completable.complete())
            whenever(serverActionableChecker.schedule(businessId)).thenReturn(Completable.complete())
            whenever(dynamicViewRepository.scheduleSyncCustomizations(businessId)).thenReturn(Completable.complete())
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(businessRepository.executeSyncBusiness(businessId)).thenReturn(Completable.complete())

            // when
            val testObserver = homeDataSyncer.execute().test()

            // then
            verify(checkAuth).execute()
            coVerify { syncIndividual.syncIndividualAndNewBusinessesIfPresent() }
            coVerify { collectionSyncer.executeSyncOnlinePayments(businessId) }
            verify(supplierCreditRepository).syncSupplierEnabledCustomerIds(businessId)
            verify(supplierCreditRepository).syncEverything(businessId)
            verify(syncCustomersImpl).execute(businessId)
            verify(syncTransactionsImpl).execute(anyOrNull(), anyOrNull(), anyOrNull(), eq(businessId))
            verify(dueInfoSyncer).schedule(businessId)
            verify(serverActionableChecker).schedule(businessId)
            verify(dynamicViewRepository).scheduleSyncCustomizations(businessId)
            verify(businessRepository).executeSyncBusiness(businessId)
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
            val testObserver = homeDataSyncer.execute().test()

            // then
            verify(checkAuth).execute()
            verify(supplierCreditRepository, times(0)).syncSupplierEnabledCustomerIds(businessId)
            verify(supplierCreditRepository, times(0)).syncEverything(businessId)
            verify(syncCustomersImpl, times(0)).execute()
            verify(syncTransactionsImpl, times(0)).execute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            verify(dueInfoSyncer, times(0)).schedule(businessId)
            verify(serverActionableChecker, times(0)).schedule(businessId)
            verify(dynamicViewRepository, times(0)).scheduleSyncCustomizations(businessId)
            coVerify(exactly = 0) { syncIndividual.syncIndividualAndNewBusinessesIfPresent() }
            coVerify(exactly = 0) { collectionSyncer.executeSyncOnlinePayments(businessId) }
            verify(businessRepository, times(0)).executeSyncBusiness(businessId)
            testObserver.assertComplete()
        }
    }
}
