package `in`.okcredit.merchant.customer_ui.addrelationship.ui.manually.di

import `in`.okcredit.merchant.customer_ui.addrelationship.ui.manually.AddRelationshipManually
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.manually.AddRelationshipManuallyContract
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.manually.AddRelationshipManuallyViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class AddRelationshipManuallyModule {

    companion object {
        @Provides
        fun initialState(
            @ViewModelParam(AddRelationshipManually.ARG_ADD_RELATIONSHIP_TYPE) relationshipType: Int,
            @ViewModelParam(AddRelationshipManually.ARG_NAME) name: String,
            @ViewModelParam(AddRelationshipManually.ARG_SOURCE) source: String,
            @ViewModelParam(AddRelationshipManually.ARG_DEFAULT_MODE) defaultMode: String,
        ): AddRelationshipManuallyContract.State = AddRelationshipManuallyContract.State(
            relationshipType = relationshipType,
            name = name,
            source = source,
            defaultMode = defaultMode
        )

        @Provides
        @ViewModelParam(AddRelationshipManually.ARG_ADD_RELATIONSHIP_TYPE)
        fun relationshipType(fragment: AddRelationshipManually): Int =
            fragment.arguments?.getInt(AddRelationshipManually.ARG_ADD_RELATIONSHIP_TYPE)
                ?: throw IllegalStateException("No Relationship Type Found")

        @Provides
        @ViewModelParam(AddRelationshipManually.ARG_NAME)
        fun getName(fragment: AddRelationshipManually): String =
            fragment.arguments?.getString(AddRelationshipManually.ARG_NAME)
                ?: ""

        @Provides
        @ViewModelParam(AddRelationshipManually.ARG_SOURCE)
        fun source(fragment: AddRelationshipManually): String =
            fragment.arguments?.getString(AddRelationshipManually.ARG_SOURCE)
                ?: ""

        @Provides
        @ViewModelParam(AddRelationshipManually.ARG_DEFAULT_MODE)
        fun defaultMode(fragment: AddRelationshipManually): String =
            fragment.arguments?.getString(AddRelationshipManually.ARG_DEFAULT_MODE)
                ?: ""

        @Provides
        @ViewModelParam(AddRelationshipManually.ARG_OPEN_FOR_RESULT)
        fun openForResult(fragment: AddRelationshipManually): Boolean =
            fragment.arguments?.getBoolean(AddRelationshipManually.ARG_OPEN_FOR_RESULT)
                ?: false

        @Provides
        fun viewModel(
            fragment: AddRelationshipManually,
            viewModelProvider: Provider<AddRelationshipManuallyViewModel>,
        ): MviViewModel<AddRelationshipManuallyContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
