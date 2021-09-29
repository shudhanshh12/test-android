package `in`.okcredit.dynamicview.component.dashboard.recycler_card

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.Environment
import `in`.okcredit.dynamicview.R
import `in`.okcredit.dynamicview.component.dashboard.recycler_card.RecyclerCardComponentModel.Kind.Companion.DEFAULT_GRID_SPAN_COUNT
import `in`.okcredit.dynamicview.isValidComponent
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyRecyclerView
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import timber.log.Timber
import kotlin.math.min

class RecyclerCardComponentViewHolder : EpoxyHolder() {

    private lateinit var itemView: View
    private lateinit var title: TextView
    private lateinit var ctaLabel: TextView
    private lateinit var recyclerView: EpoxyRecyclerView
    private lateinit var clRoot: ConstraintLayout

    private val controller = RecyclerCardComponentController()

    override fun bindView(itemView: View) {
        this.itemView = itemView
        title = itemView.findViewById(R.id.title)
        ctaLabel = itemView.findViewById(R.id.cta_label)
        recyclerView = itemView.findViewById(R.id.recycler_view)
        clRoot = itemView.findViewById(R.id.cl_root)
        recyclerView.setController(controller)
    }

    fun render(
        environment: Environment,
        componentModel: RecyclerCardComponentModel?,
        clickListener: ComponentClickListener? = null
    ) {
        if (componentModel == null || componentModel.items.isNullOrEmpty()) {
            itemView.gone()
            return
        }
        val components = mutableListOf<EpoxyModel<Any>>()
        componentModel.items.forEach {
            if (environment.isValidComponent(it)) {
                environment.componentFactory.create(itemView.context, environment, it)?.let { c ->
                    components.add(c)
                    componentModel.metadata?.itemCount?.let { itemCount ->
                        // limit number of items to be shown if itemCount is present
                        if (itemCount == components.size)
                            return@forEach
                    }
                }
            }
        }
        if (components.isNullOrEmpty()) {
            Timber.e("None of the items are supported")
            itemView.gone()
            return
        }

        componentModel.title?.let {
            title.visible()
            title.text = it
        } ?: title.gone()

        componentModel.ctaText?.let {
            ctaLabel.visible()
            ctaLabel.text = it
        } ?: ctaLabel.gone()

        ctaLabel.setOnClickListener { clickListener?.invoke() }

        componentModel.bgColor?.let {
            try {
                clRoot.setBackgroundColor(Color.parseColor(it))
            } catch (_: IllegalArgumentException) {
            }
        } ?: clRoot.setBackgroundColor(Color.WHITE)

        recyclerView.apply {
            visible()
            layoutManager = when (componentModel.kind) {
                RecyclerCardComponentModel.Kind.HORIZONTAL ->
                    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                RecyclerCardComponentModel.Kind.GRID -> {
                    GridLayoutManager(
                        context,
                        min(components.size, componentModel.metadata?.spanCount ?: DEFAULT_GRID_SPAN_COUNT)
                    )
                }
                else -> LinearLayoutManager(context)
            }
            controller.render(components)
        }
    }
}
