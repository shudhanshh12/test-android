package `in`.okcredit.voice_first.ui._di

import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.voice_first._di.BulkAddTransactions
import `in`.okcredit.voice_first.ui.bulk_add.edit_draft.EditDraftTransactionActivity
import `in`.okcredit.voice_first.ui.bulk_add.edit_draft.EditDraftTransactionContract
import `in`.okcredit.voice_first.ui.bulk_add.edit_draft.EditDraftTransactionViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Named
import javax.inject.Provider

@Module
abstract class EditDraftTransactionActivityModule {

    @Binds
    @BulkAddTransactions
    abstract fun activity(activity: EditDraftTransactionActivity): AppCompatActivity

    companion object {
        internal const val DRAFT_TRANSACTION_KEY = "draft_transaction_key"

        @Provides
        @Named(DRAFT_TRANSACTION_KEY)
        fun draftTransactionKey(activity: EditDraftTransactionActivity): String {
            return activity.intent.getStringExtra(DRAFT_TRANSACTION_KEY) ?: ""
        }

        @Provides
        fun initialState(
            @Named(DRAFT_TRANSACTION_KEY) draftId: String,
        ): EditDraftTransactionContract.State = EditDraftTransactionContract.State(draftId = draftId)

        @Provides
        fun viewModel(
            activity: EditDraftTransactionActivity,
            viewModelProvider: Provider<EditDraftTransactionViewModel>,
        ): MviViewModel<EditDraftTransactionContract.State> = activity.createViewModel(viewModelProvider)
    }
}
