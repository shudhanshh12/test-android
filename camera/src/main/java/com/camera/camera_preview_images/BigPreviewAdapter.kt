package com.camera.camera_preview_images

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.camera.R
import tech.okcredit.camera_contract.MultiScreenItem

class BigPreviewAdapter(val imagelist: ArrayList<MultiScreenItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val DUMMY_VIEW_TYPE = 2
    private val PICTURE_TYPE = 1
    private var focusedChild: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == PICTURE_TYPE)
            BigPreviewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_preview, parent, false)
            )
        else DummyHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false))
    }

    override fun getItemCount(): Int {
        return imagelist.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            return DUMMY_VIEW_TYPE
        } else PICTURE_TYPE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BigPreviewHolder) {
            focusedChild = position
            holder.bind(imagelist, position)
        }
    }
}
