package tech.okcredit.help.help_details.views

import `in`.okcredit.fileupload._id.GlideApp
import android.content.Context
import android.os.Build
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.help_details_item.view.*
import tech.okcredit.help.R
import tech.okcredit.userSupport.model.HelpInstruction

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class HelpDetailsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    init {
        LayoutInflater.from(context).inflate(R.layout.help_details_item, this, true)
    }

    @ModelProp
    fun setDetailText(detailsText: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tv_detail_text.text = Html.fromHtml(detailsText, Html.FROM_HTML_MODE_COMPACT)
        } else {
            tv_detail_text.text = Html.fromHtml(detailsText)
        }
    }

    @ModelProp
    fun setIndexText(index: Int) {
        tv_index.text = index.toString()
    }

    @ModelProp
    fun setItemImage(helpInstruction: HelpInstruction) {
        iv_instruction_Image.visibility = View.VISIBLE
        if (helpInstruction.type == "image") {
            val placeHolder = ContextCompat.getDrawable(context, R.drawable.placeholder_image)
            GlideApp.with(context)
                .load(helpInstruction.image_url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(placeHolder)
                .into(iv_instruction_Image)
                .waitForLayout()
            iv_instruction_Image.clipToOutline = true
        } else if (helpInstruction.type == "gif") {
            val placeHolder = ContextCompat.getDrawable(context, R.drawable.placeholder_image)
            GlideApp.with(context)
                .asGif()
                .load(helpInstruction.image_url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(placeHolder)
                .into(iv_instruction_Image)
                .waitForLayout()
            iv_instruction_Image.clipToOutline = true
        } else {
            iv_instruction_Image.visibility = View.GONE
        }
    }
}
