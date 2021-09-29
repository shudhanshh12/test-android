package tech.okcredit.home.ui.homesearch.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.homesearch_import_contact_view.view.*
import tech.okcredit.home.R
import tech.okcredit.home.ui.homesearch.HomeSearchContract

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ImportCustomerContactView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.homesearch_import_contact_view, this, true)
    }

    @ModelProp
    fun setSource(source: HomeSearchContract.SOURCE) {
        if (source == HomeSearchContract.SOURCE.HOME_CUSTOMER_TAB) {
            text_desc.text = context.resources.getString(R.string.import_phone_contact_desc)
        } else if (source == HomeSearchContract.SOURCE.HOME_SUPPLIER_TAB) {
            text_desc.text = context.resources.getString(R.string.import_phone_contact_desc_suppliers)
        }
    }

    interface ImportContactListener {
        fun onImportContact()
    }

    @CallbackProp
    fun setListener(listener: ImportContactListener?) {

        llImportContact.setOnClickListener {
            listener?.onImportContact()
        }
    }
}
