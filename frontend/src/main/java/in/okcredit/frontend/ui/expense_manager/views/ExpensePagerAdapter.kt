package `in`.okcredit.frontend.ui.expense_manager.views

import `in`.okcredit.frontend.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_expense_info_graphic_carousal.view.*

class ExpensePagerAdapter : RecyclerView.Adapter<ExpensePagerAdapter.InfoViewHolder>() {

    private val imgs = arrayListOf<Int>(
        R.drawable.expense_carousel_img_1,
        R.drawable.expense_carousel_img_2,
        R.drawable.expense_carousel_img_3,
        R.drawable.expense_carousel_img_4
    )

    class InfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense_info_graphic_carousal, parent, false)
        return InfoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imgs.size
    }

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        holder.itemView.img.setImageResource(imgs[position])
    }
}
