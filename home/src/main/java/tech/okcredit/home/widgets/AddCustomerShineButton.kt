package tech.okcredit.home.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.home.databinding.AddCustomerShineButtonBinding
import java.util.concurrent.TimeUnit

/**
 * A composite view for home add customer view
 **/

class AddCustomerShineButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    val binding: AddCustomerShineButtonBinding =
        AddCustomerShineButtonBinding.inflate(LayoutInflater.from(context), this)

    fun animateView() {
        Observable.timer(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                AnimationUtils.shineEffect(binding.rootView, binding.viewShine)
            }.subscribe()
    }
}
