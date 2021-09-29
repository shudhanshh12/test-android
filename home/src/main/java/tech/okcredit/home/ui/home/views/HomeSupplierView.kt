package tech.okcredit.home.ui.home.views

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.shared.performance.PerformanceTracker
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.item_supplier_tab.view.*
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.home.R
import tech.okcredit.home.utils.TextDrawableUtils
import java.util.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class HomeSupplierView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    interface SupplierSelectionListener {
        fun onSupplierSelected(supplierId: String, registered: Boolean)
        fun onSupplierProfileSelected(supplier: Supplier)
    }

    companion object {
        const val SYNC_COMPLETED = 1
        const val SYNC_PENDING = 2
        const val SYNC_NO_TXN = 3
    }

    private lateinit var supplier: Supplier

    private var tracker: Tracker? = null

    private var commonLedger: Boolean = false

    private fun getDateFormat(context: Context?): DateTimeFormatter {
        return DateTimeFormat.forPattern("dd MMM, YYYY")
            .withLocale(Locale(LocaleManager.getLanguageForDateFormat(context!!)))
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.item_supplier_tab, this, true)
    }

    private fun setSupplier(supplier: Supplier) {
        this.supplier = supplier

        setName()
        setProfilePhoto()
        setLastPayment()
        setBalance()
        setBalanceStatus()
        setSupplierRegisteredStatus()
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setTracker(tracker: Tracker) {
        this.tracker = tracker
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setPerformanceTracker(performanceTracker: PerformanceTracker) {
        llSupplierCustomerViewRoot.setTracker { performanceTracker }
    }

    @ModelProp
    fun supplierDetails(homeSupplierData: HomeSupplierData) {
        setSupplier(homeSupplierData.supplier)
        setSyncIcon(homeSupplierData)
        setNewActivityCount()
    }

    private fun setSyncIcon(homeSupplierData: HomeSupplierData) {
        val synIcon = when (homeSupplierData.syncType) {
            SYNC_COMPLETED -> R.drawable.ic_single_tick
            SYNC_PENDING -> R.drawable.ic_sync_pending
            else -> R.drawable.ic_user
        }

        ivSync.setImageDrawable(ContextCompat.getDrawable(context, synIcon))
    }

    private fun setName() {
        name_text_view.text = supplier.name
    }

    private fun setSupplierRegisteredStatus() {
        if (supplier.registered) {
            commonLedger = true
            if (supplier.addTransactionRestricted) {
                registered.setImageResource(R.drawable.ic_add_transaction_disabled_small)
            } else {
                registered.setImageResource(R.drawable.ic_common_ledger_small)
            }
            registered.visibility = View.VISIBLE
        } else {
            registered.visibility = View.GONE
        }
    }

    private fun setProfilePhoto() {

        val name = supplier.name
        val profilePhoto = supplier.profileImage

        val defaultPic = TextDrawableUtils.getRoundTextDrawable(name)
        GlideApp.with(ivProfilePhoto)
            .load(profilePhoto)
            .placeholder(defaultPic)
            .circleCrop()
            .error(defaultPic)
            .fallback(defaultPic)
            .thumbnail(0.25f)
            .into(ivProfilePhoto)
    }

    private fun setLastPayment() {
        val lastActivity = supplier.lastActivityTime
        val createdAt = supplier.createTime

        if (lastActivity != null) {
            val duration = Duration(lastActivity, DateTime().withTimeAtStartOfDay().plusDays(1))
            when (duration.standardDays) {
                0L -> tvLastPayment.text = context.getString(R.string.paid_today)
                1L -> tvLastPayment.text = context.getString(R.string.paid_yesterday)
                else -> tvLastPayment.text = lastActivity.toString(
                    getDateFormat(
                        context
                    )
                )
            }
        } else {
            tvLastPayment.text = context.getString(
                R.string.added_on_date,
                createdAt.toString(getDateFormat(context))
            )
        }
    }

    private fun setBalance() {
        CurrencyUtil.renderV2(supplier.balance, tvBalance, 0)
    }

    private fun setBalanceStatus() {
        if (supplier.balance <= 0) {
            tvBalanceStatus.text = context.getString(R.string.due)
        } else {
            tvBalanceStatus.text = context.getString(R.string.advance)
        }
    }

    private fun setNewActivityCount() {
        val count = supplier.newActivityCount
        if (count == 0L) {
            cvNewCount.visibility = View.GONE
        } else {
            cvNewCount.visibility = View.VISIBLE
            tvNewCount.text = context.getString(R.string.new_count, count.toString())
        }
    }

    @CallbackProp
    fun setListener(listener: SupplierSelectionListener?) {

        llSupplierCustomerViewRoot.setOnClickListener {
            if (commonLedger) {
                tracker?.trackViewCommonLedger(
                    PropertyValue.SUPPLIER,
                    supplier.id,
                    supplier.addTransactionRestricted
                )
            }
            listener?.onSupplierSelected(supplier.id, supplier.registered)
        }

        ivProfilePhoto.setOnClickListener {
            if (commonLedger) {
                tracker?.trackViewCommonLedger(
                    PropertyValue.SUPPLIER,
                    supplier.id,
                    supplier.addTransactionRestricted
                )
            }
            listener?.onSupplierProfileSelected(supplier)
        }
    }

    data class HomeSupplierData(
        val id: String,
        val supplier: Supplier,
        val syncType: Int,
    )
}
