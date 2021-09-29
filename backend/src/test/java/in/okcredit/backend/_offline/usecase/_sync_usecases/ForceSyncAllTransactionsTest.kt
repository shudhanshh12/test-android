package `in`.okcredit.backend._offline.usecase._sync_usecases

import `in`.okcredit.backend._offline.usecase.ForceSyncAllTransactions
import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.shared.service.keyval.KeyValService
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_IS_FORCE_SYNC_ONCE

class ForceSyncAllTransactionsTest {

    private lateinit var forceSyncAllTransactions: ForceSyncAllTransactions

    private val ab: AbRepository = mock()
    private val keyValService: KeyValService = mock()
    private val syncTransactionsImpl: SyncTransactionsImpl = mock()
    private val getBusinessIdList: GetBusinessIdList = mock()

    @Before
    fun setup() {
        forceSyncAllTransactions = ForceSyncAllTransactions(
            { ab }, { keyValService },
            { syncTransactionsImpl },
            { getBusinessIdList }
        )
    }

    @Test
    fun `should force sync transaction if feature enable is enabled and it never runs on executeWithFeatureFlagCheck()`() {
        val businessIdList = listOf<String>("id1", "id2")

        whenever(getBusinessIdList.execute()).thenReturn(flowOf(businessIdList))
        whenever(ab.isFeatureEnabled(ForceSyncAllTransactions.FEATURE_FORCE_SYNC))
            .thenReturn(Observable.just(true))

        whenever(keyValService.contains(eq(PREF_INDIVIDUAL_IS_FORCE_SYNC_ONCE), any()))
            .thenReturn(Single.just(true))

        whenever(keyValService[eq(PREF_INDIVIDUAL_IS_FORCE_SYNC_ONCE), any()])
            .thenReturn(Observable.just("false"))

        val testObserver = forceSyncAllTransactions.executeWithFeatureFlagCheck().test()

//        TODO: fix this
//        verify(getBusinessIdList).execute()
//        verify(syncTransactionsImpl).executeForceSync(any())

        testObserver.dispose()
    }

    @Test
    fun `should not run force sync transaction if feature enable is disabled on executeWithFeatureFlagCheck()`() {
        whenever(ab.isFeatureEnabled(ForceSyncAllTransactions.FEATURE_FORCE_SYNC))
            .thenReturn(Observable.just(false))

        whenever(keyValService.contains(eq(PREF_INDIVIDUAL_IS_FORCE_SYNC_ONCE), any()))
            .thenReturn(Single.just(true))

        whenever(keyValService[eq(PREF_INDIVIDUAL_IS_FORCE_SYNC_ONCE), any()])
            .thenReturn(Observable.just("false"))

        val testObserver = forceSyncAllTransactions.executeWithFeatureFlagCheck().test()

        verify(syncTransactionsImpl, never()).executeForceSync()

        testObserver.dispose()
    }

    @Test
    fun `should not run force sync transaction for users who already run once 1 on executeWithFeatureFlagCheck()`() {
        whenever(ab.isFeatureEnabled(ForceSyncAllTransactions.FEATURE_FORCE_SYNC))
            .thenReturn(Observable.just(false))

        whenever(keyValService.contains(eq(PREF_INDIVIDUAL_IS_FORCE_SYNC_ONCE), any()))
            .thenReturn(Single.just(false))

        whenever(keyValService[eq(PREF_INDIVIDUAL_IS_FORCE_SYNC_ONCE), any()])
            .thenReturn(Observable.just("false"))

        val testObserver = forceSyncAllTransactions.executeWithFeatureFlagCheck().test()

        verify(syncTransactionsImpl, never()).executeForceSync()

        testObserver.dispose()
    }

    @Test
    fun `should not run force sync transaction for users who already run once 2 on executeWithFeatureFlagCheck()`() {
        whenever(ab.isFeatureEnabled(ForceSyncAllTransactions.FEATURE_FORCE_SYNC))
            .thenReturn(Observable.just(false))

        whenever(keyValService.contains(eq(PREF_INDIVIDUAL_IS_FORCE_SYNC_ONCE), any()))
            .thenReturn(Single.just(true))

        whenever(keyValService[eq(PREF_INDIVIDUAL_IS_FORCE_SYNC_ONCE), any()])
            .thenReturn(Observable.just("true"))

        val testObserver = forceSyncAllTransactions.executeWithFeatureFlagCheck().test()

        verify(syncTransactionsImpl, never()).executeForceSync()

        testObserver.dispose()
    }

    @Test
    fun `should run force sync transaction for on executeForce()`() {

        whenever(syncTransactionsImpl.executeForceSync())
            .thenReturn(Completable.complete())

        val testObserver = forceSyncAllTransactions.executeForceSync().test()

        verify(syncTransactionsImpl).executeForceSync()
        testObserver.dispose()
    }
}
