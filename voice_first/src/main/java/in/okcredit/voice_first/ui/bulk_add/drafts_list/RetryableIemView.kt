package `in`.okcredit.voice_first.ui.bulk_add.drafts_list

import `in`.okcredit.voice_first.data.bulk_add.entities.DraftTransaction
import `in`.okcredit.voice_first.databinding.ItemDraftTransactionRetryableBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.jakewharton.rxbinding3.view.clicks
import java.util.concurrent.TimeUnit

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class RetryableIemView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private lateinit var draftItem: DraftsListItem.DraftItem

    interface Listener {
        fun onRetryParseClicked(draftTransaction: DraftTransaction)
    }

    private val bindings = ItemDraftTransactionRetryableBinding.inflate(LayoutInflater.from(ctx), this, true)

    @ModelProp
    fun setDraftItem(draftItem: DraftsListItem.DraftItem) {
        this.draftItem = draftItem

        bindings.transcript.text = draftItem.draftTransaction.voiceTranscript
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        bindings.root.clicks()
            .throttleFirst(50, TimeUnit.MILLISECONDS)
            .doOnNext { listener?.onRetryParseClicked(draftItem.draftTransaction) }
            .subscribe()
    }
}
