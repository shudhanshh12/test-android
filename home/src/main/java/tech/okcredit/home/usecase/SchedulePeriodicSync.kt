package tech.okcredit.home.usecase

import `in`.okcredit.backend.contract.PeriodicDataSyncWorker
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class SchedulePeriodicSync @Inject constructor(
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val periodicDataSyncWorker: Lazy<PeriodicDataSyncWorker>,
) {

    suspend fun execute() = getActiveBusinessId.get().execute().flatMapCompletable { _businessId ->
        periodicDataSyncWorker.get().schedule(_businessId)
    }.await()
}
