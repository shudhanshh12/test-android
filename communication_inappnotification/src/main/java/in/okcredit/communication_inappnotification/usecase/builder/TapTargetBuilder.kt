package `in`.okcredit.communication_inappnotification.usecase.builder

import `in`.okcredit.communication_inappnotification.R
import `in`.okcredit.communication_inappnotification.contract.ui.remote.TapTarget.Companion.DEFAULT_PADDING
import `in`.okcredit.communication_inappnotification.contract.ui.remote.TapTarget.Companion.DEFAULT_RADIUS
import android.graphics.Typeface
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import tech.okcredit.android.base.extensions.dpToPixel
import tech.okcredit.android.base.utils.OuterCirclePromptBackground
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal
import java.lang.ref.WeakReference

class TapTargetBuilder constructor(
    private val weakScreen: WeakReference<FragmentActivity>,
    targetView: WeakReference<View?>,
    targetViewId: Int? = null
) : InAppNotificationUiBuilder {

    private val tapTargetBuilder = MaterialTapTargetPrompt.Builder(weakScreen.get()!!)
    private var tapTargetPrompt: MaterialTapTargetPrompt? = null
    private var radius = DEFAULT_RADIUS
    private var padding = DEFAULT_PADDING
    private var outerRadius: Float

    init {
        if (targetViewId != null) {
            tapTargetBuilder.setTarget(targetViewId)
        } else {
            tapTargetBuilder.setTarget(targetView.get())
        }
        tapTargetPrompt = null
        val width = weakScreen.get()!!.resources.displayMetrics.widthPixels.toFloat()
        outerRadius = width - width / 4
    }

    override fun setBackgroundColour(colorId: Int?) =
        apply {
            if (colorId != null)
                tapTargetBuilder.backgroundColour = ContextCompat.getColor(weakScreen.get()!!, colorId)
            else
                tapTargetBuilder.backgroundColour = ContextCompat.getColor(weakScreen.get()!!, R.color.primary)
        }

    override fun setPrimaryText(title: String) = apply { tapTargetBuilder.setPrimaryText(title) }

    override fun setPrimaryTextSize(textSize: Float) =
        apply { tapTargetBuilder.primaryTextSize = weakScreen.get()!!.dpToPixel(textSize) }

    override fun setPrimaryTextGravity(gravity: Int) = apply { tapTargetBuilder.primaryTextGravity = gravity }

    override fun setPrimaryTextTypeFace(typeFace: Typeface, style: Int?) = apply {
        if (style != null) {
            tapTargetBuilder.setPrimaryTextTypeface(typeFace, style)
        } else {
            tapTargetBuilder.primaryTextTypeface = typeFace
        }
    }

    override fun setRadius(radius: Float) = apply { this.radius = radius }

    override fun setPadding(padding: Float) = apply { this.padding = padding }

    fun setPrimaryTextColor(colorId: Int?) = apply {
        tapTargetBuilder.primaryTextColour =
            ContextCompat.getColor(weakScreen.get()!!, colorId ?: R.color.white)
    }

    fun setSecondaryTextColor(colorId: Int?) = apply {
        tapTargetBuilder.secondaryTextColour =
            ContextCompat.getColor(weakScreen.get()!!, colorId ?: R.color.white)
    }

    fun setSecondaryText(subtitle: String?) = apply { subtitle?.let { tapTargetBuilder.setSecondaryText(it) } }

    fun setSecondaryTextSize(textSize: Float) =
        apply { tapTargetBuilder.secondaryTextSize = weakScreen.get()!!.dpToPixel(textSize) }

    fun setSecondaryTextTypeFace(typeFace: Typeface, style: Int? = null) = apply {
        if (style != null) {
            tapTargetBuilder.setSecondaryTextTypeface(typeFace, style)
        } else {
            tapTargetBuilder.secondaryTextTypeface = typeFace
            tapTargetBuilder.textPadding
        }
    }

    fun setFocalPadding(padding: Float) = apply { tapTargetBuilder.focalPadding = padding }

    fun setFocalRadius(radius: Float?) = apply {
        radius?.let {
            tapTargetBuilder.focalRadius = weakScreen.get()!!.dpToPixel(it)
        }
    }

    fun setSecondaryTextGravity(gravity: Int) = apply { tapTargetBuilder.secondaryTextGravity = gravity }

    fun enableCaptureTouchEventOutsidePrompt(enable: Boolean) =
        apply { tapTargetBuilder.captureTouchEventOutsidePrompt = enable }

    fun enableBackButtonDismiss(enable: Boolean) =
        apply { tapTargetBuilder.backButtonDismissEnabled = enable }

    fun setPromptBackground(outerRadius: Float?) =
        apply {
            if (outerRadius != null) {
                this.outerRadius = outerRadius
            }
            tapTargetBuilder.promptBackground = OuterCirclePromptBackground(this.outerRadius)
        }

    fun setPromptChangeListener(listener: MaterialTapTargetPrompt.PromptStateChangeListener?) = apply {
        tapTargetBuilder.setPromptStateChangeListener(listener)
    }

    override fun show() = apply {
        val radiusPx = weakScreen.get()!!.dpToPixel(radius)
        val paddingPx = weakScreen.get()!!.dpToPixel(padding)

        tapTargetBuilder.promptFocal =
            RectanglePromptFocal().setCornerRadius(radiusPx, radiusPx).setTargetPadding(paddingPx)
        tapTargetPrompt = tapTargetBuilder.show()
    }

    override fun removeReferences() {
        tapTargetBuilder.setPromptStateChangeListener(null)
        tapTargetPrompt = null
    }

    fun build() = tapTargetPrompt

    fun dismiss() {
        tapTargetPrompt?.dismiss()
        tapTargetPrompt = null
    }
}
