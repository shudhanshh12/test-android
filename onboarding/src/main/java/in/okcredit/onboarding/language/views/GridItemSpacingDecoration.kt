package `in`.okcredit.onboarding.language.views

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class GridItemSpacingDecoration(
    private val space: Int
) : ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val spanCount = (parent.layoutManager as? GridLayoutManager)?.spanCount ?: return

        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val itemCount = parent.adapter?.itemCount ?: return

        val relativeSpace = space / 2

        val noTopMargin = (position < spanCount)
        val noStartMargin = (position % spanCount == 0)
        val noEndMargin = (position % spanCount == spanCount - 1)
        val noBottomMargin = (position > itemCount - (itemCount % spanCount) - 1)

        outRect.left = if (noStartMargin) 0 else relativeSpace
        outRect.right = if (noEndMargin) 0 else relativeSpace
        outRect.bottom = if (noBottomMargin) 0 else relativeSpace
        outRect.top = if (noTopMargin) 0 else relativeSpace
    }
}
