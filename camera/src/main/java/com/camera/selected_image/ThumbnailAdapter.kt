package com.camera.selected_image

import `in`.okcredit.fileupload.usecase.IImageLoader
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.camera.R
import tech.okcredit.camera_contract.MultiScreenItem
import kotlin.collections.ArrayList

class ThumbnailAdapter(val imagelist: ArrayList<MultiScreenItem>, val interactionListener: MultipleImageActivity, val imageLoader: IImageLoader) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val DUMMY_VIEW_TYPE = 2
    private val PICTURE_TYPE = 1
    var selectedPosition = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == PICTURE_TYPE)
            ThumbNailViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_thumbnail, parent, false),
                interactionListener
            )
        else DummyHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_add_bill, parent, false),
            interactionListener
        )
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
        if (holder is ThumbNailViewHolder) {
            val thumbNailViewHolder = holder
            thumbNailViewHolder.bind(imagelist, position, selectedPosition, this, imageLoader)
        }
    }
}
