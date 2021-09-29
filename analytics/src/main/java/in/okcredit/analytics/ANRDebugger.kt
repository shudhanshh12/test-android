package `in`.okcredit.analytics

import androidx.work.WorkInfo
import androidx.work.WorkQuery
import dagger.Lazy
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.min

class ANRDebugger @Inject constructor(
    private val analyticsProvider: Lazy<IAnalyticsProvider>,
    private val workManager: Lazy<OkcWorkManager>,
) {

    private var screen = ""
    private var lastNotificationType = ""

    private val workQuery: WorkQuery by lazy {
        WorkQuery.Builder.fromStates(
            listOf(
                WorkInfo.State.RUNNING,
                WorkInfo.State.BLOCKED,
            )
        )
            .build()
    }

    fun setCurrentScreen(screen: String) {
        this.screen = screen
    }

    fun setLastNotificationType(type: String) {
        this.lastNotificationType = type
    }

    fun logWorkersAndSendEvent(throwable: Throwable) {
        val workInfoFuture = workManager.get().getWorkInfos(workQuery)
        workInfoFuture.addListener(
            {
                val workInfoList = workInfoFuture.get().map { "${it.tags.joinToString()}, ${it.state.name}" }
                val properties = mutableMapOf(
                    WORKERS to workInfoList.joinToString(separator = ";"),
                    SCREEN to screen,
                    LAST_NOTIFICATION to lastNotificationType
                )
                addStackTraceProperty(throwable, properties)
                analyticsProvider.get().trackEvents(ANR_EVENT, properties)
            },
            Executors.newSingleThreadExecutor()
        )
    }

    private fun addStackTraceProperty(throwable: Throwable, properties: MutableMap<String, String>) {
        val stackTraceList = throwable.stackTraceToString().chunked(255)
        stackTraceList
            .subList(0, min(5, stackTraceList.size))
            .forEachIndexed { index, stackTraceChunk ->
                properties["${PropertyValue.STACKTRACE}-$index"] = stackTraceChunk
            }
    }

    companion object {
        const val ANR_EVENT = "Application Not Responding"

        const val WORKERS = "worker_info"
        const val SCREEN = "screen_name"
        const val LAST_NOTIFICATION = "last_notification"
    }
}
