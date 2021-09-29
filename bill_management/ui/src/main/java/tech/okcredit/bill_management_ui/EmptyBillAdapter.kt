package tech.okcredit.bill_management_ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.info_graphic_carousal.view.*

class EmptyBillAdapter : RecyclerView.Adapter<EmptyBillAdapter.InfoViewHolder>() {

    private val imgs = arrayListOf<Int>(
        R.drawable.ic_bill_management_img_1,
        R.drawable.ic_bill_management_img_2,
        R.drawable.ic_bill_management_img_3
    )

    class InfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.empty_bill_info_graphic_carousal, parent, false)
        return InfoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imgs.size
    }

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        holder.itemView.img.setImageResource(imgs[position])
    }
}
