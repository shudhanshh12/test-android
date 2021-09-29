package `in`.okcredit.communication_inappnotification.usecase.builder

import `in`.okcredit.communication_inappnotification.R
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.skydoves.balloon.*
import java.lang.ref.WeakReference

class TooltipBuilder constructor(
    private val weakScreen: WeakReference<FragmentActivity>,
    private val targetView: WeakReference<View>,
) : InAppNotificationUiBuilder {

    private var title: String = ""
    private var textSize: Float = 12f
    private var backgroundColor: Int = R.color.primary
    private var textTypeFace: Typeface? = null
    private var cornerRadius: Float = 8f
    private var padding: Int = 8
    private var textGravity: Int = Gravity.CENTER

    private var arrowOrientation: ArrowOrientation = ArrowOrientation.BOTTOM
    private var arrowPosition: Float = 0.5f
    private var arrowSize: Int = 8
    private var widthRatio: Float = 0.7f
    private var marginRight: Int = 12
    private var marginLeft: Int = 12
    private var alpha: Float = 1f
    private var textColor: Int = R.color.white
    private var dismissOnClicked: Boolean = false
    private var dismissWhenClickedOutside: Boolean = true
    private var animation: BalloonAnimation = BalloonAnimation.FADE
    private var clickListener: OnBalloonClickListener? = null
    private var dismissListener: OnBalloonDismissListener? = null
    private var outsideTouchListener: OnBalloonOutsideTouchListener? = null
    private var autoDismissTime: Long? = null

    // if set to false, Tooltip will be automatically aligned to bottom
    private var alignTop: Boolean = true

    private var tooltipBuilder: Balloon? = null

    private var showWithoutAligning: Boolean = false

    override fun setPrimaryText(title: String) = apply { this.title = title }

    override fun setPrimaryTextSize(textSize: Float) = apply { this.textSize = textSize }

    override fun setBackgroundColour(colorId: Int?) = apply { colorId?.let { this.backgroundColor = it } }

    override fun setPrimaryTextTypeFace(typeFace: Typeface, style: Int?) = apply { textTypeFace = typeFace }

    override fun setRadius(radius: Float) = apply { cornerRadius = radius }

    override fun setPadding(padding: Float) = apply { this.padding = padding.toInt() }

    override fun setPrimaryTextGravity(gravity: Int) = apply { textGravity = gravity }

    fun setArrowOrientation(arrowOrientation: ArrowOrientation) = apply { this.arrowOrientation = arrowOrientation }

    fun setArrowPosition(@FloatRange(from = 0.0, to = 1.0) arrowPosition: Float) =
        apply { this.arrowPosition = arrowPosition }

    fun setArrowSize(arrowSize: Int) = apply { this.arrowSize = arrowSize }

    fun setWidthRatio(widthRatio: Float) = apply { this.widthRatio = widthRatio }

    fun setMarginRight(margin: Int) = apply { this.marginRight = margin }

    fun setMarginLeft(margin: Int) = apply { this.marginLeft = margin }

    fun setAlpha(alpha: Float) = apply { this.alpha = alpha }

    fun setTextColor(color: Int) = apply { this.textColor = color }

    fun enableDismissOnClicked(enable: Boolean) = apply { dismissOnClicked = enable }

    fun enableDismissWhenClickedOutside(enable: Boolean) = apply { dismissWhenClickedOutside = enable }

    fun setAnimation(animation: BalloonAnimation) = apply { this.animation = animation }

    fun setOnClickListener(listener: OnBalloonClickListener?) = apply { this.clickListener = listener }

    fun setOnDismissListener(listener: OnBalloonDismissListener?) = apply { this.dismissListener = listener }

    fun setOnTouchOutsideListener(listener: OnBalloonOutsideTouchListener?) = apply {
        this.outsideTouchListener = listener
    }

    fun setAutoDismissTime(time: Long?) = apply { time?.let { autoDismissTime = it } }

    fun setAlignTop(align: Boolean) = apply { alignTop = align }

    fun enableFreeAlignment(enable: Boolean) = apply { showWithoutAligning = true }

    override fun show() = apply {

        tooltipBuilder = createBalloon(weakScreen.get()!!) {
            setText(title)
            setTextSize(this@TooltipBuilder.textSize)
            setBackgroundColor(ContextCompat.getColor(weakScreen.get()!!, this@TooltipBuilder.backgroundColor))
            this@TooltipBuilder.textTypeFace?.let {
                setTextTypeface(it)
            }
            setCornerRadius(this@TooltipBuilder.cornerRadius)
            setPadding(this@TooltipBuilder.padding)
            setTextGravity(this@TooltipBuilder.textGravity)

            setArrowOrientation(this@TooltipBuilder.arrowOrientation)
            setArrowPosition(this@TooltipBuilder.arrowPosition)
            setWidthRatio(this@TooltipBuilder.widthRatio)
            setMarginRight(this@TooltipBuilder.marginRight)
            setMarginLeft(this@TooltipBuilder.marginLeft)
            setAlpha(this@TooltipBuilder.alpha)
            setTextColor(ContextCompat.getColor(weakScreen.get()!!, this@TooltipBuilder.textColor))
            setDismissWhenClicked(this@TooltipBuilder.dismissOnClicked)
            setDismissWhenTouchOutside(this@TooltipBuilder.dismissWhenClickedOutside)
            setBalloonAnimation(this@TooltipBuilder.animation)
            setLifecycleOwner(weakScreen.get())

            clickListener?.let {
                setOnBalloonClickListener(it)
            }
            dismissListener?.let {
                setOnBalloonDismissListener(it)
            }
            outsideTouchListener?.let {
                setOnBalloonOutsideTouchListener(it)
            }
            autoDismissTime?.let {
                setAutoDismissDuration(it)
            }
        }

        if (showWithoutAligning) {
            tooltipBuilder?.show(targetView.get()!!, 0, 0)
            return@apply
        }

        if (alignTop)
            tooltipBuilder?.showAlignTop(targetView.get()!!)
        else
            tooltipBuilder?.showAlignBottom(targetView.get()!!)
    }

    fun build() = tooltipBuilder

    override fun removeReferences() {
        tooltipBuilder?.setOnBalloonClickListener(null)
        tooltipBuilder?.setOnBalloonDismissListener(null)
        tooltipBuilder?.setOnBalloonOutsideTouchListener(null)
        tooltipBuilder = null
    }
}
