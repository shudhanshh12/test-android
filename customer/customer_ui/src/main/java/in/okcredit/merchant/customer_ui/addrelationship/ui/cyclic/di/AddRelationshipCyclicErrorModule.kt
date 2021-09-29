package `in`.okcredit.merchant.customer_ui.addrelationship.ui.cyclic.di

import `in`.okcredit.merchant.customer_ui.addrelationship.ui.cyclic.AddRelationshipCyclicError
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.cyclic.AddRelationshipCyclicErrorContract
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.cyclic.AddRelationshipCyclicErrorViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class AddRelationshipCyclicErrorModule {

    companion object {
        @Provides
        fun initialState(fragment: AddRelationshipCyclicError): AddRelationshipCyclicErrorContract.State {

            val title = fragment.arguments?.getString(AddRelationshipCyclicError.ARG_TITLE)
            val name = fragment.arguments?.getString(AddRelationshipCyclicError.ARG_NAME)
            val profileImg = fragment.arguments?.getString(AddRelationshipCyclicError.ARG_PROFILE_IMAGE)
            val number = fragment.arguments?.getString(AddRelationshipCyclicError.ARG_NUMBER)
            val description = fragment.arguments?.getString(AddRelationshipCyclicError.ARG_DESCRIPTION)
            val canShowMoveCta = fragment.arguments?.getBoolean(AddRelationshipCyclicError.ARG_TWO_CTA_ENABLED)
            val viewRelationshipType =
                fragment.arguments?.getInt(AddRelationshipCyclicError.ARG_VIEW_RELATIONSHIP_TYPE)
            val moveRelationshipType =
                fragment.arguments?.getInt(AddRelationshipCyclicError.ARG_MOVE_RELATIONSHIP_TYPE)
            val relationshipId = fragment.arguments?.getString(AddRelationshipCyclicError.ARG_RELATIONSHIP_ID)
            val shouldRequireReactivation =
                fragment.arguments?.getBoolean(AddRelationshipCyclicError.ARG_SHOULD_REQUIRE_REACTIVATION, false)
            val typeOfConflict = fragment.arguments?.getString(AddRelationshipCyclicError.ARG_TYPE_OF_CONFLICT)
            val source = fragment.arguments?.getString(AddRelationshipCyclicError.ARG_SOURCE)
            val defaultMode = fragment.arguments?.getString(AddRelationshipCyclicError.ARG_DEFAULT_MODE)

            return AddRelationshipCyclicErrorContract.State(
                headerText = title ?: throw IllegalStateException("Header Text is Missing"),
                name = name ?: throw IllegalStateException("Relation Name is Missing"),
                profile = profileImg,
                mobile = number ?: throw IllegalStateException("Relation Number is Missing"),
                descriptionText = description ?: throw IllegalStateException("Relation Number is Missing"),
                canShowMoveCta = canShowMoveCta ?: false,
                viewRelationshipType = viewRelationshipType,
                moveRelationshipType = moveRelationshipType,
                relationshipId = relationshipId ?: throw IllegalStateException("Relationship Id is Missing"),
                shouldRequireReactivation = shouldRequireReactivation ?: false,
                typeOfConflict = typeOfConflict ?: "",
                source = source ?: "",
                defaultMode = defaultMode ?: "",
            )
        }

        @Provides
        fun viewModel(
            fragment: AddRelationshipCyclicError,
            viewModelProvider: Provider<AddRelationshipCyclicErrorViewModel>,
        ): MviViewModel<AddRelationshipCyclicErrorContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
