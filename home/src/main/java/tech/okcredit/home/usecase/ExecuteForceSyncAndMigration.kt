package tech.okcredit.home.usecase

import `in`.okcredit.backend._offline.usecase.ForceSyncAllTransactions
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import javax.inject.Inject

@Reusable
class ExecuteForceSyncAndMigration @Inject constructor(
    private val checkCoreSdkFeatureStatus: Lazy<CheckCoreSdkFeatureStatus>,
    private val forceSyncAllTransactions: Lazy<ForceSyncAllTransactions>
) {

    fun execute(): Completable {
        return forceSyncAllTransactions.get().executeWithFeatureFlagCheck().onErrorComplete().andThen(
            checkCoreSdkFeatureStatus.get().execute(Unit).ignoreElements()
        )
    }
}
