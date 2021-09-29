package `in`.okcredit.frontend.ui.merchant_profile.categoryscreen.views

import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.frontend.R
import `in`.okcredit.merchant.contract.Category
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.amulyakhare.textdrawable.TextDrawable
import com.jakewharton.rxbinding3.view.clicks
import kotlinx.android.synthetic.main.category_fragment_view.view.*
import tech.okcredit.android.base.extensions.getColorFromAttr

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class CategoryFragmentView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private var category: Category? = null

    interface Listener {
        fun clickedCategoryScreenView(category: Category)
    }

    init {
        LayoutInflater.from(ctx).inflate(R.layout.category_fragment_view, this, true)
    }

    @ModelProp
    fun setCurrentCategoryStatus(isCurrentCategory: Boolean) {
        if (isCurrentCategory) {
            checked_icon.visibility = View.VISIBLE
            category_title.setTextColor(context.getColorFromAttr(R.attr.colorPrimary))
        } else {
            checked_icon.visibility = View.GONE
            category_title.setTextColor(ContextCompat.getColor(context, R.color.grey900))
        }
    }

    @ModelProp
    fun setCategory(category: Category) {
        this.category = category
        category_title.text = category.name

        val defaultPic = TextDrawable
            .builder()
            .buildRound(
                category.name?.substring(0, 1)?.toUpperCase(),
                ContextCompat.getColor(context, R.color.green_lite)
            )

        try {
            GlideApp
                .with(context)
                .load(category.imageUrl)
                .circleCrop()
                .placeholder(defaultPic)
                .fallback(defaultPic)
                .into(category_image)
        } catch (e: Exception) {
            category_image.setImageDrawable(defaultPic)
        }
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        root_view.clicks()
            .doOnNext {
                category?.let {
                    listener?.clickedCategoryScreenView(it)
                }
            }
            .subscribe()
    }
}
