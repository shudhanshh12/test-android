package `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.di

import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContacts
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContacts.Companion.ARG_CAN_SHOW_ADD_MANUALLY_OPTION
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContacts.Companion.ARG_DEFAULT_MODE
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContacts.Companion.ARG_OPEN_FOR_RESULT
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContacts.Companion.ARG_SOURCE
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContactsContract
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContactsViewModel
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.views.AddManuallyView.AddManuallyListener
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.views.ContactItemView.ContactListener
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class AddRelationshipFromContactsModule {

    @Binds
    abstract fun contactListener(fragment: AddRelationshipFromContacts): ContactListener

    @Binds
    abstract fun addManuallyListener(fragment: AddRelationshipFromContacts): AddManuallyListener

    companion object {
        @Provides
        fun initialState(
            @ViewModelParam(AddRelationshipFromContacts.ARG_ADD_RELATIONSHIP_TYPE) relationshipType: Int,
            @ViewModelParam(ARG_CAN_SHOW_ADD_MANUALLY_OPTION) canShowAddManuallyOption: Boolean,
            @ViewModelParam(ARG_SOURCE) source: String,
            @ViewModelParam(ARG_DEFAULT_MODE) defaultMode: String
        ): AddRelationshipFromContactsContract.State = AddRelationshipFromContactsContract.State(
            relationshipType = relationshipType,
            isFragmentOpenForResult = canShowAddManuallyOption.not(),
            source = source,
            defaultMode = defaultMode
        )

        @Provides
        @ViewModelParam(AddRelationshipFromContacts.ARG_ADD_RELATIONSHIP_TYPE)
        fun relationshipType(fragment: AddRelationshipFromContacts): Int =
            fragment.arguments?.getInt(AddRelationshipFromContacts.ARG_ADD_RELATIONSHIP_TYPE)
                ?: throw IllegalStateException("No Relationship Type Found")

        @Provides
        @ViewModelParam(ARG_CAN_SHOW_ADD_MANUALLY_OPTION)
        fun canShowAddManuallyOption(fragment: AddRelationshipFromContacts): Boolean =
            fragment.arguments?.getBoolean(ARG_CAN_SHOW_ADD_MANUALLY_OPTION)
                ?: true

        @Provides
        @ViewModelParam(ARG_SOURCE)
        fun source(fragment: AddRelationshipFromContacts): String =
            fragment.arguments?.getString(ARG_SOURCE) ?: ""

        @Provides
        @ViewModelParam(ARG_DEFAULT_MODE)
        fun defaultMode(fragment: AddRelationshipFromContacts): String =
            fragment.arguments?.getString(ARG_DEFAULT_MODE) ?: ""

        @Provides
        @ViewModelParam(ARG_OPEN_FOR_RESULT)
        fun openForResult(fragment: AddRelationshipFromContacts): Boolean =
            fragment.arguments?.getBoolean(ARG_OPEN_FOR_RESULT) ?: false

        @Provides
        fun viewModel(
            fragment: AddRelationshipFromContacts,
            viewModelProvider: Provider<AddRelationshipFromContactsViewModel>,
        ): MviViewModel<AddRelationshipFromContactsContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
