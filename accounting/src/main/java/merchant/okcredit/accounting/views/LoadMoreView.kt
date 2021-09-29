package merchant.okcredit.accounting.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView
import com.jakewharton.rxbinding3.view.clicks
import merchant.okcredit.accounting.databinding.TransactionLoadMoreViewBinding
import java.util.concurrent.TimeUnit

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class LoadMoreView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = TransactionLoadMoreViewBinding.inflate(LayoutInflater.from(context), this, true)

    interface Listener {
        fun onLoadMoreClicked()
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        binding.btnLoadMore.clicks()
            .throttleFirst(300, TimeUnit.MILLISECONDS)
            .doOnNext { listener?.onLoadMoreClicked() }
            .subscribe()
    }
}
