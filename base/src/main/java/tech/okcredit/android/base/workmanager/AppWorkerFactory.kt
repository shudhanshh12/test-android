package tech.okcredit.android.base.workmanager

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Provider

class AppWorkerFactory @Inject constructor(
    private val workerFactories: Map<Class<out ListenableWorker>, @JvmSuppressWildcards Provider<ChildWorkerFactory>>
) : WorkerFactory() {
    override fun createWorker(
        context: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return try {
            val foundEntry =
                workerFactories.entries.find { Class.forName(workerClassName).isAssignableFrom(it.key) }
            val factoryProvider = foundEntry?.value
            // TODO Uncomment this once all workers implement factory and start inheriting Worker class instead of RxWorker
            // ?: throw IllegalArgumentException("unknown worker class name: $workerClassName")
            factoryProvider?.get()?.create(context, workerParameters)
        } catch (exception: ClassNotFoundException) {
            FirebaseCrashlytics.getInstance().recordException(exception)
            null
        }
    }
}
