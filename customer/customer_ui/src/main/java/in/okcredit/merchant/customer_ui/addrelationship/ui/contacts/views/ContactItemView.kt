package `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.views

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.customer.contract.RelationshipType
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipEpoxyModels.ContactModel
import `in`.okcredit.merchant.customer_ui.databinding.AddCustomerFragmentContactViewV2Binding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.Glide
import tech.okcredit.android.base.extensions.getString
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.TextDrawableUtils

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ContactItemView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private var relationshipId: String? = null
    private var mobile: String? = null
    private var listener: ContactListener? = null

    interface ContactListener {
        fun onContactItemClicked(
            relationshipId: String?,
            mobile: String
        )
    }

    private val binding = AddCustomerFragmentContactViewV2Binding
        .inflate(LayoutInflater.from(context), this, true)

    init {
        binding.rootView.setOnClickListener {
            listener?.onContactItemClicked(
                relationshipId,
                mobile!!
            )
        }
    }

    @ModelProp
    fun setContact(contact: ContactModel) {
        this.relationshipId = contact.relationshipId
        this.mobile = contact.mobile
        binding.apply {
            name.text = contact.name
            mobile.text = contact.mobile
            checkForProfileImage(contact)
            checkForOkcreditIcon(contact)
        }
        checkForBalance(contact)
        checkForRelationshipType(contact)
    }

    private fun checkForRelationshipType(contact: ContactModel) {
        if (contact.relationshipId == null && contact.relationshipType == null && contact.balance == null) {
            binding.relationshipType.gone()
            return
        }

        binding.relationshipType.visible()
        if (contact.relationshipType == RelationshipType.ADD_CUSTOMER) {
            binding.relationshipType.text = context.getString(R.string.customer)
        } else {
            binding.relationshipType.text = context.getString(R.string.suppliers)
        }
    }

    private fun checkForBalance(contact: ContactModel) {
        if (contact.relationshipId == null && contact.relationshipType == null && contact.balance == null) {
            binding.tvBalance.gone()
            binding.tvBalanceStatus.gone()
            return
        }

        binding.tvBalance.visible()
        binding.tvBalanceStatus.visible()
        CurrencyUtil.renderV2(contact.balance!!, binding.tvBalance, contact.balance >= 0)
        binding.tvBalanceStatus.text = if (contact.balance > 0) {
            getString(R.string.advance)
        } else {
            getString(R.string.due)
        }
    }

    private fun checkForOkcreditIcon(contact: ContactModel) = binding.apply {
        existingCustomer.isVisible = contact.showOkcreditIcon
    }

    private fun checkForProfileImage(contact: ContactModel) = binding.apply {
        val defaultPic = TextDrawableUtils
            .getRoundTextDrawable(contact.name.firstOrNull()?.toUpperCase().toString())

        if (contact.profileImage.isNotNullOrBlank()) {
            Glide.with(context)
                .load(contact.profileImage)
                .circleCrop()
                .error(defaultPic)
                .placeholder(defaultPic)
                .fallback(defaultPic)
                .into(pic)
        } else {
            pic.setImageDrawable(defaultPic)
        }
    }

    @CallbackProp
    fun setListener(listener: ContactListener?) {
        this.listener = listener
    }
}
