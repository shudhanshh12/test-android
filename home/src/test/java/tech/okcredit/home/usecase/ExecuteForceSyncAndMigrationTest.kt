package tech.okcredit.home.usecase

import `in`.okcredit.backend._offline.usecase.ForceSyncAllTransactions
import `in`.okcredit.shared.usecase.UseCase
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Before
import org.junit.Test

class ExecuteForceSyncAndMigrationTest {

    private lateinit var executeForceSyncAndMigration: ExecuteForceSyncAndMigration

    private val checkCoreSdkFeatureStatus: CheckCoreSdkFeatureStatus = mock()
    private val forceSyncAllTransactions: ForceSyncAllTransactions = mock()

    @Before
    fun setup() {
        executeForceSyncAndMigration = ExecuteForceSyncAndMigration(
            { checkCoreSdkFeatureStatus },
            { forceSyncAllTransactions }
        )
    }

    @Test
    fun `should run both core migration and force sync `() {
        whenever(checkCoreSdkFeatureStatus.execute(Unit))
            .thenReturn(
                UseCase.wrapCompletable(Completable.complete())
            )

        whenever(forceSyncAllTransactions.executeWithFeatureFlagCheck())
            .thenReturn(
                Completable.complete()
            )

        val testObserver = executeForceSyncAndMigration.execute().test()

        verify(checkCoreSdkFeatureStatus).execute(Unit)
        verify(forceSyncAllTransactions).executeWithFeatureFlagCheck()

        testObserver.dispose()
    }

    @Test
    fun `should run migration even force sync is throwing error `() {
        whenever(checkCoreSdkFeatureStatus.execute(Unit))
            .thenReturn(
                UseCase.wrapCompletable(Completable.complete())
            )

        whenever(forceSyncAllTransactions.executeWithFeatureFlagCheck())
            .thenReturn(
                Completable.error(Exception())
            )

        val testObserver = executeForceSyncAndMigration.execute().test()

        verify(checkCoreSdkFeatureStatus).execute(Unit)
        verify(forceSyncAllTransactions).executeWithFeatureFlagCheck()

        testObserver.dispose()
    }
}
