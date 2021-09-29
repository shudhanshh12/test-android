package `in`.okcredit.merchant.customer_ui.addrelationship

import `in`.okcredit.customer.contract.RelationshipType
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.addrelationship.AddRelationshipContract.*
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContacts
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.manually.AddRelationshipManually
import `in`.okcredit.merchant.customer_ui.databinding.ActivityAddRelationshipBinding
import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.os.Bundle
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import android.content.Intent as AndroidIntent

class AddRelationshipActivity : BaseActivity<State, ViewEvent, Intent>(
    "AddRelationshipActivity"
) {

    companion object {
        const val ARG_RELATIONSHIP_TYPE = "arg_relationship_type"
        const val ARG_CAN_SHOW_TUTORIAL = "arg_can_show_tutorial"
        const val ARG_SHOW_MANUAL_FLOW = "arg_show_manual_flow"
        const val ARG_SOURCE = "arg_source_relationship_Activity"
        const val ARG_OPEN_FOR_RESULT = "arg_open_for_result"

        @JvmStatic
        fun getIntent(
            context: Context,
            relationshipType: RelationshipType,
            canShowTutorial: Boolean,
            showManualFlow: Boolean = false,
            openForResult: Boolean = false,
            source: String = "Fab"
        ): AndroidIntent {
            return AndroidIntent(context, AddRelationshipActivity::class.java).apply {
                putExtra(ARG_RELATIONSHIP_TYPE, relationshipType.code)
                putExtra(ARG_CAN_SHOW_TUTORIAL, canShowTutorial)
                putExtra(ARG_SHOW_MANUAL_FLOW, showManualFlow)
                putExtra(ARG_SOURCE, source)
                putExtra(ARG_OPEN_FOR_RESULT, openForResult)
            }
        }
    }

    val binding: ActivityAddRelationshipBinding by viewLifecycleScoped(ActivityAddRelationshipBinding::inflate)

    var relationshipType: Int? = null
    var canShowTutorial: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Base_OKCTheme)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        relationshipType = intent?.getIntExtra(ARG_RELATIONSHIP_TYPE, -1)
        canShowTutorial = intent?.getBooleanExtra(ARG_CAN_SHOW_TUTORIAL, false) ?: false
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun loadIntent(): UserIntent = Intent.Load

    override fun render(state: State) {}

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GoToAddRelationshipTutorial -> gotoAddRelationshipTutorial()
            is ViewEvent.GoToAddRelationshipFromContacts -> gotoAddRelationshipFromContacts(event.openForResult, event.source)
            is ViewEvent.GoToAddRelationshipManually -> gotoAddRelationshipManually(event.openForResult, event.source)
        }
    }

    private fun gotoAddRelationshipManually(openForResult: Boolean, source: String) {
        if (relationshipType == -1 && relationshipType == null)
            throw IllegalStateException("No Relationship Type Found")

        val fragment = AddRelationshipManually.newInstance(
            relationshipType!!,
            source = source,
            defaultMode = "Manual",
            openForResult = openForResult
        )

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .commit()
    }

    private fun gotoAddRelationshipFromContacts(openForResult: Boolean, source: String) {
        if (relationshipType == -1 && relationshipType == null)
            throw IllegalStateException("No Relationship Type Found")

        val fragment = AddRelationshipFromContacts.newInstance(
            relationshipType!!,
            source = source,
            defaultMode = "Contact",
            openForResult = openForResult
        )

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .commit()
    }

    private fun gotoAddRelationshipTutorial() {
    }
}
