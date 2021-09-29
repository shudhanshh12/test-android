package tech.okcredit.home.ui.home.views

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.model.CustomerLastActivity
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.shared.performance.PerformanceTracker
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.home_customer_view.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.home.R
import tech.okcredit.home.utils.TextDrawableUtils
import java.util.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class HomeCustomerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var isProfilePicClickable: Boolean = true
    private var viewJob = SupervisorJob()
    private val viewScope = CoroutineScope(Dispatchers.IO + viewJob)

    interface CustomerSelectionListener {
        fun onCustomerSelected(customer: Customer)
        fun onCustomerProfileSelected(customer: Customer)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewJob.cancelChildren()
    }

    companion object {
        internal fun getDateFormat(context: Context): DateTimeFormatter {
            return DateTimeFormat.forPattern("dd MMM, YYYY")
                .withLocale(Locale(LocaleManager.getLanguageForDateFormat(context)))
        }

        const val DATE_TYPE_TODAY = 3
        const val DATE_TYPE_DATE = 1
    }

    private var isUnSyncTransaction: Boolean = false
    private lateinit var customer: Customer

    private var tracker: Tracker? = null

    private lateinit var customerAndEducationObject: CustomerAndEducationObject

    private var commonLedger: Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.home_customer_view, this, true)
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setTracker(tracker: Tracker) {
        this.tracker = tracker
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setPerformanceTracker(performanceTracker: PerformanceTracker) {
        llCustomerViewRoot.setTracker { performanceTracker }
    }

    @ModelProp
    fun setCustomerViewData(customerAndEducationObject: CustomerAndEducationObject) {
        this.customer = customerAndEducationObject.customer
        this.isProfilePicClickable = customerAndEducationObject.isProfilePicClickable
        this.isUnSyncTransaction = customerAndEducationObject.isUnsyncTransaction
        this.customerAndEducationObject = customerAndEducationObject
    }

    @AfterPropsSet
    fun setDataOncePropSet() {
        setName()
        setProfilePhoto()
        setSubtitle(customerAndEducationObject)
        setBalance()
        setBalanceStatus()
        setNewActivityCount()
        setDueWarning()
        setCommonLedgerVisibility(
            customerAndEducationObject.isSupplierRegistered,
            customer,
            customerAndEducationObject.isSingleListEnabled,
            customerAndEducationObject.singleListAddTxnRestrictedCustomers
        )
    }

    private fun setDueWarning() {
        if (customer.dueWarningDrawable != 0) {
            photo_image_view.background = ContextCompat.getDrawable(context, customer.dueWarningDrawable)
        } else photo_image_view.background = ContextCompat.getDrawable(context, android.R.color.transparent)
    }

    private fun setCommonLedgerVisibility(
        isSupplierRegistered: Boolean,
        customer: Customer,
        singleListEnabled: Boolean,
        isSingleListAddTxnRestrictedCustomer: Boolean,
    ) {
        commonLedger = isSupplierRegistered
        if (singleListEnabled) {
            if (isSupplierRegistered) {
                registered.visibility = View.VISIBLE
                if (isSingleListAddTxnRestrictedCustomer || customer.isAddTransactionPermissionDenied()) {
                    registered.setImageResource(R.drawable.ic_add_transaction_disabled_small)
                } else {
                    registered.setImageResource(R.drawable.ic_common_ledger_small)
                }
            } else {
                registered.visibility = View.GONE
            }
        } else {
            if (isSupplierRegistered) {
                registered.visibility = View.VISIBLE
                if (this.customer.isAddTransactionPermissionDenied()) {
                    registered.setImageResource(R.drawable.ic_add_transaction_disabled_small)
                } else {
                    registered.setImageResource(R.drawable.ic_common_ledger_small)
                }
            } else {
                registered.visibility = View.GONE
            }
        }
    }

    private fun setName() {
        name_text_view.text = customer.description
    }

    private fun setProfilePhoto() {

        val name = customer.description
        val profilePhoto = customer.profileImage

        viewScope.launch {
            val defaultPic = TextDrawableUtils.getRoundTextDrawable(name)
            withContext(Dispatchers.Main) {
                GlideApp.with(context)
                    .load(profilePhoto)
                    .circleCrop()
                    .placeholder(defaultPic)
                    .error(defaultPic)
                    .fallback(defaultPic)
                    .thumbnail(0.25f)
                    .into(photo_image_view)
            }
        }
    }

    private fun setSubtitle(customerAndEducationObject: CustomerAndEducationObject) {
        val lastActivity = customer.lastActivity
        val createdAt = customer.createdAt

        if (customer.dueActive && customer.dueInfo_activeDate != null && customer.balanceV2 < 0) {
            customer_added_container.visibility = View.GONE
            transaction_info.visibility = View.GONE
            transaction_amount.visibility = View.GONE
            transaction_amount_subtitle_date_ab.gone()
            setDueDate()
        } else if (customer.lastActivity != createdAt) {
            customer_added_container.visibility = View.GONE
            transaction_info.visibility = View.VISIBLE
            due_info.visibility = View.GONE
            due_date_info.visibility = View.GONE
            transaction_amount.visibility = View.VISIBLE

            transaction_amount_subtitle_date_ab.visible()
            customer.lastActivityMetaInfo?.let { setSubtitleAndDateString(lastActivity, it, context) }

            ivSubtitleInfo.visibility = View.VISIBLE
            if (isUnSyncTransaction) {
                ivSubtitleInfo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sync_pending))
            } else {
                ivSubtitleInfo.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_single_tick
                    )
                )
            }
            ivSubtitleInfo.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.grey700))
        } else if (customer.lastActivity == createdAt) {
            transaction_info.visibility = View.GONE
            transaction_amount.visibility = View.GONE
            due_info.visibility = View.GONE
            due_date_info.visibility = View.GONE
            customer_added_container.visibility = View.VISIBLE
            transaction_amount_subtitle_date_ab.gone()
            val date = createdAt?.toString(getDateFormat(context))
            customer_added.text = HtmlCompat.fromHtml(
                context.getString(R.string.added_on_new, date),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            ivSubtitleInfo.visibility = View.VISIBLE
            ivSubtitleInfo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_person_placeholder))
            ivSubtitleInfo.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.grey700))
        }
    }

    private fun setDueDate() {
        ivSubtitleInfo.visibility = View.VISIBLE
        when {
            DateTimeUtils.isCurrentDate(customer.dueInfo_activeDate) -> {
                due_info.text = context.getString(R.string.due_today)
                due_info.setTextColor(ContextCompat.getColor(context, R.color.green_primary))
                ivSubtitleInfo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_calendar))
                ivSubtitleInfo.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.green_primary))
                due_date_info.visibility = GONE
                due_info.visibility = View.VISIBLE
            }
            DateTimeUtils.isDatePassed(customer.dueInfo_activeDate) -> {
                ivSubtitleInfo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_calendar))
                ivSubtitleInfo.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.red_primary))
                due_date_info.setTextColor(ContextCompat.getColor(context, R.color.red_primary))
                due_info.visibility = GONE
                due_date_info.visibility = View.VISIBLE
                getCollectionDateStrings(customer.dueInfo_activeDate, context)
            }
            DateTimeUtils.isFutureDate(customer.dueInfo_activeDate) -> {
                due_info.setTextColor(ContextCompat.getColor(context, R.color.grey700))
                val date = DateTimeUtils.getFormat2(context, customer.dueInfo_activeDate)
                due_info.text = HtmlCompat.fromHtml(
                    context.getString(R.string.due_on_new, date),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                ivSubtitleInfo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_calendar))
                ivSubtitleInfo.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.grey700))
                due_info.visibility = VISIBLE
                due_date_info.visibility = GONE
            }
        }
        if (isUnSyncTransaction) {
            ivSubtitleInfo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sync_pending))
        }
    }

    fun getCollectionDateStrings(dueinfoActivedate: DateTime?, context: Context) {
        val days = Math.abs(Days.daysBetween(DateTime.now(), dueinfoActivedate).days)
        if (days in 1..30) {
            due_date_info.text = context.resources.getQuantityString(R.plurals.pending_day, days, days)
        } else if (days > 30) {
            val monthValue = (days / 30)
            due_date_info.text = context.resources.getQuantityString(R.plurals.pending_month, monthValue, monthValue)
        }
    }

    private fun setBalance() {
        CurrencyUtil.renderV2(customer.balanceV2, tvBalance, 0)
    }

    private fun setBalanceStatus() {
        if (customer.balanceV2 <= 0) {
            tvBalanceStatus.text = context.getString(R.string.home_customer_due)
        } else {
            tvBalanceStatus.text = context.getString(R.string.home_customer_advance)
        }
    }

    private fun setNewActivityCount() {
        val count = customer.newActivityCount
        if (count == 0L) {
            cvNewCount.gone()
        } else {
            cvNewCount.visible()
            tvNewCount.text = context.getString(R.string.new_count, count.toString())
        }
    }

    @CallbackProp
    fun setListener(listener: CustomerSelectionListener?) {

        llCustomerViewRoot.setOnClickListener {
            if (commonLedger) {
                tracker?.trackViewCommonLedger(
                    PropertyValue.CUSTOMER,
                    customer.id,
                    customer.isAddTransactionPermissionDenied()
                )
            }
            listener?.onCustomerSelected(customer)
        }

        photo_image_view.setOnClickListener {
            if (isProfilePicClickable) {
                if (commonLedger) {
                    tracker?.trackViewCommonLedger(
                        PropertyValue.CUSTOMER,
                        customer.id,
                        customer.isAddTransactionPermissionDenied()
                    )
                }
                listener?.onCustomerProfileSelected(customer)
            }
        }
    }

    private fun setSubtitleAndDateString(
        dateTime: DateTime?,
        lastActivityMetaInfo: Int,
        context: Context,
    ) {
        val lastActivityStringId = CustomerLastActivity.getActivityFromCodeWithCustomerSubtitleAb(lastActivityMetaInfo)
        val blankString = ""
        val amount = if (customer.lastAmount != null) CurrencyUtil.formatV2(customer.lastAmount!!) else ""
        transaction_amount.gone()
        var subTitle = ""
        when {
            dateTime == null -> {
                subTitle = context.resources.getQuantityString(
                    lastActivityStringId,
                    DATE_TYPE_TODAY,
                    amount,
                    blankString
                )
            }
            LocalDate.now().compareTo(LocalDate(dateTime)) == 0 -> {
                subTitle = context.resources.getQuantityString(
                    lastActivityStringId,
                    DATE_TYPE_TODAY,
                    amount,
                    context.getString(R.string.today)
                )
            }
            LocalDate.now().minusDays(1).compareTo(LocalDate(dateTime)) == 0 -> {
                subTitle = context.resources.getQuantityString(
                    lastActivityStringId,
                    DATE_TYPE_TODAY,
                    amount,
                    context.getString(R.string.yesterday)
                )
            }
            else -> {
                subTitle = context.resources.getQuantityString(
                    lastActivityStringId,
                    DATE_TYPE_DATE,
                    amount,
                    customer.lastActivity!!.toString(DateTimeFormat.forPattern("dd MMM, YYYY"))
                )
            }
        }
        transaction_info.text = HtmlCompat.fromHtml(subTitle, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    data class CustomerAndEducationObject(
        val customer: Customer,
        val index: Int? = null,
        val totalCustomers: Int? = null,
        val showEducation: Boolean,
        val isUnsyncTransaction: Boolean,
        val isSupplierRegistered: Boolean,
        val isProfilePicClickable: Boolean,
        val isSingleListEnabled: Boolean,
        val singleListAddTxnRestrictedCustomers: Boolean,
    )
}
