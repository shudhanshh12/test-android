package `in`.okcredit.dynamicview.component.recycler

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.Environment
import `in`.okcredit.dynamicview.isValidComponent
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyRecyclerView
import timber.log.Timber
import kotlin.math.min

class RecyclerComponentViewHolder : EpoxyHolder() {

    private lateinit var itemView: EpoxyRecyclerView

    private val controller = RecyclerComponentController()

    override fun bindView(itemView: View) {
        this.itemView = itemView as EpoxyRecyclerView
        this.itemView.setController(controller)
    }

    fun render(
        environment: Environment,
        componentModel: RecyclerComponentModel?,
        clickListener: ComponentClickListener? = null
    ) {
        if (componentModel == null || componentModel.items.isNullOrEmpty()) {
            itemView.visibility = View.GONE
            return
        }

        val components = mutableListOf<EpoxyModel<Any>>()
        componentModel.items.forEach {
            if (environment.isValidComponent(it)) {
                environment.componentFactory.create(itemView.context, environment, it)?.let { c ->
                    components.add(c)
                }
            }
        }
        if (components.isNullOrEmpty()) {
            Timber.e("None of the items are supported")
            itemView.visibility = View.GONE
            return
        }

        itemView.apply {
            visibility = View.VISIBLE
            layoutManager = when (componentModel.kind) {
                RecyclerComponentModel.Kind.HORIZONTAL ->
                    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                RecyclerComponentModel.Kind.GRID -> GridLayoutManager(context, min(components.size, 3))
                else -> LinearLayoutManager(context)
            }
            controller.render(components)
        }
        itemView.setOnClickListener { clickListener?.invoke() }
    }
}
