package tech.okcredit.home.ui.home.views

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.fileupload._id.GlideApp
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getColor
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.extensions.addRipple
import tech.okcredit.android.base.extensions.dpToPixel
import tech.okcredit.android.base.extensions.getString
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.home.R
import tech.okcredit.home.databinding.HomeCustomerViewV2Binding
import tech.okcredit.home.ui.customer_tab.CustomerTabItem
import tech.okcredit.home.ui.customer_tab.SubtitleType
import tech.okcredit.home.ui.customer_tab.SubtitleType.*
import tech.okcredit.home.utils.TextDrawableUtils
import kotlin.math.roundToInt

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class HomeCustomerViewV2 @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : ConstraintLayout(context, attributeSet, defStyle) {

    private val binding = HomeCustomerViewV2Binding.inflate(LayoutInflater.from(context), this)

    private var viewJob = SupervisorJob()
    private val viewScope = CoroutineScope(Dispatchers.IO + viewJob)

    private var customerSelectionListener: CustomerSelectionListener? = null

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewJob.cancelChildren()
    }

    init {
        binding.root.addRipple()

        binding.root.setPadding(
            context.dpToPixel(16f).roundToInt(),
            context.dpToPixel(12f).roundToInt(),
            context.dpToPixel(16f).roundToInt(),
            0
        )

        binding.root.setOnClickListener {
            val id = it.tag as String
            customerSelectionListener?.onCustomerSelected(id)
        }

        binding.photoImageView.setOnClickListener {
            val id = binding.root.tag as String
            customerSelectionListener?.onCustomerProfileSelected(id)
        }
    }

    @CallbackProp
    fun setListener(listener: CustomerSelectionListener?) {
        this.customerSelectionListener = listener
    }

    @ModelProp
    fun setHomeCustomerItem(customerItem: CustomerTabItem.HomeCustomerItem) {
        binding.root.tag = customerItem.customerId
        binding.nameTextView.text = customerItem.name
        setCommonLedgerVisibility(customerItem.commonLedger, customerItem.addTxnPermissionDenied)
        setCustomerProfilePic(customerItem.name, customerItem.profileImage)
        setCustomerBalance(customerItem.balance)
        setCustomerSubtitle(customerItem.subtitle, customerItem.type)
        setUnreadCount(customerItem.unreadCount)
        setTargetedReferrals(customerItem.showReferralIcon)
    }

    private fun setTargetedReferrals(showReferralIcon: Boolean) {
        if (showReferralIcon) {
            binding.nameTextView.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_orange_circular_gift_icon,
                0
            )
        } else {
            binding.nameTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    private fun setCommonLedgerVisibility(commonLedger: Boolean, addTxnPermissionDenied: Boolean) {
        when {
            addTxnPermissionDenied -> {
                binding.registered.isVisible = true
                binding.registered.setImageResource(R.drawable.ic_add_transaction_disabled_small)
            }
            commonLedger -> {
                binding.registered.isVisible = true
                binding.registered.setImageResource(R.drawable.ic_common_ledger_small)
            }
            else -> {
                binding.registered.isVisible = false
            }
        }
    }

    private fun setUnreadCount(count: Int) = with(binding.tvNewCount) {
        text = if (count == 0) {
            gone()
            ""
        } else {
            visible()
            context.getString(R.string.new_count, count.toString())
        }
    }

    private fun setCustomerSubtitle(subtitle: String, type: SubtitleType) {
        binding.ivSubtitle.isVisible = true
        when (type) {
            IMMUTABLE_CUSTOMER -> {
                binding.ivSubtitle.imageTintList = ColorStateList.valueOf(getColor(context, R.color.orange_1))
                binding.ivSubtitle.setImageResource(R.drawable.ic_icon_sync_problem)
                binding.tvSubtitle.setTextColor(getColor(context, R.color.orange_1))
                binding.tvSubtitle.text = subtitle
            }
            DIRTY_CUSTOMER -> {
                binding.ivSubtitle.imageTintList = ColorStateList.valueOf(getColor(context, R.color.grey700))
                binding.ivSubtitle.setImageResource(R.drawable.ic_sync_pending)
                binding.tvSubtitle.setTextColor(getColor(context, R.color.grey600))
                binding.tvSubtitle.text = HtmlCompat.fromHtml(
                    context.getString(R.string.added_on_new, subtitle),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
            CUSTOMER_ADDED -> {
                binding.ivSubtitle.imageTintList = ColorStateList.valueOf(getColor(context, R.color.grey700))
                binding.ivSubtitle.setImageResource(R.drawable.ic_person_placeholder)
                binding.tvSubtitle.setTextColor(getColor(context, R.color.grey600))
                binding.tvSubtitle.text = HtmlCompat.fromHtml(
                    context.getString(R.string.added_on_new, subtitle),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
            DUE_TODAY -> {
                binding.ivSubtitle.setImageResource(R.drawable.ic_calendar)
                binding.ivSubtitle.imageTintList = ColorStateList.valueOf(getColor(context, R.color.green_primary))
                binding.tvSubtitle.setTextColor(getColor(context, R.color.green_primary))
                binding.tvSubtitle.text = getString(R.string.due_today)
            }
            DUE_DATE_PASSED -> {
                binding.ivSubtitle.setImageResource(R.drawable.ic_calendar)
                binding.ivSubtitle.imageTintList = ColorStateList.valueOf(getColor(context, R.color.red_primary))
                binding.tvSubtitle.setTextColor(getColor(context, R.color.red_primary))
                binding.tvSubtitle.text = HtmlCompat.fromHtml(subtitle, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
            DUE_DATE_INCOMING -> {
                binding.ivSubtitle.setImageResource(R.drawable.ic_calendar)
                binding.ivSubtitle.imageTintList = ColorStateList.valueOf(getColor(context, R.color.grey700))
                binding.tvSubtitle.setTextColor(getColor(context, R.color.grey600))
                binding.tvSubtitle.text = HtmlCompat.fromHtml(
                    context.getString(R.string.due_on_new, subtitle),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
            TRANSACTION_SYNC_DONE -> {
                binding.ivSubtitle.imageTintList = ColorStateList.valueOf(getColor(context, R.color.grey700))
                binding.tvSubtitle.setTextColor(getColor(context, R.color.grey600))
                binding.ivSubtitle.setImageResource(R.drawable.ic_single_tick)
                binding.tvSubtitle.text = HtmlCompat.fromHtml(subtitle, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
            TRANSACTION_SYNC_PENDING -> {
                binding.ivSubtitle.imageTintList = ColorStateList.valueOf(getColor(context, R.color.grey700))
                binding.tvSubtitle.setTextColor(getColor(context, R.color.grey600))
                binding.ivSubtitle.setImageResource(R.drawable.ic_sync_pending)
                binding.tvSubtitle.text = HtmlCompat.fromHtml(subtitle, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
            COLLECTION_TARGETED_REFERRAL -> {
                binding.ivSubtitle.isVisible = false
                binding.tvSubtitle.setTextColor(getColor(context, R.color.grey600))
                binding.tvSubtitle.text = subtitle
            }
        }
    }

    private fun setCustomerBalance(balance: Long) {
        CurrencyUtil.renderV2(balance, binding.tvBalance, balance >= 0)
        binding.tvBalanceStatus.text = if (balance > 0) {
            getString(R.string.advance)
        } else {
            getString(R.string.due)
        }
    }

    private fun setCustomerProfilePic(name: String, profileImage: String?) {
        viewScope.launch {
            val defaultPic = TextDrawableUtils.getRoundTextDrawable(name)

            withContext(Dispatchers.Main) {
                GlideApp.with(context)
                    .load(profileImage)
                    .placeholder(defaultPic)
                    .circleCrop()
                    .error(defaultPic)
                    .fallback(defaultPic)
                    .thumbnail(0.25f)
                    .into(binding.photoImageView)
            }
        }
    }

    interface CustomerSelectionListener {
        fun onCustomerSelected(customerId: String)
        fun onCustomerProfileSelected(customerId: String)
    }
}
