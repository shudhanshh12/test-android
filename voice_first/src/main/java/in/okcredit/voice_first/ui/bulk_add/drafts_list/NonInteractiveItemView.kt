package `in`.okcredit.voice_first.ui.bulk_add.drafts_list

import `in`.okcredit.voice_first.databinding.ItemDraftTransactionNonInteractiveBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class NonInteractiveItemView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private lateinit var draftItem: DraftsListItem.DraftItem

    private val bindings = ItemDraftTransactionNonInteractiveBinding.inflate(LayoutInflater.from(ctx), this, true)

    @ModelProp
    fun setDraftItem(draftItem: DraftsListItem.DraftItem) {
        this.draftItem = draftItem

        bindings.transcript.text = draftItem.draftTransaction.voiceTranscript
    }
}
