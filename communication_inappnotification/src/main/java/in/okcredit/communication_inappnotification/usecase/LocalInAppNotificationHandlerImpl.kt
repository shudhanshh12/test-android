package `in`.okcredit.communication_inappnotification.usecase

import `in`.okcredit.communication_inappnotification.contract.LocalInAppNotificationHandler
import `in`.okcredit.communication_inappnotification.contract.ui.local.TapTargetLocal
import `in`.okcredit.communication_inappnotification.contract.ui.local.TooltipLocal
import `in`.okcredit.communication_inappnotification.usecase.builder.InAppNotificationUiBuilder
import `in`.okcredit.communication_inappnotification.usecase.render.LocalInAppNotificationRenderer
import `in`.okcredit.communication_inappnotification.usecase.render.TapTargetRenderer
import `in`.okcredit.communication_inappnotification.usecase.render.TooltipRenderer
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.skydoves.balloon.Balloon
import dagger.Lazy
import kotlinx.coroutines.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.lang.ref.WeakReference
import javax.inject.Inject

class LocalInAppNotificationHandlerImpl @Inject constructor(
    private val renderers: Lazy<Map<String, @JvmSuppressWildcards LocalInAppNotificationRenderer>>,
) : LocalInAppNotificationHandler(), LifecycleObserver {

    private lateinit var uiBuildersList: MutableList<InAppNotificationUiBuilder?>
    private val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(supervisorJob + Dispatchers.IO)

    override suspend fun generateTooltipDelegate(
        weakScreen: WeakReference<FragmentActivity>,
        tooltip: TooltipLocal,
    ): Balloon? {
        return withContext(scope.coroutineContext) {
            val tooltipRenderer = renderers.get()[TooltipLocal.KIND] as? TooltipRenderer
            val tooltipBuilder = tooltipRenderer?.renderLocalNotification(weakScreen, tooltip)
            uiBuildersList.add(tooltipBuilder)

            tooltipBuilder?.build()
        }
    }

    override suspend fun generateTapTargetDelegate(
        weakScreen: WeakReference<FragmentActivity>,
        tapTarget: TapTargetLocal,
    ): MaterialTapTargetPrompt? {
        return withContext(scope.coroutineContext) {
            val tapTargetRenderer = renderers.get()[TapTargetLocal.KIND] as? TapTargetRenderer
            val tapTargetBuilder = tapTargetRenderer?.renderLocalNotification(weakScreen, tapTarget)
            uiBuildersList.add(tapTargetBuilder)
            tapTargetBuilder?.build()
        }
    }

    /**
     * Initializes the uiBuilderList when the client requests to generate in-app notification for the first time
     * @see LocalInAppNotificationHandler is a lifecycle aware component and in this function, it is observing
     * client's lifecycle
     */
    override fun ensureLifecycleAware(
        weakScreen: WeakReference<FragmentActivity>,
    ) {
        if (!this::uiBuildersList.isInitialized) {
            uiBuildersList = mutableListOf()
            weakScreen.get()?.lifecycle?.addObserver(this)
        }
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        supervisorJob.cancelChildren()
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        if (!this::uiBuildersList.isInitialized) {
            return
        }
        for (uiBuilder in uiBuildersList) {
            uiBuilder?.removeReferences()
        }
        uiBuildersList.clear()
    }
}
