package `in`.okcredit.collection_ui.ui.defaulters

import `in`.okcredit.backend._offline.model.CustomerLastActivity
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.DefaulterViewBinding
import `in`.okcredit.collection_ui.ui.defaulters.model.Defaulter
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.fileupload.usecase.IImageLoader
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.coroutines.*
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.TextDrawableUtils
import java.util.*
import javax.inject.Inject

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class DefaulterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var viewJob = SupervisorJob()
    private val viewScope = CoroutineScope(Dispatchers.IO + viewJob)

    private val binding = DefaulterViewBinding.inflate(LayoutInflater.from(context), this, true)

    interface DefaulterClickListener {
        fun onDefaulterClick(customerId: String)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewJob.cancelChildren()
    }

    private fun getDateFormat(context: Context?): DateTimeFormatter {
        return DateTimeFormat.forPattern("dd MMM, YYYY")
            .withLocale(Locale(LocaleManager.getLanguageForDateFormat(context!!)))
    }

    private var hasUnSyncTransactions: Boolean = false
    private lateinit var customer: Customer

    @Inject
    lateinit var imageLoader: IImageLoader

    private var commonLedger: Boolean = false

    @ModelProp
    fun setDefaulter(defaulter: Defaulter) {
        this.customer = defaulter.customer
        this.hasUnSyncTransactions = defaulter.hasUnSyncTransactions
        setName()
        setProfilePhoto()
        setSubtitle(defaulter)
        setBalance()
        setBalanceStatus()
        setDueWarning()
        setCommonLedgerVisibility(defaulter.isSupplierRegistered)
    }

    private fun setDueWarning() {
        if (customer.dueWarningDrawable != 0) {
            binding.photoImageView.background = ContextCompat.getDrawable(context, customer.dueWarningDrawable)
        } else binding.photoImageView.background = ContextCompat.getDrawable(context, android.R.color.transparent)
    }

    private fun setCommonLedgerVisibility(isCommonLedger: Boolean) {
        commonLedger = isCommonLedger
        if (isCommonLedger) {
            binding.registered.visible()
            if (customer.isAddTransactionPermissionDenied()) {
                binding.registered.setImageResource(R.drawable.ic_add_transaction_disabled_small)
            } else {
                binding.registered.setImageResource(R.drawable.ic_common_ledger_small)
            }
        } else {
            binding.registered.gone()
        }
    }

    private fun setName() {
        binding.nameTextView.text = customer.description
    }

    private fun setProfilePhoto() {
        val name = customer.description
        val profilePhoto = customer.profileImage

        viewScope.launch {
            val defaultPic = TextDrawableUtils.getRoundTextDrawable(name)
            withContext(Dispatchers.Main) {
                GlideApp.with(context)
                    .load(profilePhoto)
                    .placeholder(defaultPic)
                    .circleCrop()
                    .error(defaultPic)
                    .fallback(defaultPic)
                    .thumbnail(0.25f)
                    .into(binding.photoImageView)
            }
        }
    }

    private fun setSubtitle(defaulter: Defaulter) {
        val lastActivity = customer.lastActivity
        val createdAt = customer.createdAt

        if (customer.dueActive && customer.dueInfo_activeDate != null && customer.balanceV2 < 0) {
            binding.customerAdded.gone()
            binding.transactionInfo.gone()
            binding.transactionAmount.gone()
            binding.dueInfo.visible()
            binding.dueDateInfo.visible()
            binding.transactionAmountSubtitleDateAb.gone()
            setDueDate()
        } else if (customer.lastActivity != createdAt) {
            binding.customerAdded.gone()
            binding.transactionInfo.visible()
            binding.dueInfo.gone()
            binding.dueDateInfo.gone()
            binding.transactionAmount.visible()

            binding.transactionAmountSubtitleDateAb.visible()
            customer.lastActivityMetaInfo?.let { setSubtitleAndDateString(lastActivity, it, context) }

            binding.ivSubtitleInfo.visible()
            if (hasUnSyncTransactions) {
                binding.ivSubtitleInfo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sync_pending))
            } else {
                binding.ivSubtitleInfo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_single_tick))
            }
            binding.ivSubtitleInfo.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.grey700))
        } else if (customer.lastActivity == createdAt) {
            binding.transactionInfo.gone()
            binding.transactionAmount.gone()
            binding.dueInfo.gone()
            binding.dueDateInfo.gone()
            binding.transactionAmountSubtitleDateAb.gone()
            binding.customerAdded.visible()
            val date = createdAt?.toString(
                getDateFormat(
                    context
                )
            )
            binding.customerAdded.text = HtmlCompat.fromHtml(
                context.getString(R.string.added_on_new, date),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )

            binding.ivSubtitleInfo.visible()
            binding.ivSubtitleInfo.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_person_placeholder
                )
            )
            binding.ivSubtitleInfo.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.grey700))
        }
    }

    private fun setDueDate() {
        binding.ivSubtitleInfo.visible()
        when {
            DateTimeUtils.isCurrentDate(customer.dueInfo_activeDate) -> {
                binding.dueInfo.text = context.getString(R.string.due_today)
                binding.dueInfo.setTextColor(ContextCompat.getColor(context, R.color.green_primary))
                binding.ivSubtitleInfo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_calendar))
                binding.ivSubtitleInfo.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.green_primary))
                binding.dueDateInfo.text = ""
            }
            DateTimeUtils.isDatePassed(customer.dueInfo_activeDate) -> {
                binding.dueInfo.setTextColor(ContextCompat.getColor(context, R.color.red_primary))
                binding.ivSubtitleInfo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_calendar))
                binding.ivSubtitleInfo.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.red_primary))
                getCollectionDateStrings(customer.dueInfo_activeDate, context)
            }
            DateTimeUtils.isFutureDate(customer.dueInfo_activeDate) -> {

                binding.dueInfo.setTextColor(ContextCompat.getColor(context, R.color.grey700))
                binding.ivSubtitleInfo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_calendar))
                binding.ivSubtitleInfo.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.grey700))
                val date = DateTimeUtils.getFormat2(context, customer.dueInfo_activeDate)
                binding.dueInfo.text = HtmlCompat.fromHtml(
                    context.getString(R.string.due_on_new, date),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
        }
        if (hasUnSyncTransactions) {
            binding.ivSubtitleInfo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sync_pending))
        }
    }

    private fun getCollectionDateStrings(dueinfoActivedate: DateTime?, context: Context) {
        val days = Math.abs(Days.daysBetween(DateTime.now(), dueinfoActivedate).days)
        if (days in 1..29) {
            binding.dueInfo.text = context.resources.getQuantityString(R.plurals.pending_day, days, days)
        }
        if (days > 30) {
            val monthValue = (days / 30)
            binding.dueInfo.text = context.resources.getQuantityString(R.plurals.pending_month, monthValue, monthValue)
        }
    }

    private fun setSubtitleAndDateString(
        dateTime: DateTime?,
        lastActivityMetaInfo: Int,
        context: Context
    ) {
        val lastActivityStringId = CustomerLastActivity.getActivityFromCodeWithCustomerSubtitleAb(lastActivityMetaInfo)
        val blankString = ""
        val amount = if (customer.lastAmount != null) CurrencyUtil.formatV2(customer.lastAmount!!) else ""
        binding.transactionAmount.gone()
        var subTititle = ""
        when {
            dateTime == null -> {
                subTititle = context.resources.getQuantityString(
                    lastActivityStringId,
                    DATE_TYPE_TODAY,
                    amount,
                    blankString
                )
            }
            LocalDate.now().compareTo(LocalDate(dateTime)) == 0 -> {
                subTititle = context.resources.getQuantityString(
                    lastActivityStringId,
                    DATE_TYPE_TODAY,
                    amount,
                    context.getString(R.string.today)
                )
            }
            LocalDate.now().minusDays(1).compareTo(LocalDate(dateTime)) == 0 -> {
                subTititle = context.resources.getQuantityString(
                    lastActivityStringId,
                    DATE_TYPE_TODAY,
                    amount,
                    context.getString(R.string.yesterday)
                )
            }
            else -> {
                subTititle = context.resources.getQuantityString(
                    lastActivityStringId,
                    DATE_TYPE_DATE,
                    amount,
                    customer.lastActivity!!.toString(DateTimeFormat.forPattern("dd MMM, YYYY"))
                )
            }
        }
        binding.transactionInfo.text = HtmlCompat.fromHtml(subTititle, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun setBalance() {
        CurrencyUtil.renderV2(customer.balanceV2, binding.tvBalance, 0)
    }

    private fun setBalanceStatus() {
        if (customer.balanceV2 <= 0) {
            binding.tvBalanceStatus.text = context.getString(R.string.due)
        } else {
            binding.tvBalanceStatus.text = context.getString(R.string.advance)
        }
    }

    @CallbackProp
    fun setListener(listener: DefaulterClickListener?) {
        binding.llCustomerViewRoot.setOnClickListener {
            listener?.onDefaulterClick(customer.id)
        }
    }

    companion object {
        const val DATE_TYPE_TODAY = 3
        const val DATE_TYPE_DATE = 1
    }
}
