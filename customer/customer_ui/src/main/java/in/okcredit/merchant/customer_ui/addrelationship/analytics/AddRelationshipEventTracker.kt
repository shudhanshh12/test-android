package `in`.okcredit.merchant.customer_ui.addrelationship.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import dagger.Lazy
import javax.inject.Inject

class AddRelationshipEventTracker @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>,
) {
    companion object {
        const val ADD_RELATIONSHIP_FAILED = "Add Relationship Failed"
        const val VIEW_ADD_RELATIONSHIP_CONFLICT_DIALOG = "View Add Relationship Conflict Dialog"
        const val ADD_RELATIONSHIP_CONFLICT_CTA_CLICKED = "Add Relationship Conflict CTA Clicked"
        const val ADD_RELATIONSHIP_CONFIRM = "Add Relationship Confirm"
        const val SELECT_CONTACT = "Select Contact"
        const val ADD_RELATIONSHIP_CONTACT_SEARCH_USED = "Add Relationship Contact Search Used"
        const val VIEW_PERMISSION_DIALOG = "View Permission Dialog"
    }

    object PropertyKey {
        const val RELATION = "Relation"
        const val SOURCE = "Source"
        const val DEFAULT_MODE = "Default Mode"
        const val FLOW = "Flow"
        const val REASON = "Reason"
        const val TYPE = "Type"
        const val CTA = "cta"
        const val Exception = "Exception"
        const val PRIMARY_CTA = "primary_cta"
        const val SECONDARY_CTA = "secondary_cta"
        const val CONFLICTING_RELATION = "conflicting_relation"
    }

    fun trackAddTransactionFailed(
        reason: String,
        type: String,
        exception: String,
        flow: String,
        relation: String,
        source: String,
        defaultMode: String,
    ) {
        val propertiesMap = mapOf(
            PropertyKey.REASON to reason,
            PropertyKey.TYPE to type,
            PropertyKey.Exception to exception,
            PropertyKey.FLOW to flow,
            PropertyKey.RELATION to relation,
            PropertyKey.SOURCE to source,
            PropertyKey.DEFAULT_MODE to defaultMode
        )
        analyticsProvider.get().trackEvents(ADD_RELATIONSHIP_FAILED, propertiesMap)
    }

    fun trackViewAddTransactionConflictDialog(
        type: String,
        primaryCta: Boolean,
        secondaryCTA: Boolean,
        flow: String,
        relation: String,
        source: String,
        defaultMode: String,
    ) {
        val propertiesMap = mapOf(
            PropertyKey.TYPE to type,
            PropertyKey.PRIMARY_CTA to primaryCta,
            PropertyKey.SECONDARY_CTA to secondaryCTA,
            PropertyKey.FLOW to flow,
            PropertyKey.RELATION to relation,
            PropertyKey.SOURCE to source,
            PropertyKey.DEFAULT_MODE to defaultMode
        )
        analyticsProvider.get().trackEvents(VIEW_ADD_RELATIONSHIP_CONFLICT_DIALOG, propertiesMap)
    }

    fun trackAddRelationshipConflictCTAClicked(
        type: String,
        cta: String,
        flow: String,
        relation: String,
        source: String,
        defaultMode: String,
    ) {
        val propertiesMap = mapOf(
            PropertyKey.TYPE to type,
            PropertyKey.CTA to cta,
            PropertyKey.FLOW to flow,
            PropertyKey.CONFLICTING_RELATION to relation,
            PropertyKey.SOURCE to source,
            PropertyKey.DEFAULT_MODE to defaultMode
        )
        analyticsProvider.get().trackEvents(ADD_RELATIONSHIP_CONFLICT_CTA_CLICKED, propertiesMap)
    }

    fun trackAddRelationshipConfirm(
        flow: String,
        relation: String,
        source: String,
        defaultMode: String,
    ) {
        val propertiesMap = mapOf(
            PropertyKey.FLOW to flow,
            PropertyKey.RELATION to relation,
            PropertyKey.SOURCE to source,
            PropertyKey.DEFAULT_MODE to defaultMode
        )

        analyticsProvider.get().trackEvents(ADD_RELATIONSHIP_CONFIRM, propertiesMap)
    }

    fun trackSelectContact(
        flow: String,
        relation: String,
        source: String,
        defaultMode: String,
    ) {
        val propertiesMap = mapOf(
            PropertyKey.FLOW to flow,
            PropertyKey.RELATION to relation,
            PropertyKey.SOURCE to source,
            PropertyKey.DEFAULT_MODE to defaultMode
        )
        analyticsProvider.get().trackEvents(SELECT_CONTACT, propertiesMap)
    }

    fun trackAddRelationshipContactSearchUsed(
        flow: String,
        relation: String,
        source: String,
        defaultMode: String,
    ) {
        val propertiesMap = mapOf(
            PropertyKey.FLOW to flow,
            PropertyKey.RELATION to relation,
            PropertyKey.SOURCE to source,
            PropertyKey.DEFAULT_MODE to defaultMode
        )
        analyticsProvider.get().trackEvents(ADD_RELATIONSHIP_CONTACT_SEARCH_USED, propertiesMap)
    }

    fun trackViewPermissionDialog(
        flow: String,
        relation: String,
        source: String,
        defaultMode: String,
    ) {
        val propertiesMap = mapOf(
            PropertyKey.FLOW to flow,
            PropertyKey.RELATION to relation,
            PropertyKey.SOURCE to source,
            PropertyKey.DEFAULT_MODE to defaultMode
        )
        analyticsProvider.get().trackEvents(VIEW_PERMISSION_DIALOG, propertiesMap)
    }
}
