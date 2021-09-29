package `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts

import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.views.AddManuallyView
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.views.ContactItemView
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.views.addManuallyView
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.views.addRelationshipHeaderView
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.views.contactItemView
import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.TypedEpoxyController
import javax.inject.Inject

class AddRelationshipFromContactsController @Inject constructor(
    private val listener: ContactItemView.ContactListener,
    private val addManuallyListener: AddManuallyView.AddManuallyListener,
) : TypedEpoxyController<List<AddRelationshipEpoxyModels>>(
    EpoxyAsyncUtil.getAsyncBackgroundHandler(),
    EpoxyAsyncUtil.getAsyncBackgroundHandler()
) {

    override fun buildModels(data: List<AddRelationshipEpoxyModels>?) {
        data?.forEach {
            when (it) {
                is AddRelationshipEpoxyModels.AddRelationshipHeader -> addRelationshipHeader()
                is AddRelationshipEpoxyModels.AddManuallyModel -> addManuallyOptionView(it.name)
                is AddRelationshipEpoxyModels.ContactModel -> addCustomerContactView(it)
            }
        }
    }

    private fun addCustomerContactView(contactModel: AddRelationshipEpoxyModels.ContactModel) {
        contactItemView {
            id("${contactModel.mobile}_$modelCountBuiltSoFar")
            contact(contactModel)
            listener(listener)
        }
    }

    private fun addManuallyOptionView(name: String?) {
        addManuallyView {
            id("addManuallyView")
            name(name ?: "")
            listener(addManuallyListener)
        }
    }

    private fun addRelationshipHeader() {
        addRelationshipHeaderView {
            id("addCustomerHeaderView")
        }
    }
}
