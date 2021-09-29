package `in`.okcredit.voice_first.ui.bulk_add.drafts_list

import `in`.okcredit.voice_first.data.bulk_add.entities.DraftProcessingState.*
import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.TypedEpoxyController
import javax.inject.Inject

class DraftsListController @Inject constructor(
    private val retryActionListener: RetryableIemView.Listener,
    private val editActionListener: EditableItemView.Listener,
) : TypedEpoxyController<List<DraftsListItem>>(
    EpoxyAsyncUtil.getAsyncBackgroundHandler(),
    EpoxyAsyncUtil.getAsyncBackgroundHandler(),
) {

    override fun buildModels(data: List<DraftsListItem>) {
        data.forEach {
            when (it) {
                is DraftsListItem.ListSummary -> {
                    countItemView {
                        id("counter")
                        listSummary(it)
                    }
                }
                is DraftsListItem.DraftItem -> when (it.draftState) {
                    PROCESSING,
                    -> nonInteractiveItemView {
                        id(it.draftTransaction.draftTransactionId)
                        draftItem(it)
                    }

                    UNINITIALIZED,
                    FAILED,
                    -> retryableIemView {
                        id(it.draftTransaction.draftTransactionId)
                        draftItem(it)
                        listener(retryActionListener)
                    }

                    INCOMPLETE,
                    COMPLETE,
                    -> editableItemView {
                        id(it.draftTransaction.draftTransactionId)
                        draftItem(it)
                        listener(editActionListener)
                    }
                }
            }
        }
    }
}
