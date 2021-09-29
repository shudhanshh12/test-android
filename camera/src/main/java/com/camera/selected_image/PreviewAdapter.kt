package com.camera.selected_image

import `in`.okcredit.fileupload.usecase.IImageLoader
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.camera.R
import tech.okcredit.camera_contract.MultiScreenItem

class PreviewAdapter(val imageList: ArrayList<MultiScreenItem>, val imageLoader: IImageLoader) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val DUMMY_VIEW_TYPE = 2
    private val PICTURE_TYPE = 1
    var focusedChild: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == PICTURE_TYPE)
            PreviewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_preview, parent, false)
            )
        else DummyHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false))
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            return DUMMY_VIEW_TYPE
        } else PICTURE_TYPE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PreviewHolder) {
            focusedChild = position
            var pereHolder = holder
            pereHolder.bind(imageList, position, imageLoader)
        }
    }
}
