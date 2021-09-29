package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.BulkReminderTopBannerBinding
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.BulkReminderEpoxyModel.TopBanner
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class TopBanner @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private val binding = BulkReminderTopBannerBinding
        .inflate(LayoutInflater.from(context), this, true)

    private var listener: TopBannerListener? = null

    init {
        binding.root.setOnClickListener {
            listener?.onTopBannerClicked()
        }
    }

    @ModelProp
    fun setTopBanner(topBanner: TopBanner?) {
        topBanner?.also { banner ->
            binding.balanceDue.text = context.getString(
                R.string.rupee_placeholder,
                CurrencyUtil.formatV2(topBanner.totalBalanceDue)
            )
            binding.dueSince.text = context.getString(
                R.string.t_001_daily_remind_pending_for_days,
                banner.defaultedSince.toString()
            )
            binding.totalCustomer.text = context.getString(
                R.string.t_001_daily_remind_cust_count,
                banner.totalCustomers.toString()
            )
        }
    }

    @CallbackProp
    fun setListener(listener: TopBannerListener?) {
        this.listener = listener
    }

    interface TopBannerListener {
        fun onTopBannerClicked()
    }
}
