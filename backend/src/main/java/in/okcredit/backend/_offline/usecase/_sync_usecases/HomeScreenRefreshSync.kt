package `in`.okcredit.backend._offline.usecase._sync_usecases

import `in`.okcredit.backend.worker.HomeRefreshSyncWorkerImpl.Companion.WORKER_NAME
import androidx.work.*
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Observable
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class HomeScreenRefreshSync @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
) {

    fun getWorkStatus(): Observable<WorkInfo> {
        return Observable.interval(2, TimeUnit.SECONDS)
            .flatMap {
                val works = workManager.get().getWorkInfosForUniqueWork(WORKER_NAME, Scope.Individual).get()
                if (works.size == 0) {
                    return@flatMap Observable.empty()
                }
                return@flatMap Observable.just(works[0])
            }
    }
}
