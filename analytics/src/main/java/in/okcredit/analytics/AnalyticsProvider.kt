package `in`.okcredit.analytics

import `in`.okcredit.analytics.appsflyer.AppsFlyerEventsConsumer
import `in`.okcredit.analytics.clevertap.CleverTapEventsConsumer
import `in`.okcredit.analytics.crashlytics.CrashlyticsEventsConsumer
import `in`.okcredit.analytics.firebase.FirebaseEventsConsumer
import `in`.okcredit.analytics.logger.DebugLogger
import `in`.okcredit.analytics.mixpanel.MixpanelEventsConsumer
import dagger.Lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.base.crashlytics.RecordException.recordException
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.utils.debug
import java.util.*
import javax.inject.Inject

@AppScope
class AnalyticsProvider @Inject constructor(
    mixpanel: Lazy<MixpanelEventsConsumer>,
    clevertap: Lazy<CleverTapEventsConsumer>,
    firebase: Lazy<FirebaseEventsConsumer>,
    debugLogger: Lazy<DebugLogger>,
    crashlytics: Lazy<CrashlyticsEventsConsumer>,
    appsFlyer: Lazy<AppsFlyerEventsConsumer>,
) : IAnalyticsProvider {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Problem
     * Sequentially fired events are not guaranteed to be logged in the same sequence. This happens due to use of launch
     *
     * eg : Code that fires events in order of [1,2,3] might have events logged as [2,1,3].
     * We recently saw this while testing SyncCustomerCommands engineering events. This would inevitably break funnels
     * due to events lost to bad ordering
     *
     * Solution
     * Use locks and FIFOs (channel) to ensure that events are sequentially consumed/logged
     */
    private val fifo = Channel<AnalyticsLoggingRequest>(capacity = UNLIMITED)
    private val readLock = Mutex()

    private val exhaustiveEventsConsumer = HashSet<AnalyticEventsConsumer>()
    private val engineeringOnlyEventsConsumer = HashSet<AnalyticEventsConsumer>()

    init {
        exhaustiveEventsConsumer.add(mixpanel.get())
        exhaustiveEventsConsumer.add(clevertap.get())
        exhaustiveEventsConsumer.add(firebase.get())
        exhaustiveEventsConsumer.add(crashlytics.get())
        exhaustiveEventsConsumer.add(appsFlyer.get())
        debug {
            exhaustiveEventsConsumer.add(debugLogger.get())
            engineeringOnlyEventsConsumer.add(debugLogger.get())
        }
        engineeringOnlyEventsConsumer.add(mixpanel.get())
        engineeringOnlyEventsConsumer.add(firebase.get())
    }

    override fun setIdentity(id: String, isSignup: Boolean) {
        exhaustiveEventsConsumer.forEach { it.setIdentity(id, isSignup) }
    }

    @NonNls
    override fun setUserProperty(properties: Map<String, Any>) {
        exhaustiveEventsConsumer.forEach { it.setUserProperty(properties.toMap()) }
    }

    override fun registerSuperProperties(properties: Map<String, Any>) {
        exhaustiveEventsConsumer.forEach { it.registerSuperProperties(properties.toMap()) }
    }

    override fun setSuperProperties(properties: Map<String, Any>) {
        exhaustiveEventsConsumer.forEach { it.setSuperProperties(properties) }
    }

    override fun incrementTransactionCountSuperProperty() {
        exhaustiveEventsConsumer.forEach { it.incrementTransactionCountSuperProperty() }
    }

    override fun incrementCustomerCountSuperProperty() {
        exhaustiveEventsConsumer.forEach { it.incrementCustomerCountSuperProperty() }
    }

    @NonNls
    override fun trackEvents(eventName: String, properties: Map<String, Any>?) =
        trackEvents(eventName, properties, exhaustiveEventsConsumer)

    override fun flushEvents() {
        exhaustiveEventsConsumer.forEach { it.flushEvents() }
    }

    override fun logBreadcrumb(log: String, properties: Map<String, Any>?) {
        val map = (properties?.toMutableMap() ?: mutableMapOf()).apply {
            this["log"] = log
        }
        trackEngineeringMetricEvents(Event.BREADCRUMB, map)
    }

    override fun trackObjectViewed(`object`: String, properties: Map<String, Any>?) {
        trackEvents("$`object` ${Action.VIEWED}", properties)
    }

    override fun trackObjectInteracted(
        `object`: String,
        interactionType: InteractionType,
        properties: Map<String, Any>?,
    ) {
        val map = (properties?.toMutableMap() ?: mutableMapOf()).apply {
            this[Property.INTERACTION_TYPE] = interactionType.value
        }
        trackEvents("$`object` ${Action.INTERACTED}", map)
    }

    override fun trackObjectError(`object`: String, errorMessage: String, properties: Map<String, Any>?) {
        val map = (properties?.toMutableMap() ?: mutableMapOf()).apply {
            this[Property.MESSAGE] = errorMessage
        }
        trackEvents("$`object` Error", map)
    }

    @NonNls
    override fun trackEngineeringMetricEvents(eventName: String, properties: Map<String, Any>?) {
        trackEvents(eventName, properties, engineeringOnlyEventsConsumer)
    }

    private fun trackEvents(
        eventName: String,
        properties: Map<String, Any>?,
        providers: HashSet<AnalyticEventsConsumer>,
    ) {
        fifo.trySend(AnalyticsLoggingRequest(eventName, properties, providers))
        scope.launch { consumePendingEvents() }
    }

    private suspend fun consumePendingEvents() {
        readLock.withLock {
            while (true) {
                fifo.tryReceive().getOrNull()?.apply {
                    runCatching { providers.forEach { it.trackEvents(eventName, properties) } }
                        .exceptionOrNull()
                        ?.also { recordException(RuntimeException("Error tracking in analytics events", it)) }
                } ?: break
            }
        }
    }

    private class AnalyticsLoggingRequest(
        val eventName: String,
        val properties: Map<String, Any>?,
        val providers: HashSet<AnalyticEventsConsumer>,
    )

    override fun clearIdentity() {
        exhaustiveEventsConsumer.forEach { it.clearIdentity() }
    }
}
