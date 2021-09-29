package `in`.okcredit.merchant.customer_ui.addrelationship

import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContacts
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.di.AddRelationshipFromContactsModule
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.cyclic.AddRelationshipCyclicError
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.cyclic.di.AddRelationshipCyclicErrorModule
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.manually.AddRelationshipManually
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.manually.di.AddRelationshipManuallyModule
import `in`.okcredit.shared.base.MviViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class AddRelationshipActivityModule {

    @Binds
    abstract fun activity(activity: AddRelationshipActivity): AppCompatActivity

    @ContributesAndroidInjector(modules = [AddRelationshipFromContactsModule::class])
    abstract fun addRelationshipFromContacts(): AddRelationshipFromContacts

    @ContributesAndroidInjector(modules = [AddRelationshipManuallyModule::class])
    abstract fun addRelationshipManually(): AddRelationshipManually

    @ContributesAndroidInjector(modules = [AddRelationshipCyclicErrorModule::class])
    abstract fun addRelationshipCyclicError(): AddRelationshipCyclicError

    companion object {
        @Provides
        fun initialState(): AddRelationshipContract.State = AddRelationshipContract.State

        @Provides
        @ViewModelParam(AddRelationshipActivity.ARG_RELATIONSHIP_TYPE)
        fun relationshipType(activity: AddRelationshipActivity): Int {
            val relationshipType = activity.intent?.getIntExtra(AddRelationshipActivity.ARG_RELATIONSHIP_TYPE, -1)
            return if (relationshipType != -1 && relationshipType != null) {
                relationshipType
            } else {
                throw IllegalStateException("No Relationship Type Found")
            }
        }

        @Provides
        @ViewModelParam(AddRelationshipActivity.ARG_CAN_SHOW_TUTORIAL)
        fun canShowTutorial(activity: AddRelationshipActivity): Boolean {
            return activity.intent?.getBooleanExtra(AddRelationshipActivity.ARG_CAN_SHOW_TUTORIAL, false)
                ?: false
        }

        @Provides
        @ViewModelParam(AddRelationshipActivity.ARG_SHOW_MANUAL_FLOW)
        fun showManualFlow(activity: AddRelationshipActivity): Boolean {
            return activity.intent?.getBooleanExtra(AddRelationshipActivity.ARG_SHOW_MANUAL_FLOW, false)
                ?: false
        }

        @Provides
        @ViewModelParam(AddRelationshipActivity.ARG_OPEN_FOR_RESULT)
        fun openForResult(activity: AddRelationshipActivity): Boolean {
            return activity.intent?.getBooleanExtra(AddRelationshipActivity.ARG_OPEN_FOR_RESULT, false)
                ?: false
        }

        @Provides
        @ViewModelParam(AddRelationshipActivity.ARG_SOURCE)
        fun source(activity: AddRelationshipActivity): String {
            return activity.intent?.getStringExtra(AddRelationshipActivity.ARG_SOURCE)
                ?: ""
        }

        @Provides
        fun viewModel(
            fragment: AddRelationshipActivity,
            viewModelProvider: Provider<AddRelationshipViewModel>,
        ): MviViewModel<AddRelationshipContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
