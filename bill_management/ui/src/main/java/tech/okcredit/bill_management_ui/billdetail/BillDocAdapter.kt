package tech.okcredit.bill_management_ui.billdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.info_graphic_carousal.view.*
import tech.okcredit.android.base.utils.ImageCache
import tech.okcredit.bill_management_ui.R
import tech.okcredit.sdk.store.database.LocalBillDoc

class BillDocAdapter(private val imageCache: ImageCache) :
    RecyclerView.Adapter<BillDocAdapter.InfoViewHolder>() {

    private val imgs: ArrayList<LocalBillDoc> = ArrayList()

    class InfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.info_graphic_carousal, parent, false)
        return InfoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imgs.size
    }

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(imgs[position].imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .fallback(R.drawable.placeholder_image)
            .thumbnail(0.25f)
            .into(holder.itemView.img)
    }

    fun setItems(localBillDocList: List<LocalBillDoc>) {
        imgs.clear()
        imgs.addAll(localBillDocList)
        notifyDataSetChanged()
    }
}
