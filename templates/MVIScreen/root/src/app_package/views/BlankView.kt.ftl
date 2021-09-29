package ${escapeKotlinIdentifiers(packageName)}.${featureName?lower_case}.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.jakewharton.rxbinding3.view.clicks
import java.util.concurrent.TimeUnit

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ${featureName}View @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    interface Listener {
        fun clicked${featureName}View()
    }

    init {
        LayoutInflater.from(ctx).inflate(R.layout.${featureName?lower_case}_view, this, true)
    }

    //@ModelProp
    //fun setProperty(value: String?) {
    //}

    @CallbackProp
    fun setListener(listener: Listener?) {
        rootView.clicks()
            .throttleFirst(50, TimeUnit.MILLISECONDS)
            .doOnNext { listener?.clicked${featureName}View() }
            .subscribe()
    }
}
