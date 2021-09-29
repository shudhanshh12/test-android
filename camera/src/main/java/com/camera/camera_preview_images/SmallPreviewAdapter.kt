package com.camera.camera_preview_images

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.camera.R
import tech.okcredit.camera_contract.MultiScreenItem

class SmallPreviewAdapter(
    private val imagelist: ArrayList<MultiScreenItem>,
    private val interactionListener: CameraImagesPreview,
    private val addMoreItemId: Int?,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val DUMMY_VIEW_TYPE = 2
    private val PICTURE_TYPE = 1
    var selectedPosition = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == PICTURE_TYPE)
            SmallPreviewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_thumbnail, parent, false),
                interactionListener
            )
        else DummyHolder(
            LayoutInflater.from(parent.context).inflate(addMoreItemId ?: R.layout.item_add_bill, parent, false),
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
        if (holder is SmallPreviewHolder) {
            val thumbNailViewHolder = holder
            thumbNailViewHolder.bind(imagelist, position, selectedPosition, this)
        }
    }
}
