package `in`.okcredit.voice_first.ui.bulk_add.drafts_list

import `in`.okcredit.voice_first.R
import `in`.okcredit.voice_first.data.bulk_add.entities.*
import `in`.okcredit.voice_first.databinding.ItemDraftTransactionEditableBinding
import `in`.okcredit.voice_first.utils.CurrencyUtil.renderAmount
import `in`.okcredit.voice_first.utils.CurrencyUtil.renderArrowsForCustomer
import `in`.okcredit.voice_first.utils.CurrencyUtil.renderArrowsForSupplier
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.jakewharton.rxbinding3.view.clicks
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import java.util.concurrent.TimeUnit

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class EditableItemView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private lateinit var draftItem: DraftsListItem.DraftItem

    private var selectedMerchant: DraftMerchant? = null

    interface Listener {
        fun onEditDraftClicked(draftTransaction: DraftTransaction)
    }

    private val bindings = ItemDraftTransactionEditableBinding.inflate(LayoutInflater.from(ctx), this, true)

    @CallbackProp
    fun setListener(listener: Listener?) {
        bindings.root.clicks()
            .throttleFirst(50, TimeUnit.MILLISECONDS)
            .doOnNext { listener?.onEditDraftClicked(draftItem.draftTransaction) }
            .subscribe()
    }

    @ModelProp
    fun setDraftItem(draftItem: DraftsListItem.DraftItem) {
        this.draftItem = draftItem
        this.selectedMerchant = draftItem.draftTransaction.draftMerchants?.getOrNull(0)
        render()
    }

    private fun render() {
        bindings.transcript.text = draftItem.draftTransaction.voiceTranscript
        when {
            draftItem.draftTransaction.isComplete() -> renderCompleted()
            else -> renderIncomplete()
        }
    }

    private fun renderIncomplete() {
        bindings.arrows.setImageDrawable(null)
        bindings.amount.text = null

        bindings.title.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_draft_incomplete, 0, 0, 0)
        bindings.title.setText(R.string.t_004_bulk_voice_txn_intermediate_state_incomplete)
        bindings.title.setTextColor(ContextCompat.getColor(context, R.color.orange_ada))

        bindings.note.gone()
        bindings.arrows.gone()
        bindings.amount.gone()
    }

    private fun renderCompleted() {
        draftItem.draftTransaction.amount?.also {
            val isPayment = when (draftItem.draftTransaction.transactionType) {
                TRANSACTION_TYPE_PAYMENT -> true
                TRANSACTION_TYPE_CREDIT -> false
                else -> return@also
            }

            when (selectedMerchant?.merchantType) {
                MERCHANT_TYPE_SUPPLIER -> bindings.arrows.renderArrowsForSupplier(it, isPayment)
                MERCHANT_TYPE_CUSTOMER -> bindings.arrows.renderArrowsForCustomer(it, isPayment)
                else -> bindings.arrows.gone()
            }
            bindings.amount.renderAmount(it)
        }

        val drawableRes = when (selectedMerchant?.merchantType) {
            MERCHANT_TYPE_SUPPLIER -> R.drawable.ic_supplier
            MERCHANT_TYPE_CUSTOMER -> R.drawable.ic_customer
            else -> 0
        }

        bindings.title.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableRes, 0, 0, 0)
        bindings.title.setTextColor(ContextCompat.getColor(context, R.color.grey900))
        bindings.title.text = selectedMerchant?.merchantName

        bindings.note.isVisible = draftItem.draftTransaction.note?.isNotEmpty() == true
        bindings.arrows.visible()
        bindings.amount.visible()
    }
}
