package tech.okcredit.home.ui.reminder.bulk

import `in`.okcredit.backend.utils.CurrencyUtil
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.common.base.Strings
import tech.okcredit.home.databinding.ItemBulkReminderBinding
import java.util.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class BulkReminderCustomerView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    private var listener: ((String) -> Unit)? = null
    private val binding = ItemBulkReminderBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.root.setOnClickListener {
            val id = it.tag as String
            listener?.invoke(id)
        }
    }

    @CallbackProp
    fun setClickListener(listener: ((String) -> Unit)?) {
        this.listener = listener
    }

    @ModelProp
    fun setData(bulkReminderItem: BulkReminderItem) {
        binding.root.tag = bulkReminderItem.customerId
        binding.textCustomerName.text = bulkReminderItem.name
        CurrencyUtil.renderAsSubtitle(binding.textAmountDue, bulkReminderItem.amountDue)
        binding.imageChecked.isVisible = bulkReminderItem.checked

        val defaultPic = TextDrawable
            .builder()
            .buildRound(
                bulkReminderItem.name?.substring(0, 1)?.toUpperCase(Locale.getDefault()) ?: "",
                ColorGenerator.MATERIAL.getColor(bulkReminderItem.name)
            )

        if (Strings.isNullOrEmpty(bulkReminderItem.profilePic)) {
            binding.imageAvatar.setImageDrawable(defaultPic)
        } else {
            Glide.with(binding.imageAvatar)
                .load(bulkReminderItem.profilePic)
                .placeholder(defaultPic)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(binding.imageAvatar)
        }
    }
}
