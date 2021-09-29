package tech.okcredit.contacts.worker

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.contacts.ContactsRemoteSource
import javax.inject.Inject

class AcknowledgeContactSavedWorker constructor(
    context: Context,
    params: WorkerParameters,
    private val remoteSource: ContactsRemoteSource,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : RxWorker(context, params) {

    override fun createWork(): Single<Result> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            remoteSource.acknowledgeContactSaved(businessId)
                .andThen(Single.just(Result.success()))
                .onErrorResumeNext {
                    Single.just(Result.retry())
                }
        }
    }

    class Factory @Inject constructor(
        private val remoteSource: Lazy<ContactsRemoteSource>,
        private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    ) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return AcknowledgeContactSavedWorker(
                context,
                params,
                remoteSource.get(),
                getActiveBusinessId
            )
        }
    }
}
