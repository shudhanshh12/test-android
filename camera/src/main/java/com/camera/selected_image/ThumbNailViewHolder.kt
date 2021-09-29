package com.camera.selected_image

import `in`.okcredit.fileupload.usecase.IImageLoader
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.camera.R
import tech.okcredit.android.base.utils.DimensionUtil
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.camera_contract.MultiScreenItem
import java.util.*

class ThumbNailViewHolder(val inflate: View, interactionListener: Listener) : RecyclerView.ViewHolder(inflate) {
    private lateinit var thumbnailAdapter: ThumbnailAdapter
    private var thumbnailContainer: RelativeLayout
    private var capturedPosition: Int = 0
    private lateinit var capturedPic: CapturedImage
    private var imagePreview: ImageView

    init {
        imagePreview = inflate.findViewById(R.id.thumbnail)
        thumbnailContainer = inflate.findViewById(R.id.thumbnail_container)
        imagePreview.setOnClickListener {
            thumbnailAdapter.notifyItemChanged(thumbnailAdapter.selectedPosition)
            thumbnailAdapter.selectedPosition = capturedPosition
            interactionListener.onThumbnailClicked(capturedPic, capturedPosition)
            thumbnailAdapter.notifyItemChanged(capturedPosition)
        }
    }

    fun bind(
        imagelist: ArrayList<MultiScreenItem>,
        position: Int,
        selectedPosition: Int,
        thumbnailAdapter: ThumbnailAdapter,
        imageLoader: IImageLoader
    ) {
        capturedPic = imagelist[position] as CapturedImage
        this.thumbnailAdapter = thumbnailAdapter
        capturedPosition = position
        if (selectedPosition == capturedPosition) {
            thumbnailContainer.background = imagePreview.context.getDrawable(R.drawable.border_reactange_rounded_corner)
        } else thumbnailContainer.background = null
        val requestOptions = RequestOptions().transforms(
            CenterCrop(),
            RoundedCorners(DimensionUtil.dp2px(inflate.context, 4.0f).toInt())
        )

        imageLoader.context(inflate.context)
            .load(capturedPic.file.path)
            .apply(requestOptions)
            .into(imagePreview)
            .priority(Priority.NORMAL)
            .thumbnail(0.1f)
            .buildNormal()
    }

    interface Listener {
        fun onThumbnailClicked(capturedPic: CapturedImage, capturedPosition: Int)
    }
}
