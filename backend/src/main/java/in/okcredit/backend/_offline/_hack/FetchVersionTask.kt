package `in`.okcredit.backend._offline._hack

import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.shared.service.keyval.KeyValService
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_KEY_SERVER_VERSION
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.GsonUtil
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import javax.inject.Inject

class FetchVersionTask(
    context: Context,
    workerParams: WorkerParameters,
    private val remoteSource: Lazy<BackendRemoteSource>,
    private val keyValService: Lazy<KeyValService>
) : BaseRxWorker(context, workerParams) {

    override fun doRxWork(): Completable {
        return remoteSource.get().latestVersion.flatMapCompletable {
            return@flatMapCompletable keyValService.get().put(
                PREF_INDIVIDUAL_KEY_SERVER_VERSION,
                GsonUtil.getGson().toJson(it),
                Scope.Individual
            )
        }
    }

    class Factory @Inject constructor(
        private val remoteSource: Lazy<BackendRemoteSource>,
        private val keyValService: Lazy<KeyValService>
    ) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return FetchVersionTask(context, params, remoteSource, keyValService)
        }
    }
}
