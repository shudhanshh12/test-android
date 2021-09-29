package `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.views

import `in`.okcredit.merchant.customer_ui.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.add_customer_fragment_header_view.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class AddRelationshipHeaderView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(ctx).inflate(R.layout.add_customer_fragment_header_view, this, true)
        title.setText(R.string.phonebook_contacts)
    }
}
