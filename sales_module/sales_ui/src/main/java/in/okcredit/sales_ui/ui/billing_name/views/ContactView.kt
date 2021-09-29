package `in`.okcredit.sales_ui.ui.billing_name.views

import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.databinding.ItemBillingContactBinding
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.Glide
import tech.okcredit.android.base.utils.BitmapUtils
import tech.okcredit.contacts.contract.model.Contact

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ContactView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var listener: Listener? = null

    private var binding: ItemBillingContactBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = ItemBillingContactBinding.inflate(inflater, this, true)
    }

    @ModelProp
    fun setContact(contact: Contact) {
        binding.root.setOnClickListener {
            listener?.onClick(contact)
        }
        binding.name.text = contact.name
        binding.number.text = contact.mobile
        val defaultPic = ContextCompat.getDrawable(context, R.drawable.ic_contacts_placeholder)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            defaultPic?.setTint(Color.parseColor("#22000000"))
        }
        val picUri = if (contact.picUri.isNullOrEmpty().not())
            Uri.parse(contact.picUri)
        else
            BitmapUtils.getUriFromDrawable(context, R.drawable.ic_contacts_placeholder)

        Glide.with(context)
            .load(picUri)
            .circleCrop()
            .error(defaultPic)
            .placeholder(defaultPic)
            .fallback(defaultPic)
            .into(binding.profileImage)
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    interface Listener {
        fun onClick(contact: Contact)
    }
}
