package tech.okcredit.help.help_details.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.like_help_item_view.view.*
import tech.okcredit.android.base.extensions.getColorFromAttr
import tech.okcredit.help.R
import tech.okcredit.userSupport.data.LikeState

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class LikeDislikeView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    init {
        LayoutInflater.from(context).inflate(R.layout.like_help_item_view, this, true)
    }

    lateinit var helpId: String

    @ModelProp
    fun setHelpItemId(helpId: String) {
        this.helpId = helpId
    }

    interface ILikeDislikeInterface {
        fun onLikeClick(helpId: String)
        fun onDislikeClick(helpId: String)
    }

    @ModelProp
    fun checkLikeOrDisLike(isLike: LikeState) {
        when (isLike) {
            LikeState.LIKE -> {
                // like icon green & dislike icon should be gray and rotated
                iv_like.setColorFilter(context.getColorFromAttr(R.attr.colorPrimary))
                iv_dislike.setColorFilter(ContextCompat.getColor(context, R.color.grey500))
            }
            LikeState.DISLIKE -> {
                // like icon gray & dislike icon should be orange and rotated
                iv_like.setColorFilter(ContextCompat.getColor(context, R.color.grey500))
                iv_dislike.setColorFilter(ContextCompat.getColor(context, R.color.orange_primary))
            }
            LikeState.NORMAL -> {
                // like icon normal & dislike icon normal and should be rotated
                iv_like.setColorFilter(ContextCompat.getColor(context, R.color.grey500))
                iv_dislike.setColorFilter(ContextCompat.getColor(context, R.color.grey500))
            }
        }
    }

    @CallbackProp
    fun onLikeClick(listener: ILikeDislikeInterface?) {
        iv_dislike.setOnClickListener {
            listener?.onDislikeClick(helpId)
        }
        iv_like.setOnClickListener {
            listener?.onLikeClick(helpId)
        }
    }
}
