package tech.okcredit.home.ui.homesearch.views

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.backend.utils.QRCodeUtils
import `in`.okcredit.fileupload._id.GlideApp
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.buildSpannedString
import androidx.core.text.color
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
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.extensions.getString
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.home.R
import tech.okcredit.home.databinding.HomeSearchCustomerViewBinding
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileDialogContract
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileDialogContract.Source.CUSTOMER_SEARCH_PROFILE
import tech.okcredit.home.ui.homesearch.HomeSearchItem
import tech.okcredit.home.utils.TextDrawableUtils
import kotlin.math.roundToInt

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class HomeSearchCustomerView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : ConstraintLayout(context, attributeSet, defStyle) {

    private val binding = HomeSearchCustomerViewBinding.inflate(LayoutInflater.from(context), this)

    private var viewJob = SupervisorJob()
    private val viewScope = CoroutineScope(Dispatchers.IO + viewJob)

    private var customerSelectionListener: SearchCustomerListener? = null

    private var customerId: String = ""
    private var showReminderOption: Boolean = false

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
            customerSelectionListener?.onCustomerSelected(customerId)
        }

        binding.photoImageView.setOnClickListener {
            customerSelectionListener?.showCustomerQr(customerId, CUSTOMER_SEARCH_PROFILE)
        }

        binding.imageQuickAction.setOnClickListener {
            if (showReminderOption) {
                customerSelectionListener?.sendReminder(customerId)
            } else {
                customerSelectionListener?.showCustomerQr(
                    customerId,
                    CustomerProfileDialogContract.Source.CUSTOMER_SEARCH_QR_ACTION
                )
            }
        }

        binding.qrCard.setOnClickListener {
            customerSelectionListener?.showCustomerQr(
                customerId,
                CustomerProfileDialogContract.Source.CUSTOMER_SEARCH_QR_CARD
            )
        }
    }

    @CallbackProp
    fun setListener(listener: SearchCustomerListener?) {
        this.customerSelectionListener = listener
    }

    @ModelProp
    fun setHomeCustomerItem(customerItem: HomeSearchItem.CustomerItem) {
        customerId = customerItem.customerId
        showReminderOption = customerItem.showReminderOption
        binding.nameTextView.text = customerItem.name
        setCustomerProfilePic(customerItem.name, customerItem.profileImage)
        setCustomerBalance(customerItem.balance)
        setCommonLedgerVisibility(customerItem.commonLedger, customerItem.addTxnPermissionDenied)
        setExpandedQrCode(customerItem.showFullQRCard, customerItem.qrIntent, customerItem.name)
        setQuickAccessOptions(customerItem)
    }

    private fun setQuickAccessOptions(customerItem: HomeSearchItem.CustomerItem) {
        when {
            customerItem.showFullQRCard -> {
                binding.imageQuickAction.isVisible = false
            }
            customerItem.showQROption -> {
                binding.imageQuickAction.setImageResource(R.drawable.ic_qr_code)
                binding.imageQuickAction.isVisible = true
            }
            customerItem.showReminderOption -> {
                binding.imageQuickAction.setImageResource(R.drawable.ic_whatsapp)
                binding.imageQuickAction.isVisible = true
            }
            else -> {
                binding.imageQuickAction.isVisible = false
            }
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

    private fun setExpandedQrCode(showQRCard: Boolean, qrIntent: String?, name: String) {
        if (qrIntent.isNullOrEmpty() || !showQRCard) {
            binding.qrCard.gone()
            return
        }

        binding.imageQuickAction.gone()
        binding.qrCard.visible()
        binding.textQrDescription.text = context.getString(R.string.payment_made_to_custom_qr, name)
        viewScope.launch {
            val qrBitmap = QRCodeUtils.getBitmap(qrIntent, context, context.dpToPixel(77f).toInt())
            withContext(Dispatchers.Main) {
                if (qrBitmap != null) {
                    binding.imageQr.setImageBitmap(qrBitmap)
                }
            }
        }
    }

    private fun setCustomerBalance(balance: Long) {
        binding.tvSubtitle.text = when {
            balance > 0 -> {
                buildSpannedString {
                    append(getString(R.string.balance))
                    append(" ")
                    color(context.getColorCompat(R.color.tx_payment)) {
                        append(String.format("₹%s", CurrencyUtil.formatV2(balance)))
                    }
                    append(" ")
                    append(getString(R.string.advance))
                }
            }
            balance < 0 -> {
                buildSpannedString {
                    append(getString(R.string.balance))
                    append(" ")
                    color(context.getColorCompat(R.color.tx_credit)) {
                        append(String.format("₹%s", CurrencyUtil.formatV2(balance)))
                    }
                    append(" ")
                    append(getString(R.string.due))
                }
            }
            else -> {
                context.getString(R.string.balance_zero, CurrencyUtil.formatV2(balance))
            }
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

    interface SearchCustomerListener {
        fun onCustomerSelected(customerId: String)
        fun showCustomerQr(customerId: String, source: String)
        fun sendReminder(customerId: String)
    }
}
