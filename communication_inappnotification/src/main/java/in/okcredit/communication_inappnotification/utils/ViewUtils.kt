package `in`.okcredit.communication_inappnotification.utils

import `in`.okcredit.communication_inappnotification.R
import `in`.okcredit.communication_inappnotification.model.EducationSheet
import `in`.okcredit.communication_inappnotification.utils.InAppNotificationUtils.dpToPixel
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.fileupload._id.GlideRequest
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.button.MaterialButton
import tech.okcredit.android.base.extensions.isNotNullOrBlank

object ViewUtils {

    fun setTextAndShowOrHideTextView(text: String?, view: TextView, constraintSet: ConstraintSet) {
        if (text.isNotNullOrBlank()) {
            view.show(constraintSet)
            view.text = text
        } else {
            view.hide(constraintSet)
        }
    }

    fun getGlideRequestForUrl(context: Context, url: String?): GlideRequest<Drawable> {
        return GlideApp.with(context)
            .load(url)
            .placeholder(ColorDrawable(ContextCompat.getColor(context, R.color.grey300)))
            .dontAnimate()
            .thumbnail(EducationSheet.GLIDE_THUMBNAIL_SIZE_MULTIPLIER)
    }

    fun getButtonStartDrawableTarget(context: Context, view: MaterialButton): CustomTarget<Drawable> {
        return object : CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                resource.bounds = getButtonDrawableSizeRect(context)
                view.setCompoundDrawables(resource, null, null, null)
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
        }
    }

    internal fun getButtonDrawableSizeRect(context: Context): Rect {
        return Rect(
            0,
            0,
            EducationSheet.BUTTON_ICON_SIZE.dpToPixel(context),
            EducationSheet.BUTTON_ICON_SIZE.dpToPixel(context)
        )
    }

    fun View.show(constraintSet: ConstraintSet) = constraintSet.setVisibility(this.id, ConstraintSet.VISIBLE)

    fun View.hide(constraintSet: ConstraintSet) = constraintSet.setVisibility(this.id, ConstraintSet.GONE)
}
