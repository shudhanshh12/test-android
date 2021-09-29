package tech.okcredit.home.usecase.dashboard

import `in`.okcredit.dynamicview.R
import `in`.okcredit.dynamicview.Targets
import `in`.okcredit.dynamicview.component.dashboard.cell2.Cell2ComponentModel
import `in`.okcredit.dynamicview.component.dashboard.recycler_card.RecyclerCardComponentModel
import `in`.okcredit.dynamicview.component.dashboard.summary_card.SummaryCardComponentModel
import `in`.okcredit.dynamicview.component.recycler.RecyclerComponentModel
import `in`.okcredit.dynamicview.data.model.Action
import `in`.okcredit.dynamicview.data.model.ComponentModel
import `in`.okcredit.dynamicview.data.model.Customization
import `in`.okcredit.dynamicview.events.ClickEventHandler
import `in`.okcredit.dynamicview.events.ViewEventHandler
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.TempCurrencyUtil
import tech.okcredit.android.base.extensions.ifLet
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.home.ui.activity.HomeActivityContract.Companion.FEATURE_HOME_DASHBOARD
import tech.okcredit.home.ui.dashboard.DashboardViewModel
import tech.okcredit.home.usecase.GetCustomization
import tech.okcredit.home.usecase.dashboard.CollectionDefaultersValueProvider.Defaulter
import tech.okcredit.home.usecase.dashboard.CollectionValueProvider.Companion.COLLECTION
import tech.okcredit.home.usecase.dashboard.DashboardValueProvider.Request
import tech.okcredit.home.usecase.dashboard.NetBalanceValueProvider.Companion.NET_BALANCE
import timber.log.Timber
import javax.inject.Inject

class GetDashboardCustomizationWithValues @Inject constructor(
    private val getCustomization: Lazy<GetCustomization>,
    private val getDashboardValues: Lazy<GetDashboardValues>,
    private val context: Lazy<Context>,
    private val localeManager: Lazy<LocaleManager>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(): Observable<Customization> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            getCustomization.get().getCustomizationOrFallbackForTarget(Targets.DASHBOARD, businessId)
                .flatMap { customization ->
                    getDashboardValues.get().execute(requestForDashboardValues(customization))
                        .map { updateValuesInCustomization(customization, it) }
                }
        }
    }

    private fun requestForDashboardValues(customization: Customization): HashMap<String, Request?> {
        val component = customization.component as? RecyclerComponentModel
        val requestMap = hashMapOf<String, Request?>()
        component?.items?.forEach { item ->
            when (item) {
                is SummaryCardComponentModel -> {
                    item.metadata?.name?.let {
                        requestMap[it] = item.metadata?.duration?.let { duration -> Request(duration) }
                    }
                }
                is RecyclerCardComponentModel -> {
                    if (item.metadata?.name == CollectionDefaultersValueProvider.COLLECTION_DEFAULTERS) {
                        requestMap[CollectionDefaultersValueProvider.COLLECTION_DEFAULTERS] =
                            item.metadata?.itemCount?.let { itemCount -> Request(itemCount) }
                    }
                }
            }
        }
        return requestMap
    }

    private fun updateValuesInCustomization(
        customization: Customization,
        values: Map<String, DashboardValueProvider.Response>,
    ): Customization {
        val component = customization.component as? RecyclerComponentModel
        val itemList = ArrayList<ComponentModel>()
        component?.items?.forEach { item ->
            when (item) {
                is SummaryCardComponentModel -> {
                    values[item.metadata?.name]?.let {
                        updateSummaryCardValues(item, it)?.let { updatedItem -> itemList.add(updatedItem) }
                    }
                }
                is RecyclerCardComponentModel -> {
                    if (item.metadata?.name == CollectionDefaultersValueProvider.COLLECTION_DEFAULTERS) {
                        values[CollectionDefaultersValueProvider.COLLECTION_DEFAULTERS]?.let {
                            updateCollectionDefaultersValues(item, it)?.let { updatedItem -> itemList.add(updatedItem) }
                        }
                    } else {
                        itemList.add(item)
                    }
                }
                else -> itemList.add(item)
            }
        }
        return customization.copy(component = component?.copy(items = itemList))
    }

    private fun updateSummaryCardValues(
        item: SummaryCardComponentModel,
        dashboardValue: DashboardValueProvider.Response,
    ): ComponentModel? {
        return when {
            dashboardValue is NetBalanceValueProvider.NetBalanceDashboardValue && item.metadata?.name == NET_BALANCE ->
                item.copy(
                    value = dashboardValue.value,
                    valueDescription = dashboardValue.string,
                    eventHandlers = addTrackerPropertiesInEventHandlers(
                        item.eventHandlers,
                        "Value" to dashboardValue.value.toString()
                    )
                )
            dashboardValue is CollectionValueProvider.CollectionDashboardValue && item.metadata?.name == COLLECTION ->
                item.copy(
                    value = dashboardValue.value,
                    subtitle = dashboardValue.string,
                    eventHandlers = addTrackerPropertiesInEventHandlers(
                        item.eventHandlers,
                        "Value" to dashboardValue.value.toString()
                    )
                )
            else -> {
                Timber.e("${DashboardViewModel.TAG}: Unknown type of summary card: $item")
                null
            }
        }
    }

    private fun addTrackerPropertiesInEventHandlers(
        eventHandlers: Map<String, Set<Action>>?,
        vararg propertiesToBeAdded: Pair<String, String>,
    ): Map<String, Set<Action>>? {
        val updatedEventHandlers = mutableMapOf<String, Set<Action>>()
        eventHandlers?.forEach { eventHandler ->
            val actionSet = mutableSetOf<Action>()
            eventHandler.value.forEach { action ->
                if (action is Action.Track) {
                    val properties = action.properties.toMutableMap()
                    properties.putAll(propertiesToBeAdded)
                    actionSet.add(action.copy(properties = properties))
                } else {
                    actionSet.add(action)
                }
            }
            updatedEventHandlers[eventHandler.key] = actionSet
        }
        return updatedEventHandlers
    }

    private fun updateCollectionDefaultersValues(
        item: RecyclerCardComponentModel,
        dashboardValue: DashboardValueProvider.Response,
    ): ComponentModel? {
        val value = dashboardValue as CollectionDefaultersValueProvider.CollectionDefaultersDashboardValue
        return if (value.defaulters != null && value.defaulters.isNotEmpty()) {
            val defaulterItems = buildDefaulterItems(item, value.defaulters)

            var ctaText = item.ctaText
            ifLet(item.metadata?.spanCount, defaulterItems?.size) { spanCount, listSize ->
                if (listSize < spanCount) ctaText = null // Hide cta text if less than span count
            }
            item.copy(
                title = item.title?.let { it + value.string },
                ctaText = ctaText,
                items = defaulterItems
            )
        } else {
            null // hide view when defaulter list is empty
        }
    }

    private fun buildDefaulterItems(
        item: RecyclerCardComponentModel,
        defaulters: List<Defaulter>,
    ): List<ComponentModel>? {
        return defaulters.mapIndexed { index, defaulter ->
            Cell2ComponentModel(
                version = item.version,
                kind = Cell2ComponentModel.KIND,
                metadata = ComponentModel.Metadata(
                    name = "CollectionDefaulters",
                    lang = localeManager.get().getLanguage(),
                    feature = FEATURE_HOME_DASHBOARD
                ),
                eventHandlers = getDefaulterItemEventHandlers(defaulter, index, defaulters.size),
                title = defaulter.description,
                subtitle = context.get()
                    .getString(R.string.rupee_placeholder, TempCurrencyUtil.formatV2(defaulter.balance)),
                icon = defaulter.profileImage
            )
        }
    }

    private fun getDefaulterItemEventHandlers(
        defaulter: Defaulter,
        index: Int,
        listSize: Int,
    ): Map<String, Set<Action>> {
        val properties = mapOf(
            "Component Name" to "Defaulters",
            "Sub-Component Position" to index.plus(1).toString(),
            "Sub-Component Count" to listSize.toString(),
            "Sub-Component Value" to defaulter.id
        )
        return mapOf(
            ViewEventHandler.EVENT_KEY to setOf(Action.Track("Dashboard component viewed", properties)),
            ClickEventHandler.EVENT_KEY to setOf(
                Action.Track("Dashboard component clicked", properties),
                Action.Navigate(defaulter.onClickDeeplink)
            )
        )
    }
}
