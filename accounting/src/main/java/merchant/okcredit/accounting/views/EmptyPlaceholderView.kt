package merchant.okcredit.accounting.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView
import merchant.okcredit.accounting.databinding.AccountStatementScreenEmptyViewBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class EmptyPlaceholderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = AccountStatementScreenEmptyViewBinding.inflate(LayoutInflater.from(context), this, true)
}
