package `in`.okcredit.merchant.customer_ui.addrelationship.ui.tutorial

import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContactsContract.*
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import io.reactivex.Observable

class AddRelationshipTutorial : BaseFragment<State, ViewEvent, Intent>("AddRelationshipManually") {

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun render(state: State) {}

    override fun handleViewEvent(event: ViewEvent) {}
}
