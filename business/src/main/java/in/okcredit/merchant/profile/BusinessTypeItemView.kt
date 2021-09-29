package `in`.okcredit.merchant.profile

import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.contract.BusinessType
import `in`.okcredit.merchant.merchant.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.business_type_item_view.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class BusinessTypeItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var listener: SelectBusinessTypesListener? = null
    private var businessType: BusinessType? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.business_type_item_view, this, true)
    }

    interface SelectBusinessTypesListener {
        fun onSelectBusinessType(businessType: BusinessType)
    }

    @ModelProp
    fun setBusinessType(businessType: BusinessType) {
        this.businessType = businessType
        title.text = businessType.name
    }

    @ModelProp
    fun setBusinessTypeImage(businessTypeImage: String?) {
        if (businessTypeImage.isNullOrEmpty().not()) {
            GlideApp
                .with(context)
                .load(businessTypeImage)
                .into(image_business)
        }
    }

    @ModelProp
    fun setChecked(checked: Boolean) {
        if (checked) {
            check.visibility = View.VISIBLE
        } else {
            check.visibility = View.GONE
        }
    }

    @CallbackProp
    fun setListener(listener: SelectBusinessTypesListener?) {
        this.listener = listener

        root_view.setOnClickListener {
            businessType?.let {
                listener?.onSelectBusinessType(it)
            }
        }
    }
}
