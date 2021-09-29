package `in`.okcredit.dynamicview.view

import `in`.okcredit.dynamicview.Environment
import `in`.okcredit.dynamicview.data.model.ComponentModel
import `in`.okcredit.dynamicview.isValidComponent
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.epoxy.EpoxyVisibilityTracker
import timber.log.Timber

class DynamicView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : EpoxyRecyclerView(context, attrs, defStyleAttr) {

    private val controller = DynamicViewController()
    private var componentModel: ComponentModel? = null

    init {
        layoutManager = LinearLayoutManager(context)
        adapter = controller.adapter
        EpoxyVisibilityTracker().attach(this)
    }

    fun render(
        environment: Environment,
        componentModel: ComponentModel?
    ) {
        if (componentModel == null) {
            hideView()
            return
        }

        // This usually happens when render methods gets called multiple times
        if (this.componentModel == componentModel) {
            Timber.e("No need to render again. Same component")
            return
        }

        if (!environment.isValidComponent(componentModel)) {
            Timber.e("${componentModel.kind} not allowed in the given target ($this)")
            hideView()
            return
        }

        val component = environment.componentFactory.create(context, environment, componentModel)
        if (component == null) {
            Timber.e("${componentModel.kind} not supported")
            hideView()
            return
        }

        visibility = View.VISIBLE
        this.componentModel = componentModel
        controller.render(component)
    }

    private fun hideView() {
        visibility = View.GONE
    }
}
